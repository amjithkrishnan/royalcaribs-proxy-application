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
