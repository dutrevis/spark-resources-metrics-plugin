import io.github.dutrevis.MeminfoMetricCollector
import org.scalatest.funsuite.AnyFunSuite
import org.mockito.{MockitoSugar, ArgumentMatchersSugar}
import org.mockito.integrations.scalatest.ResetMocksAfterEachTest
import org.mockito.captor.ArgCaptor

import java.nio.charset.{StandardCharsets, Charset}
import java.nio.file.{Files, Path}

class MeminfoMetricCollectorTest
    extends AnyFunSuite
    with MockitoSugar
    with ArgumentMatchersSugar
    with ResetMocksAfterEachTest {

  // Arrange common values
  val procFileContentTest: String = new String(
    Files.readAllBytes(
      Path
        .of(".")
        .toAbsolutePath
        .getParent()
        .resolve("src/test/scala/proc/meminfo")
    ),
    StandardCharsets.UTF_8
  )

  test("Method getMetricValue should return specific Long value") {
    val metricCollector = new MeminfoMetricCollector()
    val originalMetricName: String = "MemTotal"
    val expectedLongValue: Long = 1921988

    assertResult(expectedLongValue) {
      metricCollector.getMetricValue(procFileContentTest, originalMetricName)
    }
  }

  test("Method getMetricValue should throw NoSuchElementException") {
    val metricCollector = new MeminfoMetricCollector()
    val originalMetricName: String = "WrongMetric"

    assertThrows[NoSuchElementException](
      metricCollector.getMetricValue(procFileContentTest, originalMetricName)
    )
  }
}
