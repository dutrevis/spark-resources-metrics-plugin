package io.github.dutrevis

import scala.io.Source

trait ProcFileMetricCollector {
  protected val procFileName: String

  protected def getMetricValue(
      procFileName: String,
      originalMetricName: String
  ): Any

  protected def procFileSource(procFilePath: String) {
    Source.fromFile(procFilePath)
  }

}
