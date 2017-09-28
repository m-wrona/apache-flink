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

Task managers can be auto-scaled and will be managed by job-manager.

More details can be found in [presentation](https://www.slideshare.net/dataArtisans/apache-flink-training-deployment-operations).

## Dashboard management

1) Run k8s proxy

```
kubectl proxy
```

2) Enter the [UI](http://localhost:8001/api/v1/proxy/namespaces/default/services/flink-jobmanager:8081/#/overview)

or check URL of `job-manager service` (i.e. for minikube: `minikube service --url flink-jobmanager`).

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

Before automating you might want to check [configuration](https://ci.apache.org/projects/flink/flink-docs-release-1.3/setup/config.html) and `blob.*` properties (especially `blob.storage.directory` - directory for storing blobs (such as user JARs) on the TaskManagers.)

This section described steps to create bash script for automated deployment:

1) Find ID of running job-manager pod

```
kubectl get pods
```

sample output:

```
NAME                                    READY     STATUS    RESTARTS   AGE
po/flink-jobmanager-2891032829-34d02    1/1       Running   0          1h
po/flink-taskmanager-1070171901-9tb9f   1/1       Running   0          1h
po/flink-taskmanager-1070171901-l5ld9   1/1       Running   0          1h
```

2) Copy jar file to job-manager

```
kubectl cp target/scala-2.11/flink_2.11-0.0.1-SNAPSHOT.jar flink-jobmanager-2891032829-34d02:/opt/flink/
```

3) Run jar in job manager

a) enter job manager

```
kubectl exec -it flink-jobmanager-2891032829-34d02 -- /bin/bash
```

b) run jar using [CLI](https://ci.apache.org/projects/flink/flink-docs-release-1.2/setup/cli.html) with sample main class (i.e. `com.mwronski.flink.batch.BatchWordCount`)

```

```

# Documents

* [Metrics](https://ci.apache.org/projects/flink/flink-docs-release-1.3/monitoring/metrics.html)

* [Logging](https://ci.apache.org/projects/flink/flink-docs-release-1.3/monitoring/logging.html)

* [Rest API](https://ci.apache.org/projects/flink/flink-docs-release-1.3/monitoring/rest_api.html)

* [Production ready checklist](https://ci.apache.org/projects/flink/flink-docs-release-1.3/ops/production_ready.html)