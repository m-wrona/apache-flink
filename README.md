# apache-flink

Repo for learning about Apache Flink

# DevOps

DevOps is prepared for [Kubernetes](https://ci.apache.org/projects/flink/flink-docs-release-1.3/setup/kubernetes.html).

Pre-requisites:

* minikube (local development)

1) Create Flink cluster

```
kubectl apply -f devops
```

## Dashboard management

1) Run k8s proxy

```
kubectl proxy
```

2) Enter the [UI](http://localhost:8001/api/v1/proxy/namespaces/default/services/flink-jobmanager:8081/#/overview)

# Local development

Pre-requisites: 

* Scala 

* Sbt

```
sbt run
```

## Samples

#### Sockets

You can run socket-based samples as following:

1) Open socket

```
nc -l 9000
```

2) Start job

#### Kafka

You can run Kafka-based samples as following:

```
docker-compose up
```

Other commands:

```
kafka-avro-console-consumer --topic song-feed --bootstrap-server localhost:9092 --from-beginning
```

```
kafka-topics --list --zookeeper localhost:32181
```