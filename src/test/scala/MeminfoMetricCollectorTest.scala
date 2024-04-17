import io.github.dutrevis.MeminfoMetricCollector
import org.scalatest.funsuite.AnyFunSuite
import org.scalamock.scalatest.MockFactory
import org.mockito.{MockitoSugar, ArgumentMatchersSugar}
import org.mockito.integrations.scalatest.ResetMocksAfterEachTest
import org.mockito.captor.ArgCaptor
import scala.io.{Source, BufferedSource}

import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

class MeminfoMetricCollectorTest
    extends AnyFunSuite
    with MockitoSugar
    with ArgumentMatchersSugar
    with ResetMocksAfterEachTest {

  // Arrange common mocks
  val procFileSourceMock = mock[BufferedSource]

  val procFileContent: String = new String(
    Files.readAllBytes(
      Paths
        .get(".")
        .toAbsolutePath
        .getParent()
        .resolve("src/test/scala/meminfo.test")
    ),
    StandardCharsets.UTF_8
  )

  test("Method getMetricValue should return specific Long value") {
    val metricCollector = new MeminfoMetricCollector()
    val procFileSourceTest = (new BufferedSource(
      new ByteArrayInputStream(procFileContent.getBytes)
    ))
    val originalMetricName: String = "MemTotal"
    val expectedLongValue: Long = 1921988

    assertResult(expectedLongValue) {
      metricCollector.getMetricValue(procFileSourceTest, originalMetricName)
    }
  }

  test("Method getMetricValue should call BufferedSource.getLines") {
    val metricCollector = new MeminfoMetricCollector()
    val procFileSourceTest = (new BufferedSource(
      new ByteArrayInputStream(procFileContent.getBytes)
    ))
    val originalMetricName: String = "MemTotal"

    when(procFileSourceMock.getLines())
      .thenReturn(procFileSourceTest.getLines())

    metricCollector.getMetricValue(procFileSourceMock, originalMetricName)

    verify(procFileSourceMock, times(1)).getLines()
  }

  test("Method getMetricValue should throw NoSuchElementException") {
    val metricCollector = new MeminfoMetricCollector()
    val procFileSourceTest = (new BufferedSource(
      new ByteArrayInputStream(procFileContent.getBytes)
    ))
    val originalMetricName: String = "WrongMetric"

    when(procFileSourceMock.getLines())
      .thenReturn(procFileSourceTest.getLines())
    assertThrows[NoSuchElementException](
      metricCollector.getMetricValue(procFileSourceMock, originalMetricName)
    )
  }
}

class MeminfoMetricCollectorSourceTest extends AnyFunSuite with MockFactory {
  val procFileContent: String = new String(
    Files.readAllBytes(
      Paths
        .get(".")
        .toAbsolutePath
        .getParent()
        .resolve("src/test/scala/meminfo.test")
    ),
    StandardCharsets.UTF_8
  )

  test("Method getProcFileSource should return specific BufferedSource") {
    val sourceMethod = mockFunction[String, BufferedSource]
    val metricCollector = new MeminfoMetricCollector(sourceMethod)
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
