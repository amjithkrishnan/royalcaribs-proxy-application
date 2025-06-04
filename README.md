# royalcaribs-proxy-application

## How to Build and Run

### Prerequisites
- Java (e.g., OpenJDK 21)
- Maven
- Docker
- Docker Compose

### Build and Run

cd remoteproxy
mvn clean package

docker build -t remote-server:latest -f Dockerfile.txt .

cd ../shipproxy
mvn clean package

docker build -t ship-proxy:latest -f Dockerfile.txt .

cd ..

docker-compose build
docker-compose up -d
