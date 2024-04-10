package io.github.dutrevis

import java.util.{Map => JMap}
import scala.collection.JavaConverters._

import com.codahale.metrics.{Gauge, MetricRegistry}
import org.apache.spark.SparkContext
import org.apache.spark.api.plugin.{
  DriverPlugin,
  ExecutorPlugin,
  PluginContext,
  SparkPlugin
}

import scala.io.Source

/** Collects memory resource metrics from a unix-based operating system. <p> Use
  * when Spark is running in clusters with standalone, Mesos or YARN resource
  * managers. <p> Memory metrics are obtained from the numbers of each line of
  * the `/proc/meminfo` file, available at the proc pseudo-filesystem of
  * unix-based operating systems. The file has statistics about memory usage on
  * the system, arranged in lines consisted of a parameter name, followed by a
  * colon, the value of the parameter, and an option unit of measurement. <p>
  * @note
  *   While the `/proc/meminfo` file shows kilobytes (kB; 1 kB equals 1000 B),
  *   its unit is actually kibibytes (KiB; 1 KiB equals 1024 B). This
  *   imprecision is known, but is not corrected due to legacy concerns.
  * @note
  *   Many fields have been present since at least Linux 2.6.0, but most of the
  *   other fields are available at specific Linux versions (as noted in each
  *   method docstring) or are displayed only if the kernel was configured with
  *   specific options. Their parsing requires caution when they are
  *   implemented.
  */
class MemoryMetrics extends ProcFileMetricCollector with SparkPlugin {

  override protected val procFilePath = "/proc/meminfo"
  val metricMapping = Map[String, () => Long](
    "TotalMemory" -> collectTotalMemory,
    "FreeMemory" -> collectFreeMemory,
    "UsedMemory" -> collectUsedMemory,
    "SharedMemory" -> collectSharedMemory,
    "BufferMemory" -> collectBufferMemory,
    "TotalSwapMemory" -> collectTotalSwapMemory,
    "FreeSwapMemory" -> collectFreeSwapMemory,
    "CachedSwapMemory" -> collectCachedSwapMemory,
    "UsedSwapMemory" -> collectUsedSwapMemory
  )

  /** Gets the value of a metric from a proc file located at the `procFilePath`
    * value set. The metric is searched according to the original name provided
    * in the parameter `originalMetricName`. <p>
    * @param originalMetricName
    *   the original metric name by which it is found in the proc file <p>
    * @throws MatchError
    *   if a metric is not found with the provided original name
    */
  override protected def getMetricValue(originalMetricName: String): Long = {
    val procFile = getProcFileSource()
    val procFileData = procFile.getLines
      .filter(metricLine => metricLine.contains(originalMetricName))
      .map { case s: String => s.split(":") }
      .map { case Array(k, v) => k -> v.trim().split(" ").head.toLong }
      .toMap
    val metricValue = procFileData(originalMetricName)
    procFile.close()
    metricValue
  }

  /** Registers an existing metric into a metric registry under a metric name as
    * a Dropwizard's Gauge metric type - an instantaneous reading of a
    * particular value -, by setting the provided collector method as the
    * `getValue` method of the Gauge instance. To avoid future reading errors
    * when the Gauge is executed by the metrics system, the collector method is
    * called once before its registration, assuring that the metric is read with
    * no matching errors. If a `MatchError` is thrown, the method is simply not
    * registered, avoiding the blockage of future registrations by the plugin.
    * <p>
    * @param metricRegistry
    *   a MetricRegistry instance from dropwizard.metrics <p>
    * @param metricName
    *   a metric name as a String <p>
    * @param collectorMethod
    *   a method without arguments that returns a metric value as a Long <p>
    * @throws IllegalArgumentException
    *   if the metric name is already registered
    */
  protected def registerGaugeMetric(
      metricRegistry: MetricRegistry,
      metricName: String,
      collectorMethod: () => Long
  ): Unit = {
    try {
      val testCall = collectorMethod()
      metricRegistry.register(
        MetricRegistry.name(metricName),
        new Gauge[Long] {
          override def getValue: Long = collectorMethod()
        }
      )
    } catch {
      case e: MatchError => Unit
    }
  }

  /** Collects the `MemTotal` parameter, which is the total usable RAM (i.e.,
    * physical RAM minus a few reserved bits and the kernel binary code). <p>
    * @note
    *   Registered value is in kibibytes and is of type LONG.
    */
  def collectTotalMemory(): Long = {
    val originalMetricName: String = "MemTotal"
    getMetricValue(originalMetricName)
  }

  /** Collects the `MemFree` parameter, which is the amount of physical RAM left
    * unused by the system. <p>
    * @note
    *   Registered value is in kibibytes and is of type LONG.
    */
  def collectFreeMemory(): Long = {
    val originalMetricName: String = "MemFree"
    getMetricValue(originalMetricName)
  }

  /** Calculates the total used memory, by collecting the `MemTotal` parameter -
    * the total usable RAM (i.e., physical RAM minus a few reserved bits and the
    * kernel binary code) - and the `MemFree` parameter - the amount of physical
    * RAM left unused by the system -, returning the subtraction of the value of
    * the latter out of the first. <p>
    * @note
    *   Registered value is in kibibytes and is of type LONG.
    */
  def collectUsedMemory(): Long = {
    val memTotal: Long = getMetricValue("MemTotal")
    val memFree: Long = getMetricValue("MemFree")
    memTotal - memFree
  }

  /** Collects the `Shmem` parameter - the amount of memory located either for
    * processes with separate address spaces that are sharing a portion of
    * memory, or for tmpfs(5) filesystems, whose contents reside in virtual
    * memory to speed up file access. <p>
    * @note
    *   Available since Linux 2.6.32
    * @note
    *   Registered value is in kibibytes and is of type LONG.
    */
  def collectSharedMemory(): Long = {
    val originalMetricName: String = "Shmem"
    getMetricValue(originalMetricName)
  }

  /** Collects the `Buffers` parameter, which is the amount of memory that is
    * used as a relatively temporary storage for raw disk blocks. <p>
    * @note
    *   This value shouldn't get tremendously large (20 MB or so).
    * @note
    *   Registered value is in kibibytes and is of type LONG.
    */
  def collectBufferMemory(): Long = {
    val originalMetricName: String = "Buffers"
    getMetricValue(originalMetricName)
  }

  /** Collects the `SwapTotal` parameter, which is the total amount of swap
    * space available on the disk - when the physical memory is full, the system
    * uses the swap space. <p>
    * @note
    *   Registered value is in kibibytes and is of type LONG.
    */
  def collectTotalSwapMemory(): Long = {
    val originalMetricName: String = "SwapTotal"
    getMetricValue(originalMetricName)
  }

  /** Collects the `SwapFree` parameter, which is the amount of swap space
    * currently unused - the memory that has been transferred from RAM to the
    * disk temporarily. <p>
    * @note
    *   Registered value is in kibibytes and is of type LONG.
    */
  def collectFreeSwapMemory(): Long = {
    val originalMetricName: String = "SwapFree"
    getMetricValue(originalMetricName)
  }

  /** Collects the `SwapCached` parameter, which is the amount of swap space
    * used as cache memory - memory that has once been moved into swap, then
    * back into the main memory, but still also remains in the swapfile. <p>
    * @note
    *   Useful memory to save I/O, as it does not need to be moved into swap
    *   again.
    * @note
    *   Registered value is in kibibytes and is of type LONG.
    */
  def collectCachedSwapMemory(): Long = {
    val originalMetricName: String = "SwapCached"
    getMetricValue(originalMetricName)
  }

  /** Calculates the used swap memory by collecting the `SwapTotal` parameter -
    * the total amount of swap space available on the disk - and the `SwapFree`
    * parameter - the total amount of swap space currently unused -, returning
    * the subtraction of the value of the latter out of the first. <p>
    * @note
    *   Registered value is in kibibytes and is of type LONG.
    */
  def collectUsedSwapMemory(): Long = {
    val swapTotal: Long = getMetricValue("SwapTotal")
    val swapFree: Long = getMetricValue("SwapFree")
    swapTotal - swapFree
  }

  /** Returns the plugin's driver-side component. The returned DriverPlugin
    * instance is called once early in the initialization of the Spark driver,
    * blocking the driver's initialization during its operations, consisting of
    * sequentially register each mapped metric as a Gauge metric into an
    * existing metric Registry. The plugin component ends its execution once all
    * metrics are registered, leaving to the Spark Metrics system the job of
    * collecting and exporting the registered metrics in a pre-configured
    * frequency. <p>
    * @return
    *   An empty Map, provided as `extraConf` to an `ExecutorPlugin` instance.
    */
  override def driverPlugin(): DriverPlugin = {
    new DriverPlugin() {
      override def init(
          sc: SparkContext,
          myContext: PluginContext
      ): JMap[String, String] = {
        for (
          (metricName: String, collectorMethod: (() => Long)) <-
            metricMapping
        )
          registerGaugeMetric(
            myContext.metricRegistry,
            metricName,
            collectorMethod
          )
        Map.empty[String, String].asJava
      }
    }
  }

  /** Returns the plugin's executor-side component. The returned ExecutorPlugin
    * instance is called once early in the initialization of the executor
    * process, blocking the executor's initialization during its operations,
    * consisting of sequentially register each mapped metric as a Gauge metric
    * into an existing metric Registry. The plugin component ends its execution
    * once all metrics are registered, leaving to the Spark Metrics system the
    * job of collecting and exporting the registered metrics in a pre-configured
    * frequency. <p>
    */
  override def executorPlugin(): ExecutorPlugin = {
    new ExecutorPlugin() {
      override def init(
          myContext: PluginContext,
          extraConf: JMap[String, String]
      ): Unit = {
        for (
          (metricName: String, collectorMethod: (() => Long)) <-
            metricMapping
        )
          registerGaugeMetric(
            myContext.metricRegistry,
            metricName,
            collectorMethod
          )
      }
    }
  }
}
