package com.evolutiongaming.scaffeine

import com.github.benmanes.caffeine.cache.RemovalCause
import com.github.benmanes.caffeine.cache.stats.CacheStats
import io.prometheus.metrics.core.metrics.{Counter, GaugeWithCallback, Summary}
import io.prometheus.metrics.model.registry.{Collector, PrometheusRegistry}
import io.prometheus.metrics.model.snapshots.{Unit => PrometheusUnit}

/**
 * [[MeteredStatsCounter]] backed by the Prometheus client 1.x, exposing the same metrics as
 * [[MetricsStatsCounter]] does with the 0.x simpleclient. The 1.x client adds the `_total` suffix
 * to counter names on its own, and its summaries compute quantiles over a 5 minute window instead
 * of 10.
 *
 * It is kept apart from [[MetricsStatsCounter]] because `prometheus-metrics-core` is an optional
 * dependency: only consumers who add it themselves may load classes that reference it.
 */
class PrometheusV1MetricsStatsCounter(registry: PrometheusRegistry, prefix: String) extends MeteredStatsCounter {

  // like MetricsStatsCounter: when a cache with the same name already registered its metrics, keep them
  private def register[A <: Collector](collector: A): A = {
    try registry.register(collector)
    catch { case _: IllegalArgumentException => () }
    collector
  }

  private def createCounter(name: String, help: String) =
    register(Counter.builder().name(s"${ prefix }_$name").help(help).build())

  private val misses = createCounter("misses", "Misses")

  private val hits = createCounter("hits", "Hits")

  private val eviction = createCounter("evictions", "Evictions")

  private val blockingCounter = createCounter("blocking_calls", "Blocking calls")

  private val result = register(
    Counter
      .builder()
      .name(s"${ prefix }_load_result")
      .help("Load result: success or failure")
      .labelNames("result")
      .build(),
  )

  private lazy val loadSuccess = result.labelValues("success")

  private lazy val loadFailure = result.labelValues("failure")

  private val loadTime = register(
    Summary
      .builder()
      .name(s"${ prefix }_load_time")
      .help("Load time in seconds")
      .quantile(0.9, 0.01)
      .quantile(0.99, 0.001)
      .build(),
  )

  // the gauge callback is fixed at build time, but the cache, and so its size, only exists after registerEstimatedSize
  @volatile private var estimatedSize: () => Long = () => 0L

  register(
    GaugeWithCallback
      .builder()
      .name(s"${ prefix }_estimated_size")
      .help("Estimated size")
      .callback(callback => callback.call(estimatedSize().toDouble))
      .build(),
  )

  override def snapshot: CacheStats = CacheStats.of(
    hits.get.toLong,
    misses.get.toLong,
    0,
    0,
    0,
    eviction.get.toLong,
    0,
  )

  override def recordMisses(count: Int): Unit = misses.inc(count.toDouble)

  override def recordHits(count: Int): Unit = hits.inc(count.toDouble)

  override def recordEviction(weight: Int, cause: RemovalCause): Unit = eviction.inc(weight.toDouble)

  override def recordLoadFailure(nanos: Long): Unit = {
    loadFailure.inc()
    loadTime.observe(PrometheusUnit.nanosToSeconds(nanos))
  }

  override def recordLoadSuccess(nanos: Long): Unit = {
    loadSuccess.inc()
    loadTime.observe(PrometheusUnit.nanosToSeconds(nanos))
  }

  def recordBlockingCall(): Unit = blockingCounter.inc()

  def registerEstimatedSize(f: => Long): Unit = estimatedSize = () => f
}
