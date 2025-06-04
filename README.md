# 🚢 royalcaribs-proxy-application

## 📦 Prerequisites

Ensure the following tools are installed on your system:

- [Java OpenJDK 21](https://jdk.java.net/21/)
- [Apache Maven](https://maven.apache.org/)
- [Docker](https://www.docker.com/)
- [Docker Compose](https://docs.docker.com/compose/)

---

## 🔧 How to Build and Run

```bash
# Navigate to remoteproxy directory
cd remoteproxy

# Build the Java project
mvn clean package

# Build Docker image for remoteproxy
docker build -t remote-server:latest -f Dockerfile.txt .

# Navigate to shipproxy directory
cd ../shipproxy

# Build the Java project
mvn clean package

# Build Docker image for shipproxy
docker build -t ship-proxy:latest -f Dockerfile.txt .

# Go back to root directory
cd ..

# Build and start containers using Docker Compose
docker-compose build
docker-compose up -d
```

---

## 🌐 Test the Proxy

### 🔹 Single Request

```bash
curl -x http://localhost:8080 http://httpforever.com/
```

### 🔹 Multiple Requests

#### ✅ Windows PowerShell

```powershell
1..10 | ForEach-Object {
  Start-Job { curl.exe -x http://localhost:8080 http://httpforever.com/ }
}
Get-Job | Wait-Job
```

#### ✅ Linux / macOS

```bash
for i in {1..10}; do
  curl -x http://localhost:8080 http://httpforever.com/ &
done
wait
```

---

## 📜 View Logs

To view logs from the `ship-proxy` service:

```bash
docker logs -f ship-proxy
```

---

## 📝 Observation (Based on Problem Statement)

According to the design requirement, the system must handle HTTP/HTTPS requests **sequentially** over a single persistent TCP connection between the ship (proxy client) and the offshore proxy server.

From the ample logs, we can confirm this behavior:

```
Request Id : 6f187215-d1de-423d-a8d5-d559119bd712
Preparing to send 131 bytes to offshore proxy for Request Id : 6f187215-d1de-423d-a8d5-d559119bd712
Received response of length 5209 bytes from offshore for Request Id : 6f187215-d1de-423d-a8d5-d559119bd712
Processing browser request on thread: queue-processor

Request Id : 9caea147-d99c-4bc1-b0a6-2ce2e4cd5faf
Preparing to send 131 bytes to offshore proxy for Request Id : 9caea147-d99c-4bc1-b0a6-2ce2e4cd5faf
Received response of length 5209 bytes from offshore for Request Id : 9caea147-d99c-4bc1-b0a6-2ce2e4cd5faf
Processing browser request on thread: queue-processor
```

Despite multiple requests being sent concurrently (e.g., using a PowerShell or Linux loop), the **log output clearly shows the requests are processed one after another**, ensuring only one is handled at a time.

