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

/** Collects CPU resource metrics from a unix-based operating system. <p> Use
  * when Spark is running in clusters with standalone, Mesos or YARN resource
  * managers. <p> CPU metrics are obtained from the numbers of the first line of
  * the `/proc/stat` file, available at the proc pseudo-filesystem of unix-based
  * operating systems. These numbers identify the amount of time the CPU has
  * spent performing different kinds of work, arranged in columns at the
  * following order: "cpu_user", "cpu_nice", "cpu_system", "cpu_idle",
  * "cpu_iowait", "cpu_irq" and "cpu_softirq". <p>
  * @note
  *   All of the numbers retrieved are aggregates since the system first booted.
  * @note
  *   Time units are in USER_HZ or Jiffies (typically hundredths of a second)
  * @note
  *   Values for "cpu_steal", "cpu_guest" and "cpu_guest_nice", available at
  *   spectific Linux versions, are not parsed from the file.
  */
class CPUMetrics extends SparkPlugin {

  /** Maps the collector methods to their respective metric names, that will be
    * displayed in the Dropwizard's metric system.
    */
  val metricMapping = Map[String, (StatMetricCollector) => Double](
    "UserCPU" -> collectUserCPU,
    "NiceCPU" -> collectNiceCPU,
    "SystemCPU" -> collectSystemCPU,
    "IdleCPU" -> collectIdleCPU,
    "WaitCPU" -> collectWaitCPU
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
    *   a StatMetricCollector instance <p>
    * @param collectorMethod
    *   a method that receives the metricCollector and returns a metric value as
    *   a Double <p>
    */
  def createGaugeMetric(
      metricCollector: StatMetricCollector,
      collectorMethod: (StatMetricCollector) => Double
  ): Gauge[Double] = {
    new Gauge[Double] {
      override def getValue: Double = { collectorMethod(metricCollector) }
    }
  }

  /** Collects the aggregated CPU usage value for normal processes executing in
    * user mode, as an average out of the total of CPU usage time for all
    * processes. <p>
    * @param metricCollector
    *   a StatMetricCollector instance <p>
    * @note
    *   Collected value is of type Double with precision of 2.
    */
  def collectUserCPU(metricCollector: StatMetricCollector): Double = {
    val procFileContent = metricCollector.getProcFileContent()
    val originalMetricName: String = "cpu_user"
    metricCollector.getMetricValue(procFileContent, originalMetricName)
  }

  /** Collects the aggregated CPU usage value for niced processes (there is, run
    * with the nice command) executing in user mode and calculates its average
    * out of the total of the CPU usage time for all processes. <p>
    * @param metricCollector
    *   a StatMetricCollector instance <p>
    * @note
    *   Collected value is of type Double with precision of 2.
    */
  def collectNiceCPU(metricCollector: StatMetricCollector): Double = {
    val procFileContent = metricCollector.getProcFileContent()
    val originalMetricName: String = "cpu_nice"
    metricCollector.getMetricValue(procFileContent, originalMetricName)
  }

  /** Collects the aggregated CPU usage value processes executing in kernel mode
    * as an average out of the total of the CPU usage time for all processes.
    * <p>
    * @param metricCollector
    *   a StatMetricCollector instance <p>
    * @note
    *   Collected value is of type Double with precision of 2.
    */
  def collectSystemCPU(metricCollector: StatMetricCollector): Double = {
    val procFileContent = metricCollector.getProcFileContent()
    val originalMetricName: String = "cpu_system"
    metricCollector.getMetricValue(procFileContent, originalMetricName)
  }

  /** Collects the aggregated CPU usage value for when no processes are running,
    * as an average out of the total of the CPU usage time for all processes.
    * <p>
    * @param metricCollector
    *   a StatMetricCollector instance <p>
    * @note
    *   Collected value is of type Double with precision of 2.
    */
  def collectIdleCPU(metricCollector: StatMetricCollector): Double = {
    val procFileContent = metricCollector.getProcFileContent()
    val originalMetricName: String = "cpu_idle"
    metricCollector.getMetricValue(procFileContent, originalMetricName)
  }

  /** Collects the aggregated CPU usage value for when it's waiting for I/O to
    * complete, as an average out of the total of the CPU usage time for all
    * processes. <p>
    * @param metricCollector
    *   a StatMetricCollector instance <p>
    * @note
    *   Collected value is of type Double with precision of 2.
    */
  def collectWaitCPU(metricCollector: StatMetricCollector): Double = {
    val procFileContent = metricCollector.getProcFileContent()
    val originalMetricName: String = "cpu_iowait"
    metricCollector.getMetricValue(procFileContent, originalMetricName)
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
        val metricCollector = new StatMetricCollector
        for (
          (
            metricName: String,
            collectorMethod: ((StatMetricCollector) => Double)
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
        val metricCollector = new StatMetricCollector
        for (
          (
            metricName: String,
            collectorMethod: ((StatMetricCollector) => Double)
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
