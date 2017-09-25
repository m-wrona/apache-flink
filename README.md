# apache-flink

Repo for learning about Apache Flink

# Local development

```
sbt run
```

You can run socket-based samples as following:

1) Open socket

```
nc -l 9000
```

2) Start job

# Samples

* StreamSocketWindowWordCount: count number of words from the socket based on time window


## Kafka commands

```
kafka-avro-console-consumer --topic song-feed --bootstrap-server localhost:9092 --from-beginning
```

```
kafka-topics --list --zookeeper localhost:32181
```