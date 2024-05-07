import io.github.dutrevis.ProcFileMetricCollector
import org.scalatest.funsuite.AnyFunSuite
import org.scalamock.scalatest.MockFactory

import java.nio.charset.{StandardCharsets, Charset}
import java.nio.file.{Files, Path}

class CollectorClass extends ProcFileMetricCollector {
  override val procFilePath: String = "any/path"

  def getMetricValue(procFileContent: String, originalMetricName: String): Any =
    ()

}

class ProcFileMetricCollectorTest extends AnyFunSuite with MockFactory {

  test("Method getProcFileContent should return specific Path") {

    val metricCollector = new CollectorClass
    val charsetTest = StandardCharsets.UTF_8
    val stringSeqTest = "any/string"

    val expectedPath = Path.of(stringSeqTest, stringSeqTest)

    assertResult(expectedPath) {
      metricCollector.defaultPathGetter(stringSeqTest, stringSeqTest)
    }
  }

  test("Method getProcFileContent should return specific String") {
    val pathMock = mock[Path]
    val pathOfMethod = mockFunction[String, Seq[String], Path]
    val pathReadAllBytesMethod = mockFunction[Path, Array[Byte]]

    val metricCollector = new CollectorClass
    val charsetTest = StandardCharsets.UTF_8
    val byteArrayTest = "Any String".getBytes()

    val expectedString = new String(byteArrayTest, charsetTest)

    pathOfMethod
      .expects(metricCollector.procFilePath, Nil)
      .returns(pathMock)

    pathReadAllBytesMethod
      .expects(pathMock)
      .returns(byteArrayTest)

    assertResult(expectedString) {
      metricCollector.getProcFileContent(
        pathOfMethod,
        pathReadAllBytesMethod,
        charsetTest
      )
    }
  }

}
