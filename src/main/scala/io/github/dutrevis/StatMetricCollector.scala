package io.github.dutrevis

class StatMetricCollector extends ProcFileMetricCollector {

  override val procFilePath = "/proc/stat"

  /** A List with the names of the columns present in the `/proc/stat` file.
    * They are used as keys of a Map that points to the values collected from
    * the file. <p>
    */
  val procFileCPUColumns = List(
    "cpu_user",
    "cpu_nice",
    "cpu_system",
    "cpu_idle",
    "cpu_iowait",
    "cpu_irq",
    "cpu_softirq"
  )

  /** Gets the value of a metric from the content of a proc file provided. The
    * metric is searched according to the original name provided in the
    * parameter `originalMetricName`. <p>
    * @param procFileContent
    *   a String with the content of the desired proc file <p>
    * @param originalMetricName
    *   the original metric name by which it is found in the proc file <p>
    * @return
    *   the metric value as Double
    * @throws NoSuchElementException
    *   if a metric is not found with the provided original name
    */
  override def getMetricValue(
      procFileContent: String,
      originalMetricName: String
  ): Double = {
    val procFileData = (procFileCPUColumns
      .zip(
        procFileContent.linesIterator
          .take(1)
          .mkString
          .split(" +")
          .tail
          .map(_.toLong)
      )
      .toMap)
    val metricValue =
      procFileData(originalMetricName) / procFileData.foldLeft(0.0)(_ + _._2)
    metricValue
  }

}
