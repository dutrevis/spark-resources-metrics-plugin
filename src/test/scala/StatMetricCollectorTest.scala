import io.github.dutrevis.StatMetricCollector
import org.scalatest.funsuite.AnyFunSuite
import org.mockito.{MockitoSugar, ArgumentMatchersSugar}
import org.mockito.integrations.scalatest.ResetMocksAfterEachTest
import org.mockito.captor.ArgCaptor

import java.nio.charset.{StandardCharsets, Charset}
import java.nio.file.{Files, Path}

class StatMetricCollectorTest
    extends AnyFunSuite
    with MockitoSugar
    with ArgumentMatchersSugar
    with ResetMocksAfterEachTest {

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

  val procFileContentTest: String = new String(
    Files.readAllBytes(
      Path
        .of(".")
        .toAbsolutePath
        .getParent()
        .resolve("src/test/scala/proc/stat")
    ),
    StandardCharsets.UTF_8
  )

  test("Method getMetricValue should return specific Double value") {
    val metricCollector = new StatMetricCollector
    val originalMetricName: String = "cpu_user"
    val expectedDoubleValue: Double =
      procFileDataTest(originalMetricName) / procFileDataTest.foldLeft(0.0)(
        _ + _._2
      )

    assertResult(expectedDoubleValue) {
      metricCollector.getMetricValue(procFileContentTest, originalMetricName)
    }
  }

  test("Method getMetricValue should throw NoSuchElementException") {
    val metricCollector = new StatMetricCollector
    val originalMetricName: String = "wrong_metric"

    assertThrows[NoSuchElementException](
      metricCollector.getMetricValue(procFileContentTest, originalMetricName)
    )
  }

}
