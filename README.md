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

**Note:** 

There is only one instance of job-manager since it's assumed that k8s will restart it automatically.

However after restart of job-manager info about jobs is lot and must be restored manually.

So if failure recovery should be supported for job-manager then cluster of job-managers must be created using `Zookeeper`.

Task managers can be auto-scaled and will be managed by job-manager.

More details can be found in [presentation](https://www.slideshare.net/dataArtisans/apache-flink-training-deployment-operations).

## Dashboard management

1) Run k8s proxy

```
kubectl proxy
```

2) Enter the [UI](http://localhost:8001/api/v1/proxy/namespaces/default/services/flink-jobmanager:8081/#/overview)

or check URL of `job-manager service`:

```
kubectl get service flink-jobmanager
```

or for `minikube` @ localhost:

```
minikube service --url flink-jobmanager
```

# Local development

Pre-requisites: 

* Scala 

* Sbt

```
sbt run
```

or to build:

```
sbt package
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

# Deployment

## Manual deployment using UI

1) Go to UI console -> `Submit new job`

2) Upload jar

3) Run uploaded jar with chosen main class (i.e. `com.mwronski.flink.batch.BatchWordCount`)

## Automated deployment

Before automating you might want to check [configuration](https://ci.apache.org/projects/flink/flink-docs-release-1.3/setup/config.html) and `blob.*` properties (especially `blob.storage.directory` - directory for storing blobs (such as user JARs) on the TaskManagers).

This section describes how to run task using [REST API](https://ci.apache.org/projects/flink/flink-docs-release-1.3/monitoring/rest_api.html#submitting-programs).

**Note:** All below steps should be placed in bash script for automation purposes.

1) Deploy JAR file

```
curl -v -F  upload=@a.jar http://192.168.99.100:31273/jars/upload
```

2) Get ID of uploaded JAR File

```
curl http://192.168.99.100:31273/jars
```

3) Run task based on found ID of a JAR file

```
curl -XPOST http://192.168.99.100:31273/jars/5935d344-c7a8-44ed-898e-8a005262ecc5_a.jar/run?entry-class=com.mwronski.flink.batch.BatchWordCount
```

4) Check job status

```
curl http://192.168.99.100:31273/jobs
```

# Documents

* [Metrics](https://ci.apache.org/projects/flink/flink-docs-release-1.3/monitoring/metrics.html)

* [Logging](https://ci.apache.org/projects/flink/flink-docs-release-1.3/monitoring/logging.html)

* [Rest API](https://ci.apache.org/projects/flink/flink-docs-release-1.3/monitoring/rest_api.html)

* [Production ready checklist](https://ci.apache.org/projects/flink/flink-docs-release-1.3/ops/production_ready.html)