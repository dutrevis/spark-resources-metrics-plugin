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

// Collects CPU resource metrics from the unix-based operating system.
// Use when running Spark with fisical clusters.

class CPUMetrics extends SparkPlugin {

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
