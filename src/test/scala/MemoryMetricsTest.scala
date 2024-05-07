import io.github.dutrevis.{MemoryMetrics, MeminfoMetricCollector}
import org.scalatest.funsuite.AnyFunSuite
import org.mockito.{MockitoSugar, ArgumentMatchersSugar}
import org.mockito.captor.ArgCaptor
import org.mockito.integrations.scalatest.ResetMocksAfterEachTest
import com.codahale.metrics.{Gauge, Metric, MetricRegistry}

class MemoryMetricsTest
    extends AnyFunSuite
    with MockitoSugar
    with ArgumentMatchersSugar
    with ResetMocksAfterEachTest {

  // Arrange common mocks
  val gaugeMock = mock[Gauge[Long]]
  val metricMock = mock[Metric]
  val metricRegistryMock = mock[MetricRegistry]
  val meminfoMetricCollectorMock = mock[MeminfoMetricCollector]

  // Arrange common values
  val procFileContentTest = new String

  test("Method createGaugeMetric should return Gauge[Long]") {
    val memMetrics = new MemoryMetrics
    val collectorMethod = (m: MeminfoMetricCollector) => { 123456.toLong }

    val returnedGaugeMetric =
      memMetrics.createGaugeMetric(meminfoMetricCollectorMock, collectorMethod)
    assert(returnedGaugeMetric.isInstanceOf[Gauge[Long]])
  }

  test("Method registerMetric should call register") {
    val memMetrics = new MemoryMetrics
    val metricName: String = "AnyMetric"

    when(metricRegistryMock.register(any[String], any[Metric]))
      .thenReturn(metricMock)
    memMetrics.registerMetric(metricRegistryMock, metricName, gaugeMock)

    verify(metricRegistryMock, times(1)).register(metricName, gaugeMock)
  }

  test("Method collectTotalMemory should call getMetricValue") {
    val originalMetricName: String = "MemTotal"
    val memMetrics = new MemoryMetrics

    when(meminfoMetricCollectorMock.getProcFileContent())
      .thenReturn(procFileContentTest)
    memMetrics.collectTotalMemory(meminfoMetricCollectorMock)
    verify(meminfoMetricCollectorMock, times(1))
      .getMetricValue(procFileContentTest, originalMetricName)
  }

  test("Method collectTotalMemory should call getMetricValue with args") {
    val memMetrics = new MemoryMetrics
    val originalMetricName: String = "MemTotal"
    val procFileContentCaptor = ArgCaptor[String]
    val originalMetricNameCaptor = ArgCaptor[String]

    when(meminfoMetricCollectorMock.getProcFileContent())
      .thenReturn(procFileContentTest)
    memMetrics.collectTotalMemory(meminfoMetricCollectorMock)
    verify(meminfoMetricCollectorMock).getMetricValue(
      procFileContentCaptor,
      originalMetricNameCaptor
    )
    procFileContentCaptor hasCaptured procFileContentTest
    originalMetricNameCaptor hasCaptured originalMetricName
  }

  test("Method collectTotalMemory should return Long") {
    val originalMetricName: String = "MemTotal"
    val expectedLongValue: Long = 1921988
    val memMetrics = new MemoryMetrics

    when(meminfoMetricCollectorMock.getProcFileContent())
      .thenReturn(procFileContentTest)
    when(
      meminfoMetricCollectorMock.getMetricValue(
        procFileContentTest,
        originalMetricName
      )
    )
      .thenReturn(expectedLongValue)

    assertResult(expectedLongValue) {
      memMetrics.collectTotalMemory(meminfoMetricCollectorMock)
    }
    assert(
      memMetrics
        .collectTotalMemory(meminfoMetricCollectorMock)
        .isInstanceOf[Long]
    )
  }

  test("Method collectFreeMemory should return Long") {
    val originalMetricName: String = "MemFree"
    val expectedLongValue: Long = 1374408
    val memMetrics = new MemoryMetrics

    when(meminfoMetricCollectorMock.getProcFileContent())
      .thenReturn(procFileContentTest)
    when(
      meminfoMetricCollectorMock.getMetricValue(
        procFileContentTest,
        originalMetricName
      )
    )
      .thenReturn(expectedLongValue)

    assertResult(expectedLongValue) {
      memMetrics.collectFreeMemory(meminfoMetricCollectorMock)
    }
    assert(
      memMetrics
        .collectFreeMemory(meminfoMetricCollectorMock)
        .isInstanceOf[Long]
    )
  }

  test("Method collectUsedMemory should return Long") {
    val originalMetricNameMemTotal: String = "MemTotal"
    val originalMetricNameMemFree: String = "MemFree"
    val expectecMemTotalValue: Long = 1921988
    val expectecMemFreeValue: Long = 1374408
    val memMetrics = new MemoryMetrics

    when(meminfoMetricCollectorMock.getProcFileContent())
      .thenReturn(procFileContentTest)
    when(
      meminfoMetricCollectorMock.getMetricValue(
        procFileContentTest,
        originalMetricNameMemTotal
      )
    )
      .thenReturn(expectecMemTotalValue)
    when(
      meminfoMetricCollectorMock.getMetricValue(
        procFileContentTest,
        originalMetricNameMemFree
      )
    )
      .thenReturn(expectecMemFreeValue)

    assertResult(expectecMemTotalValue - expectecMemFreeValue) {
      memMetrics.collectUsedMemory(meminfoMetricCollectorMock)
    }
    assert(
      memMetrics
        .collectUsedSwapMemory(meminfoMetricCollectorMock)
        .isInstanceOf[Long]
    )
  }

  test("Method collectSharedMemory should return Long") {
    val originalMetricName: String = "Shmem"
    val expectedLongValue: Long = 196
    val memMetrics = new MemoryMetrics

    when(meminfoMetricCollectorMock.getProcFileContent())
      .thenReturn(procFileContentTest)
    when(
      meminfoMetricCollectorMock.getMetricValue(
        procFileContentTest,
        originalMetricName
      )
    )
      .thenReturn(expectedLongValue)

    assertResult(expectedLongValue) {
      memMetrics.collectSharedMemory(meminfoMetricCollectorMock)
    }
    assert(
      memMetrics
        .collectSharedMemory(meminfoMetricCollectorMock)
        .isInstanceOf[Long]
    )
  }

  test("Method collectBufferMemory should return Long") {
    val originalMetricName: String = "Buffers"
    val expectedLongValue: Long = 32688
    val memMetrics = new MemoryMetrics

    when(meminfoMetricCollectorMock.getProcFileContent())
      .thenReturn(procFileContentTest)
    when(
      meminfoMetricCollectorMock.getMetricValue(
        procFileContentTest,
        originalMetricName
      )
    )
      .thenReturn(expectedLongValue)

    assertResult(expectedLongValue) {
      memMetrics.collectBufferMemory(meminfoMetricCollectorMock)
    }
    assert(
      memMetrics
        .collectBufferMemory(meminfoMetricCollectorMock)
        .isInstanceOf[Long]
    )
  }

  test("Method collectTotalSwapMemory should return Long") {
    val originalMetricName: String = "SwapTotal"
    val expectedLongValue: Long = 1048572
    val memMetrics = new MemoryMetrics

    when(meminfoMetricCollectorMock.getProcFileContent())
      .thenReturn(procFileContentTest)
    when(
      meminfoMetricCollectorMock.getMetricValue(
        procFileContentTest,
        originalMetricName
      )
    )
      .thenReturn(expectedLongValue)

    assertResult(expectedLongValue) {
      memMetrics.collectTotalSwapMemory(meminfoMetricCollectorMock)
    }
    assert(
      memMetrics
        .collectTotalSwapMemory(meminfoMetricCollectorMock)
        .isInstanceOf[Long]
    )
  }

  test("Method collectFreeSwapMemory should return Long") {
    val originalMetricName: String = "SwapFree"
    val expectedLongValue: Long = 1048572
    val memMetrics = new MemoryMetrics

    when(meminfoMetricCollectorMock.getProcFileContent())
      .thenReturn(procFileContentTest)
    when(
      meminfoMetricCollectorMock.getMetricValue(
        procFileContentTest,
        originalMetricName
      )
    )
      .thenReturn(expectedLongValue)

    assert(
      memMetrics
        .collectFreeSwapMemory(meminfoMetricCollectorMock)
        .isInstanceOf[Long]
    )
  }

  test("Method collectCachedSwapMemory should return Long") {
    val originalMetricName: String = "SwapCached"
    val expectedLongValue: Long = 0
    val memMetrics = new MemoryMetrics

    when(meminfoMetricCollectorMock.getProcFileContent())
      .thenReturn(procFileContentTest)
    when(
      meminfoMetricCollectorMock.getMetricValue(
        procFileContentTest,
        originalMetricName
      )
    )
      .thenReturn(expectedLongValue)

    assert(
      memMetrics
        .collectCachedSwapMemory(meminfoMetricCollectorMock)
        .isInstanceOf[Long]
    )
  }

  test("Method collectUsedSwapMemory should return Long") {
    val originalMetricNameSwapTotal: String = "SwapTotal"
    val originalMetricNameSwapFree: String = "SwapFree"
    val expectecSwapTotalValue: Long = 1048572
    val expectecSwapFreeValue: Long = 1048572
    val memMetrics = new MemoryMetrics

    when(meminfoMetricCollectorMock.getProcFileContent())
      .thenReturn(procFileContentTest)
    when(
      meminfoMetricCollectorMock.getMetricValue(
        procFileContentTest,
        originalMetricNameSwapTotal
      )
    )
      .thenReturn(expectecSwapTotalValue)
    when(
      meminfoMetricCollectorMock.getMetricValue(
        procFileContentTest,
        originalMetricNameSwapFree
      )
    )
      .thenReturn(expectecSwapFreeValue)

    assertResult(expectecSwapTotalValue - expectecSwapFreeValue) {
      memMetrics.collectUsedSwapMemory(meminfoMetricCollectorMock)
    }
    assert(
      memMetrics
        .collectUsedSwapMemory(meminfoMetricCollectorMock)
        .isInstanceOf[Long]
    )
  }
}
