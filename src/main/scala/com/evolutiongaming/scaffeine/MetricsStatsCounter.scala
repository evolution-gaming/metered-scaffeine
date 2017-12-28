package com.evolutiongaming.scaffeine

import com.github.benmanes.caffeine.cache.stats.{CacheStats, StatsCounter}
import io.prometheus.client._

class MetricsStatsCounter(registry: CollectorRegistry, prefix: String) extends StatsCounter {

  private def create[C <: SimpleCollector[_], B <: SimpleCollector.Builder[B, C]](builder: B, name: String): C = {
    val collector = builder
      .name(s"${ prefix }_$name".replaceAll("\\.", "_"))
      .help(s"$prefix $name")
      .create()

    try {
      registry register collector
    } catch {
      case _: IllegalArgumentException => // unfortunately there is no way to check if a collector already registered
    }
    collector
  }

  private def createCounter(name: String) = create[Counter, Counter.Builder](Counter.build(), name)

  private def createHistogram(name: String) = create[Histogram, Histogram.Builder](Histogram.build(), name)

  private val misses = createCounter("misses")

  private val hits = createCounter("hits")

  private val eviction = createCounter("evictions")

  private val failure = createCounter("load_failure")

  private val success = createCounter("load_success")

  private val loadTime = createHistogram("load_time")

  private val blockingCounter = createCounter("blocking_calls")

  private val gauge = create[Gauge, Gauge.Builder](Gauge.build(), "estimatedSize")

  override def snapshot = new CacheStats(
    hits.get.toLong,
    misses.get.toLong,
    success.get.toLong,
    failure.get.toLong,
    0,
    eviction.get.toLong,
    0)

  override def recordMisses(i: Int): Unit = misses.inc(i.toDouble)

  override def recordHits(i: Int): Unit = hits.inc(i.toDouble)

  override def recordEviction(): Unit = eviction.inc()

  override def recordLoadFailure(nanos: Long): Unit = {
    failure.inc()
    loadTime.observe(nanos.toDouble / 1000)
  }

  override def recordLoadSuccess(nanos: Long): Unit = {
    success.inc()
    loadTime.observe(nanos.toDouble / 1000)
  }

  def recordBlockingCall(): Unit = {
    blockingCounter.inc()
  }

  def registerEstimatedSize(f: => Long): Unit = {
    val child = new Gauge.Child() {
      override def get() = f.toDouble
    }
    gauge.setChild(child)
  }
}
