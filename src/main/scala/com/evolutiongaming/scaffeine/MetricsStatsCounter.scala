package com.evolutiongaming.scaffeine

import com.github.benmanes.caffeine.cache.stats.{CacheStats, StatsCounter}
import io.prometheus.client.{CollectorRegistry, Counter}

import scala.concurrent.duration.Duration

class MetricsStatsCounter(registry: CollectorRegistry, prefix: String) extends StatsCounter {

  private def createCounter(name: String): Counter = {
    val c = Counter.build().name(s"${prefix}_$name".replaceAll("\\.", "_")).help(s"$prefix $name").create()
    registry.register(c)
    c
  }

  private val misses = createCounter("misses")

  private val hits = createCounter("hits")

  private val eviction = createCounter("evictions")

  private val failure = createCounter("load_failure")

  private val success = createCounter("load_success")

  private val time = createCounter("load_time")

  private val blockingCounter = createCounter("blocking_calls")

  override def snapshot = new CacheStats(hits.get.toLong, misses.get.toLong, success.get.toLong, failure.get.toLong, time.get.toLong, eviction.get.toLong, 0)

  override def recordMisses(i: Int): Unit = misses.inc(i.toDouble)

  override def recordHits(i: Int): Unit = hits.inc(i.toDouble)

  override def recordEviction(): Unit = eviction.inc()

  override def recordLoadFailure(l: Long): Unit = {
    failure.inc()
    time.inc(Duration.fromNanos(l).toMillis.toDouble)
  }

  override def recordLoadSuccess(l: Long): Unit = {
    success.inc()
    time.inc(Duration.fromNanos(l).toMillis.toDouble)
  }

  def recordBlockingCall(): Unit = {
    blockingCounter.inc()
  }
}
