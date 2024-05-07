package io.github.dutrevis

import java.nio.charset.{Charset, StandardCharsets}
import java.nio.file.{Files, Path}

trait ProcFileMetricCollector {
  protected val procFilePath: String

  /** Method to be implemented with the logic to get a metric value from a file
    * content (provided as a String) using a given metric name.<p>
    * @param procFileContent
    *   a String with a file content from which the metric will be collected.<p>
    * @param originalMetricName
    *   a String with the metric name, used to search and collect its value from
    *   the file content.<p>
    */
  def getMetricValue(
      procFileContent: String,
      originalMetricName: String
  ): Any

  /** Gets the file path from a String or a sequence of Strings. Used as the
    * `path_getter` default function.<p>
    * @param s1
    *   a String used to compose the first element of a file path.<p>
    * @param s2
    *   an optional sequence of Strings used to compose the last elements of a
    *   file path.<p>
    * @return
    *   a `Path` object with the file path.
    */
  def defaultPathGetter(s1: String, s2: String*): Path = Path.of(s1, s2: _*)

  /** Returns the String content of a proc file located at the `procFilePath`,
    * enconded using a provided charset. <p>
    * @param path_getter
    *   a Function used to get the file path from a String or a sequence of
    *   Strings, returning a `Path` object.<p>
    * @param file_reader
    *   a Function that uses a `Path` object to get the content of a file,
    *   returning it in the format of an Array of bytes.<p>
    * @param charset
    *   a `Charset` object used to define the final String encoding.<p>
    * @return
    *   a `String` with the content of the file read.
    */
  def getProcFileContent(
      path_getter: ((String, Seq[String]) => Path) = defaultPathGetter,
      file_reader: ((Path) => Array[Byte]) = Files.readAllBytes,
      charset: Charset = StandardCharsets.UTF_8
  ): String = {
    new String(
      file_reader(path_getter(procFilePath, Nil)),
      charset
    )
  }

}
