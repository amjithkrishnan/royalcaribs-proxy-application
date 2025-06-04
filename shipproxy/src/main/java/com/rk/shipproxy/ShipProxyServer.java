package com.rk.shipproxy;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.*;
import java.util.UUID;
import java.util.concurrent.*;

@Component
public class ShipProxyServer {

	@Value("${offshore.host}")
	private String offshoreHost;

	@Value("${offshore.port}")
	private int offshorePort;

	@Value("${shipproxy.listen-port:8080}")
	private int listenPort;

	private Socket serverConnection;
	private final BlockingQueue<Socket> requestQueue = new LinkedBlockingQueue<>();

	@PostConstruct
	public void start() {
		try {
			connectToOffshoreProxy();
			new Thread(this::acceptBrowserConnections, "acceptor").start();
			new Thread(this::processQueueSequentially, "queue-processor").start();
		} catch (IOException e) {
			System.err.println("Failed to connect to offshore proxy: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void connectToOffshoreProxy() throws IOException {
		serverConnection = new Socket(offshoreHost, offshorePort);
		System.out.println("Connected to offshore proxy at " + offshoreHost + ":" + offshorePort);
	}

	private void acceptBrowserConnections() {
		try (ServerSocket localProxyServer = new ServerSocket(listenPort)) {
			System.out.println("Ship proxy listening on port " + listenPort);
			while (true) {
				Socket clientSocket = localProxyServer.accept();
				requestQueue.offer(clientSocket);
			}
		} catch (IOException e) {
			System.err.println("Error while accepting connections: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void processQueueSequentially() {
		while (true) {
			try {
				Socket browserSocket = requestQueue.take();
				System.out.println("Processing browser request on thread: " + Thread.currentThread().getName());
				handleRequest(browserSocket);
			} catch (Exception e) {
				System.err.println("Failed to process request: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	private void handleRequest(Socket browserSocket) throws IOException {
		try (InputStream clientIn = browserSocket.getInputStream();
				OutputStream clientOut = browserSocket.getOutputStream()) {
			ByteArrayOutputStream requestBuffer = new ByteArrayOutputStream();
			byte[] buffer = new byte[4096];
			int bytesRead;

			while ((bytesRead = clientIn.read(buffer)) != -1) {
				requestBuffer.write(buffer, 0, bytesRead);
				if (bytesRead < buffer.length)
					break;
			}

			byte[] requestBytes = requestBuffer.toByteArray();
			
			String reqId = UUID.randomUUID().toString();
			
			System.out.println("Request Id : "+ reqId); 

			System.out.println("Preparing to send " + requestBytes.length + " bytes to offshore proxy for Request Id : "+reqId);

			synchronized (this) {

				if (serverConnection == null || serverConnection.isClosed() || !serverConnection.isConnected()) {
					reconnectToOffshore();
				}

				OutputStream toServer = serverConnection.getOutputStream();
				InputStream fromServer = serverConnection.getInputStream();

				DataOutputStream dos = new DataOutputStream(toServer);
				dos.writeInt(requestBytes.length); // Send length prefix
				dos.write(requestBytes); // Send actual data
				dos.flush();

				// Read framed response
				DataInputStream dis = new DataInputStream(fromServer);
				int responseLen = dis.readInt();
				byte[] responseBuffer = dis.readNBytes(responseLen);

				System.out.println("Received response of length " + responseLen + " bytes from offshore for Request Id : "+reqId);

				clientOut.write(responseBuffer);
				clientOut.flush();
			}
		} catch (Exception e) {

			System.err.println("Error during communication with offshore proxy: " + e.getMessage());
			closeServerConnection();
			throw e;

		} finally {
			browserSocket.close();
		}
	}

	private synchronized void closeServerConnection() {
		if (serverConnection != null && !serverConnection.isClosed()) {
			try {
				serverConnection.close();
				System.out.println("Closed offshore proxy connection due to error.");
			} catch (IOException e) {
				System.err.println("Failed to close offshore connection: " + e.getMessage());
			} finally {
				serverConnection = null;
			}
		}
	}

	private synchronized void reconnectToOffshore() throws IOException {
		System.out.println("Reconnecting to offshore proxy...");
		closeServerConnection();
		connectToOffshoreProxy();
	}
}