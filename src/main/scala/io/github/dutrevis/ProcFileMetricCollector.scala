package io.github.dutrevis

import scala.io.Source

trait ProcFileMetricCollector {
  protected val procFilePath: String

  protected def getMetricValue(originalMetricName: String): Any

  protected def getProcFileSource(): scala.io.Source = {
    Source.fromFile(procFilePath)
  }

}
