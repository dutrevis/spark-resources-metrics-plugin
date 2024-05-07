package io.github.dutrevis

import java.util.{Map => JMap}
import scala.collection.JavaConverters._

import com.codahale.metrics.{Gauge, Metric, MetricRegistry}
import org.apache.spark.SparkContext
import org.apache.spark.api.plugin.{
  DriverPlugin,
  ExecutorPlugin,
  PluginContext,
  SparkPlugin
}

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
  *   specific options. If these fields are not found, the metrics won't be
  *   registered onto Dropwizard's metric system.
  */
class MemoryMetrics extends SparkPlugin {

  /** Maps the collector methods to their respective metric names, that will be
    * displayed in the Dropwizard's metric system.
    */
  val metricMapping = Map[String, (MeminfoMetricCollector) => Long](
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

  /** Registers a provided Dropwizard's Metric instance into a metric registry
    * under a metric name. <p>
    * @param metricRegistry
    *   a MetricRegistry instance from dropwizard.metrics <p>
    * @param metricName
    *   a metric name as a String <p>
    * @param metricInstance
    *   an instance of a dropwizard's Metric class to be registered <p>
    * @throws IllegalArgumentException
    *   if the metric name is already registered
    */
  def registerMetric(
      metricRegistry: MetricRegistry,
      metricName: String,
      metricInstance: Metric
  ): Unit = {
    metricRegistry.register(metricName, metricInstance)
    ()
  }

  /** Creates a Dropwizard's Gauge metric type - an instantaneous reading of a
    * particular value -, setting the provided collector method as the
    * `getValue` method of the Gauge instance. <p>
    * @param metricCollector
    *   a MetricRegistry instance from dropwizard.metrics <p>
    * @param collectorMethod
    *   a method that receives the metricCollector and returns a metric value as
    *   a Long <p>
    */
  def createGaugeMetric(
      metricCollector: MeminfoMetricCollector,
      collectorMethod: (MeminfoMetricCollector) => Long
  ): Gauge[Long] = {
    new Gauge[Long] {
      override def getValue: Long = { collectorMethod(metricCollector) }
    }
  }

  /** Collects the `MemTotal` parameter, which is the total usable RAM (i.e.,
    * physical RAM minus a few reserved bits and the kernel binary code). <p>
    * @note
    *   Registered value is in kibibytes and is of type LONG.
    */
  def collectTotalMemory(metricCollector: MeminfoMetricCollector): Long = {
    val procFileContent = metricCollector.getProcFileContent()
    val originalMetricName: String = "MemTotal"
    metricCollector.getMetricValue(procFileContent, originalMetricName)
  }

  /** Collects the `MemFree` parameter, which is the amount of physical RAM left
    * unused by the system. <p>
    * @note
    *   Registered value is in kibibytes and is of type LONG.
    */
  def collectFreeMemory(metricCollector: MeminfoMetricCollector): Long = {
    val procFileContent = metricCollector.getProcFileContent()
    val originalMetricName: String = "MemFree"
    metricCollector.getMetricValue(procFileContent, originalMetricName)
  }

  /** Calculates the total used memory, by collecting the `MemTotal` parameter -
    * the total usable RAM (i.e., physical RAM minus a few reserved bits and the
    * kernel binary code) - and the `MemFree` parameter - the amount of physical
    * RAM left unused by the system -, returning the subtraction of the value of
    * the latter out of the first. <p>
    * @note
    *   Registered value is in kibibytes and is of type LONG.
    */
  def collectUsedMemory(metricCollector: MeminfoMetricCollector): Long = {
    val procFileContent = metricCollector.getProcFileContent()
    val memTotal: Long =
      metricCollector.getMetricValue(procFileContent, "MemTotal")
    val memFree: Long =
      metricCollector.getMetricValue(procFileContent, "MemFree")
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
  def collectSharedMemory(metricCollector: MeminfoMetricCollector): Long = {
    val procFileContent = metricCollector.getProcFileContent()
    val originalMetricName: String = "Shmem"
    metricCollector.getMetricValue(procFileContent, originalMetricName)
  }

  /** Collects the `Buffers` parameter, which is the amount of memory that is
    * used as a relatively temporary storage for raw disk blocks. <p>
    * @note
    *   This value shouldn't get tremendously large (20 MB or so).
    * @note
    *   Registered value is in kibibytes and is of type LONG.
    */
  def collectBufferMemory(metricCollector: MeminfoMetricCollector): Long = {
    val procFileContent = metricCollector.getProcFileContent()
    val originalMetricName: String = "Buffers"
    metricCollector.getMetricValue(procFileContent, originalMetricName)
  }

  /** Collects the `SwapTotal` parameter, which is the total amount of swap
    * space available on the disk - when the physical memory is full, the system
    * uses the swap space. <p>
    * @note
    *   Registered value is in kibibytes and is of type LONG.
    */
  def collectTotalSwapMemory(metricCollector: MeminfoMetricCollector): Long = {
    val procFileContent = metricCollector.getProcFileContent()
    val originalMetricName: String = "SwapTotal"
    metricCollector.getMetricValue(procFileContent, originalMetricName)
  }

  /** Collects the `SwapFree` parameter, which is the amount of swap space
    * currently unused - the memory that has been transferred from RAM to the
    * disk temporarily. <p>
    * @note
    *   Registered value is in kibibytes and is of type LONG.
    */
  def collectFreeSwapMemory(metricCollector: MeminfoMetricCollector): Long = {
    val procFileContent = metricCollector.getProcFileContent()
    val originalMetricName: String = "SwapFree"
    metricCollector.getMetricValue(procFileContent, originalMetricName)
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
  def collectCachedSwapMemory(metricCollector: MeminfoMetricCollector): Long = {
    val procFileContent = metricCollector.getProcFileContent()
    val originalMetricName: String = "SwapCached"
    metricCollector.getMetricValue(procFileContent, originalMetricName)
  }

  /** Calculates the used swap memory by collecting the `SwapTotal` parameter -
    * the total amount of swap space available on the disk - and the `SwapFree`
    * parameter - the total amount of swap space currently unused -, returning
    * the subtraction of the value of the latter out of the first. <p>
    * @note
    *   Registered value is in kibibytes and is of type LONG.
    */
  def collectUsedSwapMemory(metricCollector: MeminfoMetricCollector): Long = {
    val procFileContent = metricCollector.getProcFileContent()
    val swapTotal: Long =
      metricCollector.getMetricValue(procFileContent, "SwapTotal")
    val swapFree: Long =
      metricCollector.getMetricValue(procFileContent, "SwapFree")
    swapTotal - swapFree
  }

  /** Returns the plugin's driver-side component. The returned DriverPlugin
    * instance is called once, early in the initialization of the Spark driver.
    * The operation it performs consists in the sequential registration of each
    * mapped metric as a Gauge metric into an existing metric Registry. A test
    * call is executed once on each collector method before its registration,
    * assuring that the metric is available to be read and collected from the
    * local OS, thus preventing future errors when the Gauge is first executed
    * by the metrics system. If a `NoSuchElementException` is thrown in this
    * attempt, the method is not registered, enabling the registration of the
    * subsequent mapped metrics by the plugin. The plugin component ends its
    * execution once all metrics are registered, leaving to the Dropwizard's
    * Metrics system the job of collecting and exporting the registered metrics
    * in a pre or user-defined frequency. <p>
    * @note
    *   The driver's initialization is blocked during the operations inside
    *   `init`, so heavy performing operations must be avoided.
    * @note
    *   The overriden `init` method must return a Map, that will be provided as
    *   `extraConf` to an `ExecutorPlugin` instance.
    * @return
    *   An instance of the `DriverPlugin`
    */
  override def driverPlugin(): DriverPlugin = {
    new DriverPlugin() {
      override def init(
          sc: SparkContext,
          myContext: PluginContext
      ): JMap[String, String] = {
        val metricCollector = new MeminfoMetricCollector
        for (
          (
            metricName: String,
            collectorMethod: ((MeminfoMetricCollector) => Long)
          ) <-
            metricMapping
        )
          try {
            var testCall = collectorMethod(metricCollector)
            registerMetric(
              myContext.metricRegistry,
              MetricRegistry.name(metricName),
              createGaugeMetric(metricCollector, collectorMethod)
            )
          } catch {
            case e: NoSuchElementException => ()
          }
        Map.empty[String, String].asJava
      }
    }
  }

  /** Returns the plugin's executor-side component. The returned ExecutorPlugin
    * instance is called once, early in the initialization of the executor
    * process. The operation it performs consists in the sequential registration
    * of each mapped metric as a Gauge metric into an existing metric Registry.
    * A test call is executed once on each collector method before its
    * registration, assuring that the metric is available to be read and
    * collected from the local OS, thus preventing future errors when the Gauge
    * is first executed by the metrics system. If a `NoSuchElementException` is
    * thrown in this attempt, the method is not registered, enabling the
    * registration of the subsequent mapped metrics by the plugin. The plugin
    * component ends its execution once all metrics are registered, leaving to
    * the Dropwizard's Metrics system the job of collecting and exporting the
    * registered metrics in a pre or user-defined frequency. <p>
    * @note
    *   The executor's initialization is blocked during the operations inside
    *   `init`, so heavy performing operations must be avoided.
    * @return
    *   An instance of the `ExecutorPlugin` Unit
    */
  override def executorPlugin(): ExecutorPlugin = {
    new ExecutorPlugin() {
      override def init(
          myContext: PluginContext,
          extraConf: JMap[String, String]
      ): Unit = {
        val metricCollector = new MeminfoMetricCollector
        for (
          (
            metricName: String,
            collectorMethod: ((MeminfoMetricCollector) => Long)
          ) <-
            metricMapping
        )
          try {
            var testCall = collectorMethod(metricCollector)
            registerMetric(
              myContext.metricRegistry,
              MetricRegistry.name(metricName),
              createGaugeMetric(metricCollector, collectorMethod)
            )
          } catch {
            case e: NoSuchElementException => ()
          }
      }
    }
  }
}
