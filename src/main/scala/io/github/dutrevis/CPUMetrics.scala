package io.github.dutrevis

import java.util.{Map => JMap}
import scala.collection.JavaConverters._
import scala.io.Source

import com.codahale.metrics.{Gauge, MetricRegistry}
import org.apache.spark.SparkContext
import org.apache.spark.api.plugin.{
  DriverPlugin,
  ExecutorPlugin,
  PluginContext,
  SparkPlugin
}

/**
  * Collects CPU resource metrics from a unix-based operating system.
  * <p>
  * Use when Spark is running in clusters with standalone, Mesos or YARN resource managers.
  * <p>
  * CPU metrics are obtained from the numbers of the first line of the `/proc/stat` file,
  * available at the proc pseudo-filesystem of unix-based operating systems.
  * These numbers identify the amount of time the CPU has spent performing
  * different kinds of work, arranged in columns at the following order:
  * "cpu_user", "cpu_nice", "cpu_system", "cpu_idle", "cpu_iowait", "cpu_irq" and "cpu_softirq".
  * <p>
  * @note All of the numbers retrieved are aggregates since the system first booted.
  * @note Time units are in USER_HZ or Jiffies (typically hundredths of a second)
  * @note Values for "cpu_steal", "cpu_guest" and "cpu_guest_nice", available at spectific
  * Linux versions, are not parsed from the file.
  */
class CPUMetrics extends SparkPlugin {

  /**
    * Collects the aggregated CPU usage value for normal processes executing in user mode,
    * calculates its average out of the total of CPU usage time for all processes and
    * registers the result into the provided MetricRegistry.
    * <p>
    * @param metricRegistry a MetricRegistry instance from dropwizard.metrics
    * <p>
    * @note Registered value is of type LONG with precision of 2.
    */
  def registerUserCPU(metricRegistry: MetricRegistry): Unit = {
    metricRegistry.register(
      MetricRegistry.name("UserCPU"),
      new Gauge[Double] {
        override def getValue: Double = {
          val procFile = Source.fromFile(CPUMetrics.procFileName)
          val procFileData = (CPUMetrics.cpuColumns zip procFile.getLines
            .take(1)
            .mkString
            .split(" +")
            .tail
            .map(_.toLong)).toMap
          val metricValue =
            procFileData("cpu_user") / procFileData.foldLeft(0.0)(_ + _._2)
          procFile.close()
          metricValue
        }
      }
    )
  }

  /**
    * Collects the aggregated CPU usage value for niced processes (there is, run with the
    * nice command) executing in user mode, calculates its average out of the total of the
    * CPU usage time for all processes and registers the result into the provided MetricRegistry.
    * <p>
    * @param metricRegistry a MetricRegistry instance from dropwizard.metrics
    * <p>
    * @note Registered value is of type LONG with precision of 2.
    */
  def registerNiceCPU(metricRegistry: MetricRegistry): Unit = {
    metricRegistry.register(
      MetricRegistry.name("NiceCPU"),
      new Gauge[Double] {
        override def getValue: Double = {
          val procFile = Source.fromFile(CPUMetrics.procFileName)
          val procFileData = (CPUMetrics.cpuColumns zip procFile.getLines
            .take(1)
            .mkString
            .split(" +")
            .tail
            .map(_.toLong)).toMap
          val metricValue =
            procFileData("cpu_nice") / procFileData.foldLeft(0.0)(_ + _._2)
          procFile.close()
          metricValue
        }
      }
    )
  }

  /**
    * Collects the aggregated CPU usage value processes executing in kernel mode,
    * calculates its average out of the total of the CPU usage time for all processes
    * and registers the result into the provided MetricRegistry.
    * <p>
    * @param metricRegistry a MetricRegistry instance from dropwizard.metrics
    * <p>
    * @note Registered value is of type LONG with precision of 2.
    */
  def registerSystemCPU(metricRegistry: MetricRegistry): Unit = {
    metricRegistry.register(
      MetricRegistry.name("SystemCPU"),
      new Gauge[Double] {
        override def getValue: Double = {
          val procFile = Source.fromFile(CPUMetrics.procFileName)
          val procFileData = (CPUMetrics.cpuColumns zip procFile.getLines
            .take(1)
            .mkString
            .split(" +")
            .tail
            .map(_.toLong)).toMap
          val metricValue =
            procFileData("cpu_system") / procFileData.foldLeft(0.0)(_ + _._2)
          procFile.close()
          metricValue
        }
      }
    )
  }

  /**
    * Collects the aggregated CPU usage value for when no processes are running,
    * calculates its average out of the total of the CPU usage time for all processes
    * and registers the result into the provided MetricRegistry.
    * <p>
    * @param metricRegistry a MetricRegistry instance from dropwizard.metrics
    * <p>
    * @note Registered value is of type LONG with precision of 2.
    */
  def registerIdleCPU(metricRegistry: MetricRegistry): Unit = {
    metricRegistry.register(
      MetricRegistry.name("IdleCPU"),
      new Gauge[Double] {
        override def getValue: Double = {
          val procFile = Source.fromFile(CPUMetrics.procFileName)
          val procFileData = (CPUMetrics.cpuColumns zip procFile.getLines
            .take(1)
            .mkString
            .split(" +")
            .tail
            .map(_.toLong)).toMap
          val metricValue =
            procFileData("cpu_idle") / procFileData.foldLeft(0.0)(_ + _._2)
          procFile.close()
          metricValue
        }
      }
    )
  }

  /**
    * Collects the aggregated CPU usage value for when it's waiting for I/O to complete,
    * calculates its average out of the total of the CPU usage time for all processes
    * and registers the result into the provided MetricRegistry.
    * <p>
    * @param metricRegistry a MetricRegistry instance from dropwizard.metrics
    * <p>
    * @note Registered value is of type LONG with precision of 2.
    */
  def registerWaitCPU(metricRegistry: MetricRegistry): Unit = {
    metricRegistry.register(
      MetricRegistry.name("WaitCPU"),
      new Gauge[Double] {
        override def getValue: Double = {
          val procFile = Source.fromFile(CPUMetrics.procFileName)
          val procFileData = (CPUMetrics.cpuColumns zip procFile.getLines
            .take(1)
            .mkString
            .split(" +")
            .tail
            .map(_.toLong)).toMap
          val metricValue =
            procFileData("cpu_iowait") / procFileData.foldLeft(0.0)(_ + _._2)
          procFile.close()
          metricValue
        }
      }
    )
  }

  // Return the plugin's driver-side component.
  override def driverPlugin(): DriverPlugin = {
    new DriverPlugin() {
      override def init(
          sc: SparkContext,
          myContext: PluginContext
      ): JMap[String, String] = {
        registerUserCPU(myContext.metricRegistry)
        registerNiceCPU(myContext.metricRegistry)
        registerSystemCPU(myContext.metricRegistry)
        registerIdleCPU(myContext.metricRegistry)
        registerWaitCPU(myContext.metricRegistry)
        Map.empty[String, String].asJava
      }
    }
  }

  // Return the plugin's executor-side component.
  override def executorPlugin(): ExecutorPlugin = {
    new ExecutorPlugin() {
      override def init(
          myContext: PluginContext,
          extraConf: JMap[String, String]
      ): Unit = {
        registerUserCPU(myContext.metricRegistry)
        registerNiceCPU(myContext.metricRegistry)
        registerSystemCPU(myContext.metricRegistry)
        registerIdleCPU(myContext.metricRegistry)
        registerWaitCPU(myContext.metricRegistry)
      }
    }
  }

}

object CPUMetrics {
  private val procFileName = "/proc/stat"
  private val cpuColumns = List(
    "cpu_user",
    "cpu_nice",
    "cpu_system",
    "cpu_idle",
    "cpu_iowait",
    "cpu_irq",
    "cpu_softirq"
  )
}
