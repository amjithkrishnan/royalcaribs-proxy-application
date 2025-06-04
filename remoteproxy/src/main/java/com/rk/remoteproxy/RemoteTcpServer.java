package com.rk.remoteproxy;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Component
public class RemoteTcpServer {
	
	@Value("${remote.listen-port:9090}")
	private int LISTEN_PORT;

	@PostConstruct
	public void start() throws IOException {
		ServerSocket serverSocket = new ServerSocket(LISTEN_PORT);
		System.out.println("Offshore proxy listening on port " + LISTEN_PORT);

		new Thread(() -> {
			while (true) {
				try {
					Socket shipConnection = serverSocket.accept();
					System.out.println("Received connection from ship: " + shipConnection.getRemoteSocketAddress());
					handleShipConnection(shipConnection);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	private void handleShipConnection(Socket socket) {
		new Thread(() -> {
			try (DataInputStream dis = new DataInputStream(socket.getInputStream());
					DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {

				HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
				while (true) {
					int length = dis.readInt();
					byte[] requestBytes = dis.readNBytes(length);
					String httpRequestString = new String(requestBytes, StandardCharsets.UTF_8);

					System.out.println("Offshore proxy received HTTP request:");
					System.out.println(httpRequestString.split("\r\n")[0]);

					String[] lines = httpRequestString.split("\r\n");
					String uriLine = lines[0];
					String uri = uriLine.split(" ")[1];

					HttpRequest httpRequest = HttpRequest.newBuilder().uri(URI.create(uri))
							.timeout(Duration.ofSeconds(10)).GET().build();
					
					HttpResponse<byte[]> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofByteArray());

					String headers = "HTTP/1.1 " + response.statusCode() + " OK\r\n" + "Content-Length: "
							+ response.body().length + "\r\n" + "Content-Type: text/html\r\n"
							+ "Connection: close\r\n\r\n";

					byte[] responseBytes = headers.getBytes(StandardCharsets.UTF_8);
					ByteArrayOutputStream fullResponse = new ByteArrayOutputStream();
					fullResponse.write(responseBytes);
					fullResponse.write(response.body());

					byte[] fullResponseBytes = fullResponse.toByteArray();
					dos.writeInt(fullResponseBytes.length);
					dos.write(fullResponseBytes);
					dos.flush();
					
					System.out.println("Sent response of size " + fullResponseBytes.length + " bytes to ship");


				}
			} catch (Exception e) {
				System.err.println("Error in offshore proxy: " + e.getMessage());
				e.printStackTrace();
			}
		}).start();
	}
}
