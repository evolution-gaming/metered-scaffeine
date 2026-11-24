package com.evolutiongaming

import com.github.blemale.scaffeine.Scaffeine
import io.prometheus.client.CollectorRegistry

import scala.concurrent.{ExecutionContext, Future}

package object scaffeine {

  implicit class ScaffeineOps(val scaffeine: Scaffeine[Any, Any]) extends AnyVal {

    def asyncCache[K, V](
      loader: K => Future[Option[V]],
    )(implicit
      ec: ExecutionContext,
    ): ScalaAsyncLoadingCache[K, V] = {

      ScalaAsyncLoadingCache(scaffeine, loader, None)
    }

    def asyncCache[K, V](
      loader: K => Future[Option[V]],
      stats: (String, CollectorRegistry),
    )(implicit
      ec: ExecutionContext,
    ): ScalaAsyncLoadingCache[K, V] = {

      val (name, registry) = stats
      asyncCache(name, registry)(loader)(using ec)
    }

    def asyncCache[K, V](
      name: String,
      registry: CollectorRegistry,
    )(
      loader: K => Future[Option[V]],
    )(implicit
      ec: ExecutionContext,
    ): ScalaAsyncLoadingCache[K, V] = {

      meteredAsyncCache(new MetricsStatsCounter(registry, name))(loader)(using ec)
    }

    /**
     * Async cache that records its statistics with the given counter, e.g. a
     * [[PrometheusV1MetricsStatsCounter]].
     *
     * Not an `asyncCache` overload: one taking a single non-function argument would leave the
     * lambda passed to `asyncCache(loader)` without a parameter type.
     */
    def meteredAsyncCache[K, V](
      statsCounter: MeteredStatsCounter,
    )(
      loader: K => Future[Option[V]],
    )(implicit
      ec: ExecutionContext,
    ): ScalaAsyncLoadingCache[K, V] = {

      ScalaAsyncLoadingCache(scaffeine.recordStats(() => statsCounter), loader, Some(statsCounter))
    }
  }
}
