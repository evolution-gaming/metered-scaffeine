package com.evolutiongaming.scaffeine

import com.github.benmanes.caffeine.cache.stats.{CacheStats, StatsCounter}
import io.prometheus.client._

class MetricsStatsCounter(registry: CollectorRegistry, prefix: String) extends StatsCounter {

  private def register(collector: Collector): Unit = {
    try {
      registry register collector
    } catch {
      case _: IllegalArgumentException => // unfortunately there is no way to check if a collector already registered
    }
  }

  private def createCounter(name: String, help: String) = {
    val collector = Counter.build()
      .name(s"${ prefix }_$name")
      .help(help)
      .create()
    register(collector)
    collector
  }

  private val misses = createCounter("misses", "Misses")

  private val hits = createCounter("hits", "Hits")

  private val eviction = createCounter("evictions", "Evictions")

  private val blockingCounter = createCounter("blocking_calls", "Blocking calls")

  private val result = {
    val collector = Counter.build()
      .name(s"${ prefix }_load_result")
      .help("Load result: success or failure")
      .labelNames("result")
      .create()
    register(collector)
    collector
  }

  private val loadTime = {
    val collector = Summary.build()
      .name(s"${ prefix }_load_time")
      .help(s"Load time in millis")
      .quantile(0.9, 0.01)
      .quantile(0.99, 0.001)
      .create()
    register(collector)
    collector
  }

  private val gauge = {
    val collector = Gauge.build()
      .name(s"${ prefix }_estimated_size")
      .help(s"Estimated size")
      .create()
    register(collector)
    collector
  }

  override def snapshot = new CacheStats(
    hits.get.toLong,
    misses.get.toLong,
    0,
    0,
    0,
    eviction.get.toLong,
    0)

  override def recordMisses(i: Int): Unit = misses.inc(i.toDouble)

  override def recordHits(i: Int): Unit = hits.inc(i.toDouble)

  override def recordEviction(): Unit = eviction.inc()

  override def recordLoadFailure(nanos: Long): Unit = {
    result.labels("failure").inc()
    loadTime.observe(nanos.toDouble / 1000)
  }

  override def recordLoadSuccess(nanos: Long): Unit = {
    result.labels("success").inc()
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
