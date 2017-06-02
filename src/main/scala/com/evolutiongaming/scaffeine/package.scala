package com.evolutiongaming

import com.codahale.metrics.MetricRegistry
import com.github.blemale.scaffeine.Scaffeine

import scala.concurrent.{ExecutionContext, Future}

package object scaffeine {

  implicit class ScaffeineOps(val scaffeine: Scaffeine[Any, Any]) extends AnyVal {

    def asyncCache[K, V](loader: (K) => Future[Option[V]])
      (implicit ec: ExecutionContext): ScalaAsyncLoadingCache[K, V] = {

      ScalaAsyncLoadingCache(scaffeine, loader, None)
    }

    def asyncCache[K, V](loader: (K) => Future[Option[V]], stats: (String, MetricRegistry))
      (implicit ec: ExecutionContext): ScalaAsyncLoadingCache[K, V] = {

      val (name, registry) = stats
      asyncCache(name, registry)(loader)(ec)
    }

    def asyncCache[K, V](name: String, registry: MetricRegistry)(loader: (K) => Future[Option[V]])
      (implicit ec: ExecutionContext): ScalaAsyncLoadingCache[K, V] = {

      val statsCounter = new MetricsStatsCounter(registry, name)
      ScalaAsyncLoadingCache(scaffeine.recordStats(() => statsCounter), loader, Some(statsCounter))
    }
  }
}
