## Requirements
 - Docker and docker-compose
 
### Pre-requisites
Start RabbitMQ via docker
```bash
docker-compose up -d --build
```

### Usage
Test if server is running
``bash
curl localhost:8080
``
Issue a message with
```bash
curl 'localhost:8080/message?value=test'
```