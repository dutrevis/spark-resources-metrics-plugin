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

/**
  * Collects memory resource metrics from a unix-based operating system.
  * <p>
  * Use when Spark is running in clusters with standalone, Mesos or YARN resource managers.
  * <p>
  * Memory metrics are obtained from the numbers of each line of the `/proc/meminfo` file,
  * available at the proc pseudo-filesystem of unix-based operating systems.
  * The file has statistics about memory usage on the system, arranged in lines consisted
  * of a parameter name, followed by a colon, the value of the parameter, and an option
  * unit of measurement.
  * <p>
  * @note While the `/proc/meminfo` file shows kilobytes (kB; 1 kB equals 1000 B), its unit
  * is actually kibibytes (KiB; 1 KiB equals 1024 B). This imprecision is known, but is not
  * corrected due to legacy concerns.
  * @note Many fields have been present since at least Linux 2.6.0, but most of the other
  * fields are available at specific Linux versions (as noted in each method docstring) or
  * are displayed only if the kernel was configured with specific options. Their parsing
  * requires caution when they are implemented.
  */
class MemoryMetrics extends SparkPlugin {

  /**
    * Collects the `MemTotal` parameter, which is the total usable RAM (i.e., physical RAM
    * minus a few reserved bits and the kernel binary code), and registers the result into
    * the provided MetricRegistry.
    * <p>
    * @param metricRegistry a MetricRegistry instance from dropwizard.metrics
    * <p>
    * @note Registered value is in kibibytes and is of type LONG.
    */
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

  /**
    * Collects the `MemFree` parameter, which is the amount of physical RAM left unused by
    * the system, and registers the value into the provided MetricRegistry.
    * <p>
    * @param metricRegistry a MetricRegistry instance from dropwizard.metrics
    * <p>
    * @note Registered value is in kibibytes and is of type LONG.
    */
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

  /**
    * Calculates the total used memory, by collecting the `MemTotal` parameter - the total
    * usable RAM (i.e., physical RAM minus a few reserved bits and the kernel binary code)
    * - and the `MemFree` parameter - the amount of physical RAM left unused by the system
    * -, subtracting the value of the latter out of the first and registering the result
    * into the provided MetricRegistry.
    * <p>
    * @param metricRegistry a MetricRegistry instance from dropwizard.metrics
    * <p>
    * @note Registered value is in kibibytes and is of type LONG.
    */
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

  /**
    * Collects the `Shmem` parameter - the amount of memory located either for processes
    * with separate address spaces that are sharing a portion of memory, or for tmpfs(5)
    * filesystems, whose contents reside in virtual memory to speed up file access. It
    * then registers the value into the provided MetricRegistry.
    * <p>
    * @param metricRegistry a MetricRegistry instance from dropwizard.metrics
    * <p>
    * @note Available since Linux 2.6.32
    * @note Registered value is in kibibytes and is of type LONG.
    */
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

  /**
    * Collects the `Buffers` parameter, which is the amount of memory that is used as a
    * relatively temporary storage for raw disk blocks, amd registers the value into the
    * provided MetricRegistry.
    * <p>
    * @param metricRegistry a MetricRegistry instance from dropwizard.metrics
    * <p>
    * @note This value shouldn't get tremendously large (20 MB or so).
    * @note Registered value is in kibibytes and is of type LONG.
    */
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

  /**
    * Collects the `SwapTotal` parameter, which is the total amount of swap space available
    * on the disk - when the physical memory is full, the system uses the swap space.
    * The value is then registered into the provided MetricRegistry.
    * <p>
    * @param metricRegistry a MetricRegistry instance from dropwizard.metrics
    * <p>
    * @note Registered value is in kibibytes and is of type LONG.
    */
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

  /**
    * Collects the `SwapFree` parameter, which is the amount of swap space currently
    * unused - the memory that has been transferred from RAM to the disk temporarily.
    * The value is then registered into the provided MetricRegistry.
    * <p>
    * @param metricRegistry a MetricRegistry instance from dropwizard.metrics
    * <p>
    * @note Registered value is in kibibytes and is of type LONG.
    */
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

  /**
    * Calculates the used swap memory by collecting the `SwapTotal` parameter - the total
    * amount of swap space available on the disk - and the `SwapFree` parameter - the total
    * amount of swap space currently unused -, subtracting the value of the latter out of
    * the first and registering the result into the provided MetricRegistry.
    * <p>
    * @param metricRegistry a MetricRegistry instance from dropwizard.metrics
    * <p>
    * @note Registered value is in kibibytes and is of type LONG.
    */
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
