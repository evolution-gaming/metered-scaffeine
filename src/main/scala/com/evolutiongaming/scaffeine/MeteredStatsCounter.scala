package com.evolutiongaming.scaffeine

import com.github.benmanes.caffeine.cache.stats.StatsCounter

/**
 * Caffeine [[StatsCounter]] that also tracks blocking calls and the estimated cache size, as
 * [[ScalaAsyncLoadingCache]] needs.
 *
 * Implemented by [[MetricsStatsCounter]] for the Prometheus simpleclient 0.x and by
 * [[PrometheusV1MetricsStatsCounter]] for the Prometheus client 1.x.
 */
trait MeteredStatsCounter extends StatsCounter {

  def recordBlockingCall(): Unit

  def registerEstimatedSize(f: => Long): Unit
}
