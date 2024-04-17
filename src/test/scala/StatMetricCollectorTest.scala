import io.github.dutrevis.StatMetricCollector
import org.scalatest.funsuite.AnyFunSuite
import org.scalamock.scalatest.MockFactory
import org.mockito.{MockitoSugar, ArgumentMatchersSugar}
import org.mockito.integrations.scalatest.ResetMocksAfterEachTest
import org.mockito.captor.ArgCaptor
import scala.io.{Source, BufferedSource}

import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

class StatMetricCollectorTest
    extends AnyFunSuite
    with MockitoSugar
    with ArgumentMatchersSugar
    with ResetMocksAfterEachTest {

  // Arrange common mocks
  val procFileSourceMock = mock[BufferedSource]

  // Arrange common values
  val procFileDataTest = Map[String, Long](
    "cpu_user" -> 79242,
    "cpu_nice" -> 0,
    "cpu_system" -> 74306,
    "cpu_idle" -> 842486413,
    "cpu_iowait" -> 756859,
    "cpu_irq" -> 6140,
    "cpu_softirq" -> 67701
  )

  val procFileContent: String = new String(
    Files.readAllBytes(
      Paths
        .get(".")
        .toAbsolutePath
        .getParent()
        .resolve("src/test/scala/proc/stat")
    ),
    StandardCharsets.UTF_8
  )

  test("Method getMetricValue should return specific Double value") {
    val metricCollector = new StatMetricCollector()
    val procFileSourceTest = (new BufferedSource(
      new ByteArrayInputStream(procFileContent.getBytes)
    ))
    val originalMetricName: String = "cpu_user"
    val expectedDoubleValue: Double =
      procFileDataTest(originalMetricName) / procFileDataTest.foldLeft(0.0)(
        _ + _._2
      )

    assertResult(expectedDoubleValue) {
      metricCollector.getMetricValue(procFileSourceTest, originalMetricName)
    }
  }

  test("Method getMetricValue should call BufferedSource.getLines") {
    val metricCollector = new StatMetricCollector()
    val procFileSourceTest = (new BufferedSource(
      new ByteArrayInputStream(procFileContent.getBytes)
    ))
    val originalMetricName: String = "cpu_user"

    when(procFileSourceMock.getLines())
      .thenReturn(procFileSourceTest.getLines())

    metricCollector.getMetricValue(procFileSourceMock, originalMetricName)

    verify(procFileSourceMock, times(1)).getLines()
  }

  test("Method getMetricValue should throw NoSuchElementException") {
    val metricCollector = new StatMetricCollector()
    val procFileSourceTest = (new BufferedSource(
      new ByteArrayInputStream(procFileContent.getBytes)
    ))
    val originalMetricName: String = "wrong_metric"

    when(procFileSourceMock.getLines())
      .thenReturn(procFileSourceTest.getLines())
    assertThrows[NoSuchElementException](
      metricCollector.getMetricValue(procFileSourceMock, originalMetricName)
    )
  }
}

class StatMetricCollectorSourceTest extends AnyFunSuite with MockFactory {
  val procFileContent: String = new String(
    Files.readAllBytes(
      Paths
        .get(".")
        .toAbsolutePath
        .getParent()
        .resolve("src/test/scala/proc/stat")
    ),
    StandardCharsets.UTF_8
  )

  test("Method getProcFileSource should return specific BufferedSource") {
    val sourceMethod = mockFunction[String, BufferedSource]
    val metricCollector = new StatMetricCollector(sourceMethod)
    val sourceTest = (new BufferedSource(
      new ByteArrayInputStream(procFileContent.getBytes)
    ))
    sourceMethod
      .expects(metricCollector.procFilePath)
      .returns(sourceTest)

    assertResult(sourceTest) {
      metricCollector.getProcFileSource()
    }
  }

}
