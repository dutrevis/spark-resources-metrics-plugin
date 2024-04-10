package io.github.dutrevis

import scala.io.Source

trait ProcFileMetricCollector {
  protected val procFilePath: String

  protected def getMetricValue(
      procFileName: String,
      originalMetricName: String
  ): Any

  protected def procFileSource(): scala.io.Source = {
    Source.fromFile(procFilePath)
  }

}
