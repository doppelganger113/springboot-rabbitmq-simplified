# Spring boot RabbitMQ simplified

![GitHub Action](https://github.com/doppelganger113/springboot-rabbitmq-simplified/workflows/Java%20CI%20with%20Maven/badge.svg)

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