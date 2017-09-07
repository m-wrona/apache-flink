package com.mwronski.flink.stream

import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.api.windowing.time.Time

object StreamSocketWindowWordCount {
  def main(args: Array[String]) {

    val env = StreamExecutionEnvironment.getExecutionEnvironment

    val text = env.socketTextStream("localhost", 9000, '\n')

    val counts = text.flatMap {
      _.toLowerCase.split("\\s+")
    }
      .map {
        (_, 1)
      }
      .keyBy(0)
      .timeWindow(Time.seconds(30))
      .sum(1)

    counts.print()
    env.execute("WordCount")

  }

}
