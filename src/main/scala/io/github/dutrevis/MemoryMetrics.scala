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

// Collects memory resource metrics from the unix-based operating system.
// Use when running Spark with fisical clusters.

class MemoryMetrics extends SparkPlugin {

  def registerTotalMemory(metricRegistry: MetricRegistry): Unit = {
    metricRegistry.register(
      MetricRegistry.name("TotalMemory"),
      new Gauge[Long] {
        override def getValue: Long = {
          val procFile = Source.fromFile(MemoryMetrics.procFileName)
          val procFileData = procFile.getLines
            .filter(metric => metric.contains("MemTotal"))
            .map(_.split(":"))
            .map { case Array(k, v) =>
              k -> v.trim().split(" ").head.toLong
            }
            .toMap
          val metricValue = procFileData("MemTotal")
          procFile.close()
          metricValue
        }
      }
    )
  }

  def registerFreeMemory(metricRegistry: MetricRegistry): Unit = {
    metricRegistry.register(
      MetricRegistry.name("FreeMemory"),
      new Gauge[Long] {
        override def getValue: Long = {
          val procFile = Source.fromFile(MemoryMetrics.procFileName)
          val procFileData = procFile.getLines
            .filter(metric => metric.contains("MemFree"))
            .map(_.split(":"))
            .map { case Array(k, v) => k -> v.trim().split(" ").head.toLong }
            .toMap
          val metricValue = procFileData("MemFree")
          procFile.close()
          metricValue
        }
      }
    )
  }

  def registerUsedMemory(metricRegistry: MetricRegistry): Unit = {
    metricRegistry.register(
      MetricRegistry.name("UsedMemory"),
      new Gauge[Long] {
        override def getValue: Long = {
          val procFile = Source.fromFile(MemoryMetrics.procFileName)
          val procFileData = procFile.getLines
            .filter(metric =>
              metric.contains("MemTotal") || metric.contains("MemFree")
            )
            .map(_.split(":"))
            .map { case Array(k, v) => k -> v.trim().split(" ").head.toLong }
            .toMap
          val metricValue = procFileData("MemTotal") - procFileData("MemFree")
          procFile.close()
          metricValue
        }
      }
    )
  }

  def registerSharedMemory(metricRegistry: MetricRegistry): Unit = {
    metricRegistry.register(
      MetricRegistry.name("SharedMemory"),
      new Gauge[Long] {
        override def getValue: Long = {
          val procFile = Source.fromFile(MemoryMetrics.procFileName)
          val procFileData = procFile.getLines
            .filter(metric => metric.contains("Shmem"))
            .map(_.split(":"))
            .map { case Array(k, v) => k -> v.trim().split(" ").head.toLong }
            .toMap
          val metricValue = procFileData("Shmem")
          procFile.close()
          metricValue
        }
      }
    )
  }

  def registerBufferMemory(metricRegistry: MetricRegistry): Unit = {
    metricRegistry.register(
      MetricRegistry.name("BufferMemory"),
      new Gauge[Long] {
        override def getValue: Long = {
          val procFile = Source.fromFile(MemoryMetrics.procFileName)
          val procFileData = procFile.getLines
            .filter(metric => metric.contains("Buffers"))
            .map(_.split(":"))
            .map { case Array(k, v) => k -> v.trim().split(" ").head.toLong }
            .toMap
          val metricValue = procFileData("Buffers")
          procFile.close()
          metricValue
        }
      }
    )
  }

  def registerTotalSwapMemory(metricRegistry: MetricRegistry): Unit = {
    metricRegistry.register(
      MetricRegistry.name("TotalSwapMemory"),
      new Gauge[Long] {
        override def getValue: Long = {
          val procFile = Source.fromFile(MemoryMetrics.procFileName)
          val procFileData = procFile.getLines
            .filter(metric => metric.contains("SwapTotal"))
            .map(_.split(":"))
            .map { case Array(k, v) => k -> v.trim().split(" ").head.toLong }
            .toMap
          val metricValue = procFileData("SwapTotal")
          procFile.close()
          metricValue
        }
      }
    )
  }

  def registerFreeSwapMemory(metricRegistry: MetricRegistry): Unit = {
    metricRegistry.register(
      MetricRegistry.name("FreeSwapMemory"),
      new Gauge[Long] {
        override def getValue: Long = {
          val procFile = Source.fromFile(MemoryMetrics.procFileName)
          val procFileData = procFile.getLines
            .filter(_.contains("SwapFree"))
            .map(_.split(":"))
            .map { case Array(k, v) => k -> v.trim().split(" ").head.toLong }
            .toMap
          val metricValue = procFileData("SwapFree")
          procFile.close()
          metricValue
        }
      }
    )
  }

  def registerCachedSwapMemory(metricRegistry: MetricRegistry): Unit = {
    metricRegistry.register(
      MetricRegistry.name("CachedSwapMemory"),
      new Gauge[Long] {
        override def getValue: Long = {
          val procFile = Source.fromFile(MemoryMetrics.procFileName)
          val procFileData = procFile.getLines
            .filter(metric =>
              metric.contains("SwapTotal") || metric.contains("SwapFree")
            )
            .map(_.split(":"))
            .map { case Array(k, v) => k -> v.trim().split(" ").head.toLong }
            .toMap
          val metricValue = procFileData("SwapTotal") - procFileData("SwapFree")
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
        registerTotalMemory(myContext.metricRegistry)
        registerFreeMemory(myContext.metricRegistry)
        registerUsedMemory(myContext.metricRegistry)
        registerSharedMemory(myContext.metricRegistry)
        registerBufferMemory(myContext.metricRegistry)
        registerTotalSwapMemory(myContext.metricRegistry)
        registerFreeSwapMemory(myContext.metricRegistry)
        registerCachedSwapMemory(myContext.metricRegistry)
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
        registerTotalMemory(myContext.metricRegistry)
        registerFreeMemory(myContext.metricRegistry)
        registerUsedMemory(myContext.metricRegistry)
        registerSharedMemory(myContext.metricRegistry)
        registerBufferMemory(myContext.metricRegistry)
        registerTotalSwapMemory(myContext.metricRegistry)
        registerFreeSwapMemory(myContext.metricRegistry)
        registerCachedSwapMemory(myContext.metricRegistry)
      }
    }
  }

}

object MemoryMetrics {
  private val procFileName = "/proc/meminfo"
}
