package io.github.dutrevis

import scala.io.{Source, BufferedSource}

class StatMetricCollector(
    sourceMethod: String => BufferedSource = Source.fromFile
) extends ProcFileMetricCollector {

  override val procFilePath = "/proc/stat"

  val procFileCPUColumns = List(
    "cpu_user",
    "cpu_nice",
    "cpu_system",
    "cpu_idle",
    "cpu_iowait",
    "cpu_irq",
    "cpu_softirq"
  )

  /** Gets the value of a metric from a proc file located at the `procFilePath`
    * value set. The metric is searched according to the original name provided
    * in the parameter `originalMetricName`. <p>
    * @param procFileSource
    *   a BufferedSource instance, with access to the desired proc file <p>
    * @param originalMetricName
    *   the original metric name by which it is found in the proc file <p>
    * @return
    *   the metric value as Long
    * @throws NoSuchElementException
    *   if a metric is not found with the provided original name
    */
  override def getMetricValue(
      procFileSource: BufferedSource,
      originalMetricName: String
  ): Double = {
    val procFileData = (procFileCPUColumns
      .zip(
        procFileSource.getLines
          .take(1)
          .mkString
          .split(" +")
          .tail
          .map(_.toLong)
      )
      .toMap)
    val metricValue =
      procFileData(originalMetricName) / procFileData.foldLeft(0.0)(_ + _._2)
    procFileSource.close()
    metricValue
  }

  /** Access and buffers a proc file located at the `procFilePath`. <p>
    * @return
    *   a `BufferedSource` instance of the file read
    */
  override def getProcFileSource(): scala.io.BufferedSource = {
    sourceMethod(procFilePath)
  }
}
