import io.github.dutrevis.{CPUMetrics, StatMetricCollector}

import com.codahale.metrics.{Gauge, Metric, MetricRegistry}

import org.mockito.captor.ArgCaptor
import org.mockito.integrations.scalatest.ResetMocksAfterEachTest
import org.mockito.{MockitoSugar, ArgumentMatchersSugar}
import org.scalatest.funsuite.AnyFunSuite

class CPUMetricsTest
    extends AnyFunSuite
    with MockitoSugar
    with ArgumentMatchersSugar
    with ResetMocksAfterEachTest {

  // Arrange common mocks
  val gaugeMock = mock[Gauge[Double]]
  val metricMock = mock[Metric]
  val metricRegistryMock = mock[MetricRegistry]
  val statMetricCollectorMock = mock[StatMetricCollector]

  // Arrange common values
  val procFileContentTest = new String

  test("Method createGaugeMetric should return Gauge[Double]") {
    val cpuMetrics = new CPUMetrics
    val collectorMethod = (s: StatMetricCollector) => { 123456.toDouble }

    val returnedGaugeMetric =
      cpuMetrics.createGaugeMetric(statMetricCollectorMock, collectorMethod)
    assert(returnedGaugeMetric.isInstanceOf[Gauge[Double]])
  }

  test("Method registerMetric should call register") {
    val cpuMetrics = new CPUMetrics
    val metricName: String = "any_metric"

    when(metricRegistryMock.register(any[String], any[Metric]))
      .thenReturn(metricMock)
    cpuMetrics.registerMetric(metricRegistryMock, metricName, gaugeMock)

    verify(metricRegistryMock, times(1)).register(metricName, gaugeMock)
  }

  test("Method collectUserCPU should call getMetricValue") {
    val originalMetricName: String = "cpu_user"
    val cpuMetrics = new CPUMetrics

    when(statMetricCollectorMock.getProcFileContent())
      .thenReturn(procFileContentTest)
    cpuMetrics.collectUserCPU(statMetricCollectorMock)
    verify(statMetricCollectorMock, times(1))
      .getMetricValue(procFileContentTest, originalMetricName)
  }

  test("Method collectUserCPU should call getMetricValue with args") {
    val cpuMetrics = new CPUMetrics
    val originalMetricName: String = "cpu_user"
    val procFileContentCaptor = ArgCaptor[String]
    val originalMetricNameCaptor = ArgCaptor[String]

    when(statMetricCollectorMock.getProcFileContent())
      .thenReturn(procFileContentTest)
    cpuMetrics.collectUserCPU(statMetricCollectorMock)
    verify(statMetricCollectorMock).getMetricValue(
      procFileContentCaptor,
      originalMetricNameCaptor
    )
    procFileContentCaptor hasCaptured procFileContentTest
    originalMetricNameCaptor hasCaptured originalMetricName
  }

  test("Method collectUserCPU should return Double") {
    val originalMetricName: String = "cpu_user"
    val expectedDoubleValue: Double = 79242
    val cpuMetrics = new CPUMetrics

    when(statMetricCollectorMock.getProcFileContent())
      .thenReturn(procFileContentTest)
    when(
      statMetricCollectorMock.getMetricValue(
        procFileContentTest,
        originalMetricName
      )
    )
      .thenReturn(expectedDoubleValue)

    assertResult(expectedDoubleValue) {
      cpuMetrics.collectUserCPU(statMetricCollectorMock)
    }
  }

  test("Method collectNiceCPU should return Double") {
    val originalMetricName: String = "cpu_nice"
    val expectedDoubleValue: Double = 0
    val cpuMetrics = new CPUMetrics

    when(statMetricCollectorMock.getProcFileContent())
      .thenReturn(procFileContentTest)
    when(
      statMetricCollectorMock.getMetricValue(
        procFileContentTest,
        originalMetricName
      )
    )
      .thenReturn(expectedDoubleValue)

    assertResult(expectedDoubleValue) {
      cpuMetrics.collectNiceCPU(statMetricCollectorMock)
    }
  }

  test("Method collectSystemCPU should return Double") {
    val originalMetricName: String = "cpu_system"
    val expectedDoubleValue: Double = 74306
    val cpuMetrics = new CPUMetrics

    when(statMetricCollectorMock.getProcFileContent())
      .thenReturn(procFileContentTest)
    when(
      statMetricCollectorMock.getMetricValue(
        procFileContentTest,
        originalMetricName
      )
    )
      .thenReturn(expectedDoubleValue)

    assertResult(expectedDoubleValue) {
      cpuMetrics.collectSystemCPU(statMetricCollectorMock)
    }
  }

  test("Method collectIdleCPU should return Double") {
    val originalMetricName: String = "cpu_idle"
    val expectedDoubleValue: Double = 842486413
    val cpuMetrics = new CPUMetrics

    when(statMetricCollectorMock.getProcFileContent())
      .thenReturn(procFileContentTest)
    when(
      statMetricCollectorMock.getMetricValue(
        procFileContentTest,
        originalMetricName
      )
    )
      .thenReturn(expectedDoubleValue)

    assertResult(expectedDoubleValue) {
      cpuMetrics.collectIdleCPU(statMetricCollectorMock)
    }
  }

  test("Method collectWaitCPU should return Double") {
    val originalMetricName: String = "cpu_iowait"
    val expectedDoubleValue: Double = 756859
    val cpuMetrics = new CPUMetrics

    when(statMetricCollectorMock.getProcFileContent())
      .thenReturn(procFileContentTest)
    when(
      statMetricCollectorMock.getMetricValue(
        procFileContentTest,
        originalMetricName
      )
    )
      .thenReturn(expectedDoubleValue)

    assertResult(expectedDoubleValue) {
      cpuMetrics.collectWaitCPU(statMetricCollectorMock)
    }
  }
}
