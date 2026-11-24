package com.evolutiongaming.scaffeine.test

import com.evolutiongaming.concurrent.CurrentThreadExecutionContext
import com.evolutiongaming.scaffeine._
import com.github.blemale.scaffeine.Scaffeine
import io.prometheus.client.CollectorRegistry
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.{ExecutionContext, Future}

class MetricsStatsCounterSpec extends AnyFlatSpec with Matchers {

  private implicit val ec: ExecutionContext = CurrentThreadExecutionContext

  "MetricsStatsCounter" should "count hits, misses and successful loads of a cache" in {
    val registry = new CollectorRegistry()
    val cache = Scaffeine().asyncCache[Int, String]("cache_test", registry) { key =>
      Future.successful(Some(key.toString))
    }

    cache.getBlocking(1) shouldBe Some("1")
    cache.getBlocking(1) shouldBe Some("1")
    cache.synchronous().cleanUp()

    sampleValue(registry, "cache_test_misses") shouldBe Some(1.0)
    sampleValue(registry, "cache_test_hits") shouldBe Some(1.0)
    sampleValue(registry, "cache_test_load_result", "result" -> "success") shouldBe Some(1.0)
    sampleValue(registry, "cache_test_load_time_count") shouldBe Some(1.0)
    sampleValue(registry, "cache_test_estimated_size") shouldBe Some(1.0)
  }

  it should "be usable where a MeteredStatsCounter is expected" in {
    val registry = new CollectorRegistry()
    val statsCounter: MeteredStatsCounter = new MetricsStatsCounter(registry, "cache_test")

    statsCounter.recordBlockingCall()

    sampleValue(registry, "cache_test_blocking_calls") shouldBe Some(1.0)
  }

  private def sampleValue(registry: CollectorRegistry, name: String, labels: (String, String)*): Option[Double] =
    Option(registry.getSampleValue(name, labels.map(_._1).toArray, labels.map(_._2).toArray)).map(_.doubleValue)
}
