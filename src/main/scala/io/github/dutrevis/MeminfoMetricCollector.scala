package io.github.dutrevis

import scala.io.{Source, BufferedSource}

class MeminfoMetricCollector(
    sourceMethod: String => BufferedSource = Source.fromFile
) extends ProcFileMetricCollector {

  override val procFilePath = "/proc/meminfo"

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
  ): Long = {
    val procFileData = procFileSource.getLines
      .filter(metricLine => metricLine.contains(originalMetricName))
      .map { case s: String => s.split(":") }
      .map { case Array(k, v) => k -> v.trim().split(" ").head.toLong }
      .toMap
    val metricValue = procFileData(originalMetricName)
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
