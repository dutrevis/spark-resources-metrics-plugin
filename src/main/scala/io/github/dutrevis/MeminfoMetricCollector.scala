package io.github.dutrevis

class MeminfoMetricCollector extends ProcFileMetricCollector {

  override val procFilePath = "/proc/meminfo"

  /** Gets the value of a metric from the content of a proc file provided. The
    * metric is searched according to the original name provided in the
    * parameter `originalMetricName`. <p>
    * @param procFileContent
    *   a String with the content of the desired proc file <p>
    * @param originalMetricName
    *   the original metric name by which it is found in the proc file <p>
    * @return
    *   the metric value as Long
    * @throws NoSuchElementException
    *   if a metric is not found with the provided original name
    */
  override def getMetricValue(
      procFileContent: String,
      originalMetricName: String
  ): Long = {
    val procFileData = procFileContent.linesIterator
      .filter(metricLine => metricLine.toString.contains(originalMetricName))
      .map { case s: String => s.split(":") }
      .map { case Array(k, v) => k -> v.trim().split(" ").head.toLong }
      .toMap
    val metricValue = procFileData(originalMetricName)
    metricValue
  }

}
