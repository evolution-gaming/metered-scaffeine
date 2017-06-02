package com.evolutiongaming.scaffeine

import java.util.concurrent.ExecutionException

import com.evolutiongaming.concurrent.{CurrentThreadExecutionContext, ExecutionContextExecutorServiceFactory}
import com.github.benmanes.caffeine.cache.{AsyncLoadingCache => CaffeineAsyncLoadingCache}
import com.github.blemale.scaffeine.{AsyncLoadingCache, Scaffeine}

import scala.compat.java8.FutureConverters._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Try}

class ScalaAsyncLoadingCache[K, V](
  underlying: CaffeineAsyncLoadingCache[K, V],
  statsCounter: Option[MetricsStatsCounter] = None) extends AsyncLoadingCache(underlying) {

  def getFuture(key: K): Future[Option[V]] = {
    val cf = underlying get key
    if (cf.isDone) {
      Future fromTry Try(Option(cf.get())).recoverWith { case e: ExecutionException => Failure(e.getCause) }
    } else {
      (cf.toScala map Option.apply) (CurrentThreadExecutionContext)
    }
  }

  def getBlocking(key: K): Option[V] = {
    if (!(underlying get key).isDone) statsCounter foreach { _.recordBlockingCall() }
    Option(underlying.synchronous().get(key))
  }
}

object ScalaAsyncLoadingCache {
  def apply[K, V](
    scaffeine: Scaffeine[Any, Any],
    loader: K => Future[Option[V]],
    statsCounter: Option[MetricsStatsCounter])
    (implicit ec: ExecutionContext): ScalaAsyncLoadingCache[K, V] = {

    val l = loader andThen { _.map { _ getOrElse null.asInstanceOf[V] }(CurrentThreadExecutionContext) }

    val executorService = ExecutionContextExecutorServiceFactory(ec)
    val cache = scaffeine.executor(executorService).buildAsyncFuture[K, V](l).underlying
    new ScalaAsyncLoadingCache[K, V](cache, statsCounter)
  }
}
