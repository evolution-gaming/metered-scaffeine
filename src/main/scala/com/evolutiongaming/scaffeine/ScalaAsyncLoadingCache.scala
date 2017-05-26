package com.evolutiongaming.scaffeine

import com.github.blemale.scaffeine.AsyncLoadingCache
import com.github.benmanes.caffeine.cache.{AsyncLoadingCache => CaffeineAsyncLoadingCache}

import scala.compat.java8.FutureConverters._
import scala.concurrent.{ExecutionContext, Future}

class ScalaAsyncLoadingCache[K, V](
  override val underlying: CaffeineAsyncLoadingCache[K, V],
  statsCounter: Option[MetricsStatsCounter] = None)
  (implicit ec: ExecutionContext) extends AsyncLoadingCache(underlying) {

  def getAsync(key: K): Future[Option[V]] = {
    (underlying.get(key) match {
      case f if f.isDone && !f.isCompletedExceptionally && !f.isCancelled => Future.successful(f.get())
      case f => f.toScala
    }).map(Option(_))
  }

  def getSync(key: K): Option[V] = {
    statsCounter.foreach { _.recordBlockingCall() }
    Option(underlying.synchronous().get(key))
  }
}
