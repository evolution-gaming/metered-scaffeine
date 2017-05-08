package com.evolutiongaming

import java.util
import java.util.Collections
import java.util.concurrent.AbstractExecutorService

import com.github.blemale.scaffeine.Scaffeine

import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService, Future}
import scala.concurrent.duration.TimeUnit

package object scaffeine {

  val Scaffeine = com.github.blemale.scaffeine.Scaffeine

  private def executor(ec: ExecutionContext): ExecutionContextExecutorService = ec match {
    case eces: ExecutionContextExecutorService => eces
    case other => new AbstractExecutorService with ExecutionContextExecutorService {
      override def prepare(): ExecutionContext = other
      override def isShutdown = false
      override def isTerminated = false
      override def shutdown(): Unit = ()
      override def shutdownNow(): util.List[Runnable] = Collections.emptyList[Runnable]
      override def execute(runnable: Runnable): Unit = other execute runnable
      override def reportFailure(t: Throwable): Unit = other reportFailure t
      override def awaitTermination(length: Long,unit: TimeUnit): Boolean = false
    }
  }

  implicit class AsyncCacheOps(val c: Scaffeine[Any, Any]) extends AnyVal {

    def asyncCache[K, V](loader: (K) => Future[Option[V]])(implicit ec: ExecutionContext): ScalaAsyncLoadingCache[K, V] = {
      val l = loader andThen { _ map { _ getOrElse null.asInstanceOf[V] }}

      new ScalaAsyncLoadingCache[K, V](c.executor(executor(ec)).buildAsyncFuture[K, V](l).underlying)(ec)
    }
  }

}
