package com.evolutiongaming.scaffeine.test

import com.evolutiongaming.concurrent.CurrentThreadExecutionContext
import com.evolutiongaming.scaffeine._
import com.github.blemale.scaffeine.Scaffeine
import io.prometheus.metrics.model.registry.PrometheusRegistry
import io.prometheus.metrics.model.snapshots.{CounterSnapshot, GaugeSnapshot, MetricSnapshot, SummarySnapshot}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._

class PrometheusV1MetricsStatsCounterSpec extends AnyFlatSpec with Matchers {

  private implicit val ec: ExecutionContext = CurrentThreadExecutionContext

  private val prefix = "cache_test"

  "PrometheusV1MetricsStatsCounter" should "register the same metrics as MetricsStatsCounter" in {
    val registry = new PrometheusRegistry()
    new PrometheusV1MetricsStatsCounter(registry, prefix)

    registry.scrape().asScala.map(_.getMetadata.getName).toSet shouldBe Set(
      "cache_test_misses",
      "cache_test_hits",
      "cache_test_evictions",
      "cache_test_blocking_calls",
      "cache_test_load_result",
      "cache_test_load_time",
      "cache_test_estimated_size",
    )
  }

  it should "count hits, misses and successful loads of a cache" in {
    val registry = new PrometheusRegistry()
    val cache = meteredCache(registry)

    cache.getBlocking(1) shouldBe Some("1")
    cache.getBlocking(1) shouldBe Some("1")

    counterValue(registry, "cache_test_misses") shouldBe Some(1.0)
    counterValue(registry, "cache_test_hits") shouldBe Some(1.0)
    counterValue(registry, "cache_test_load_result", "result" -> "success") shouldBe Some(1.0)
    counterValue(registry, "cache_test_load_result", "result" -> "failure") shouldBe None
    loadTimeCount(registry) shouldBe Some(1L)
  }

  it should "report the estimated size of a cache" in {
    val registry = new PrometheusRegistry()
    val cache = meteredCache(registry)

    cache.getBlocking(1)
    cache.getBlocking(2)
    cache.synchronous().cleanUp()

    gaugeValue(registry, "cache_test_estimated_size") shouldBe Some(2.0)
  }

  it should "count blocking calls" in {
    val registry = new PrometheusRegistry()
    val statsCounter = new PrometheusV1MetricsStatsCounter(registry, prefix)

    statsCounter.recordBlockingCall()

    counterValue(registry, "cache_test_blocking_calls") shouldBe Some(1.0)
  }

  it should "return cache statistics in a snapshot" in {
    val registry = new PrometheusRegistry()
    val statsCounter = new PrometheusV1MetricsStatsCounter(registry, prefix)

    statsCounter.recordHits(2)
    statsCounter.recordMisses(3)

    val snapshot = statsCounter.snapshot
    snapshot.hitCount() shouldBe 2L
    snapshot.missCount() shouldBe 3L
  }

  it should "not fail when a cache with the same name is created twice" in {
    val registry = new PrometheusRegistry()
    new PrometheusV1MetricsStatsCounter(registry, prefix)

    noException should be thrownBy new PrometheusV1MetricsStatsCounter(registry, prefix)
  }

  private def meteredCache(registry: PrometheusRegistry): ScalaAsyncLoadingCache[Int, String] =
    Scaffeine()
      .meteredAsyncCache[Int, String](new PrometheusV1MetricsStatsCounter(registry, prefix)) { key =>
        Future.successful(Some(key.toString))
      }

  private def counterValue(registry: PrometheusRegistry, name: String, labels: (String, String)*): Option[Double] =
    snapshot(registry, name)
      .collect { case counter: CounterSnapshot => counter.getDataPoints.asScala.toList }
      .getOrElse(Nil)
      .find(dataPoint => labels.forall { case (label, value) => dataPoint.getLabels.get(label) == value })
      .map(_.getValue)

  private def gaugeValue(registry: PrometheusRegistry, name: String): Option[Double] =
    snapshot(registry, name)
      .collect { case gauge: GaugeSnapshot => gauge.getDataPoints.asScala.toList }
      .flatMap(_.headOption)
      .map(_.getValue)

  private def loadTimeCount(registry: PrometheusRegistry): Option[Long] =
    snapshot(registry, "cache_test_load_time")
      .collect { case summary: SummarySnapshot => summary.getDataPoints.asScala.toList }
      .flatMap(_.headOption)
      .map(_.getCount)

  private def snapshot(registry: PrometheusRegistry, name: String): Option[MetricSnapshot] =
    registry.scrape().asScala.find(_.getMetadata.getName == name)
}
