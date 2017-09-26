resolvers in ThisBuild ++= Seq("Apache Development Snapshot Repository" at "https://repository.apache.org/content/repositories/snapshots/",
  Resolver.mavenLocal)

name := "Flink"

version := "0.0.1-SNAPSHOT"

organization := "com.mwronski.flink"

scalaVersion in ThisBuild := "2.11.11"

val flinkVersion = "1.3.2"
val flinkKafkaVersion = "0.10"

val flinkDependencies = Seq(
  "org.apache.flink" %% "flink-scala" % flinkVersion % "provided",
  "org.apache.flink" %% "flink-streaming-scala" % flinkVersion % "provided",
  "org.apache.flink" % "flink-connector-kafka-0.10_2.11" % "1.2.0",
  "io.confluent" % "kafka-streams-avro-serde" % "3.3.0"
)

lazy val root = (project in file(".")).
  settings(
    libraryDependencies ++= flinkDependencies
  )

mainClass in assembly := Some("com.mwronski.flink.stream.StreamSocketWindowWordCount")

// make run command include the provided dependencies
run in Compile := Defaults.runTask(fullClasspath in Compile,
  mainClass in(Compile, run),
  runner in(Compile, run)
).evaluated

// exclude Scala library from assembly
assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)
