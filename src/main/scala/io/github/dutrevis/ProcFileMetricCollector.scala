package io.github.dutrevis

import scala.io.{Source, BufferedSource}

trait ProcFileMetricCollector {
  protected val procFilePath: String

  def getMetricValue(
      procFileSource: BufferedSource,
      originalMetricName: String
  ): Any

  def getProcFileSource(): scala.io.BufferedSource

}
