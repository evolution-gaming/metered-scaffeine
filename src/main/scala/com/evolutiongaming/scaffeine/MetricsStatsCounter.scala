package com.evolutiongaming.scaffeine

import com.codahale.metrics.MetricRegistry
import com.github.benmanes.caffeine.cache.stats.{CacheStats, StatsCounter}
import nl.grons.metrics.scala.{MetricBuilder, MetricName}

import scala.concurrent.duration.Duration

class MetricsStatsCounter(registry: MetricRegistry, prefix: String) extends StatsCounter {
  private val metrics = new MetricBuilder(MetricName(prefix), registry)

  private val misses = metrics.counter("misses")

  private val hits = metrics.counter("hits")

  private val eviction = metrics.counter("evictions")

  private val failure = metrics.counter("load_failure")

  private val success = metrics.counter("load_success")

  private val time = metrics.timer("load_time")

  override def snapshot = new CacheStats(hits.count, misses.count, success.count, failure.count, time.count, eviction.count, 0)

  override def recordMisses(i: Int): Unit = misses += i

  override def recordHits(i: Int): Unit = hits += i

  override def recordEviction(): Unit = eviction.inc()

  override def recordLoadFailure(l: Long): Unit = {
    failure.inc()
    time.update(Duration.fromNanos(l))
  }

  override def recordLoadSuccess(l: Long): Unit = {
    success.inc()
    time.update(Duration.fromNanos(l))
  }
}
