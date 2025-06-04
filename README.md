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

### Test

curl -x http://localhost:8080 http://httpforever.com/

####For trigger multiple request at a time
####Windows powershell
1..10 | ForEach-Object {
  Start-Job { curl.exe -x http://localhost:8080 http://httpforever.com/ } 
}
Get-Job | Wait-Job

#####Linux

for i in {1..10}; do
  curl -x http://localhost:8080 http://httpforever.com/ &
done
wait


### Logs

docker logs -f ship-proxy
