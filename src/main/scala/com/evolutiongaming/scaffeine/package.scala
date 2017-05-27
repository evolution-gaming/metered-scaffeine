package com.evolutiongaming

import java.util
import java.util.Collections
import java.util.concurrent.AbstractExecutorService

import com.codahale.metrics.MetricRegistry
import com.github.blemale.scaffeine.Scaffeine

import scala.concurrent.duration.TimeUnit
import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService, Future}

package object scaffeine {

  val Scaffeine = com.github.blemale.scaffeine.Scaffeine

  private def executor(ec: ExecutionContext): ExecutionContextExecutorService = ec match {
    case eces: ExecutionContextExecutorService => eces
    case other                                 => new AbstractExecutorService with ExecutionContextExecutorService {
      override def prepare(): ExecutionContext = other
      override def isShutdown = false
      override def isTerminated = false
      override def shutdown(): Unit = ()
      override def shutdownNow(): util.List[Runnable] = Collections.emptyList[Runnable]
      override def execute(runnable: Runnable): Unit = other execute runnable
      override def reportFailure(t: Throwable): Unit = other reportFailure t
      override def awaitTermination(length: Long, unit: TimeUnit): Boolean = false
    }
  }

  implicit class AsyncCacheOps(val c: Scaffeine[Any, Any]) extends AnyVal {

    def asyncCache[K, V](loader: (K) => Future[Option[V]])
      (implicit ec: ExecutionContext): ScalaAsyncLoadingCache[K, V] = {

      createCache(c, loader, None)
    }

    def asyncCache[K, V](loader: (K) => Future[Option[V]], stats: (String, MetricRegistry))
      (implicit ec: ExecutionContext): ScalaAsyncLoadingCache[K, V] = {

      val (name, registry) = stats
      asyncCache(name, registry)(loader)(ec)
    }

    def asyncCache[K, V](name: String, registry: MetricRegistry)(loader: (K) => Future[Option[V]])
      (implicit ec: ExecutionContext): ScalaAsyncLoadingCache[K, V] = {

      val statsCounter = new MetricsStatsCounter(registry, name)
      createCache(c.recordStats(() => statsCounter), loader, Some(statsCounter))
    }
  }

  private def createCache[K, V](
    c: Scaffeine[Any, Any],
    loader: (K) => Future[Option[V]],
    statsCounter: Option[MetricsStatsCounter])
    (implicit ec: ExecutionContext) = {

    val l = loader andThen { _ map { _ getOrElse null.asInstanceOf[V] } }

    new ScalaAsyncLoadingCache[K, V](c.executor(executor(ec)).buildAsyncFuture[K, V](l).underlying, statsCounter)(ec)
  }
}
