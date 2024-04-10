package io.github.dutrevis

trait ProcFileMetricCollector {
  protected val procFileName: String

  protected def getMetricValue(
      procFileName: String,
      originalMetricName: String
  ): Any
}
