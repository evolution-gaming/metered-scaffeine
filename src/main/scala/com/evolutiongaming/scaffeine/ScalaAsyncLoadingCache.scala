package com.evolutiongaming.scaffeine

import com.github.blemale.scaffeine.AsyncLoadingCache
import com.github.benmanes.caffeine.cache.{AsyncLoadingCache => CaffeineAsyncLoadingCache}

import scala.compat.java8.FutureConverters._
import scala.concurrent.{ExecutionContext, Future}

class ScalaAsyncLoadingCache[K, V](override val underlying: CaffeineAsyncLoadingCache[K, V])(implicit ec: ExecutionContext) extends AsyncLoadingCache(underlying) {
  def getAsync(key: K)(implicit ec: ExecutionContext): Future[Option[V]] = underlying.get(key).toScala.map(Option(_))

  def getSync(key: K): Option[V] = Option(underlying.synchronous().get(key))
}
