import io.github.dutrevis.MemoryMetrics
import com.codahale.metrics.MetricRegistry
import org.scalatest.funsuite.AnyFunSuite

class MemoryMetricsTest extends AnyFunSuite {
  val memMetrics = new MemoryMetrics()
  val metricReg = new MetricRegistry()

  test("Method registerTotalMemory should return Unit") {
    assert(
      memMetrics
        .registerTotalMemory(metricReg)
        .isInstanceOf[Unit]
    )
  }

  test("Method registerFreeMemory should return Unit") {
    assert(
      memMetrics
        .registerFreeMemory(metricReg)
        .isInstanceOf[Unit]
    )
  }

  test("Method registerUsedMemory should return Unit") {
    assert(
      memMetrics
        .registerUsedMemory(metricReg)
        .isInstanceOf[Unit]
    )
  }

  test("Method registerSharedMemory should return Unit") {
    assert(
      memMetrics
        .registerSharedMemory(metricReg)
        .isInstanceOf[Unit]
    )
  }

  test("Method registerBufferMemory should return Unit") {
    assert(
      memMetrics
        .registerBufferMemory(metricReg)
        .isInstanceOf[Unit]
    )
  }

  test("Method registerTotalSwapMemory should return Unit") {
    assert(
      memMetrics
        .registerTotalSwapMemory(metricReg)
        .isInstanceOf[Unit]
    )
  }

  test("Method registerFreeSwapMemory should return Unit") {
    assert(
      memMetrics
        .registerFreeSwapMemory(metricReg)
        .isInstanceOf[Unit]
    )
  }

  test("Method registerCachedSwapMemory should return Unit") {
    assert(
      memMetrics
        .registerCachedSwapMemory(metricReg)
        .isInstanceOf[Unit]
    )
  }
}
