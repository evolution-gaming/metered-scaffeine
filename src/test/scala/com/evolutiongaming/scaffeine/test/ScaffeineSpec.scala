package com.evolutiongaming.scaffeine.test

import java.util.concurrent.CompletionException

import org.scalatest.{AsyncFlatSpec, Matchers}
import com.evolutiongaming.scaffeine._

import scala.concurrent.{ExecutionContext, Future}

class ScaffeineSpec extends AsyncFlatSpec with Matchers {

  it should "return existing cached value when called synchronously" in {
    cache getSync(5) shouldEqual Some("5")
  }

  it should "return existing cached value when called asynchronously" in {
    cache get(4) map { _ shouldEqual "4" }
  }

  it should "return existing cached value when called asynchronously with Option support" in {
    cache getAsync(3) map { _ shouldEqual Some("3") }
  }

  it should "return existing cached value in future when called synchronously" in {
    cache getSync(55) shouldEqual Some("55")
  }

  it should "return existing cached value in future when called asynchronously" in {
    cache get(54) map { _ shouldEqual "54" }
  }

  it should "return existing cached value in future when called asynchronously with Option support" in {
    cache getAsync(53) map { _ shouldEqual Some("53") }
  }

  it should "return None when called synchronously for non existent value" in {
    cache getSync(104) shouldEqual None
  }

  it should "return None when called asynchronously for non existent value" in {
    cache getAsync(105) map { _ shouldEqual None }
  }

  it should "return null when called asynchronously for non existent value without Option support" in {
    cache get(106) map { _ shouldEqual null }
  }
  
  it should "return None when called synchronously for a key which fails to load a value" in {
    assertThrows[CompletionException] {
      cache getSync(204)
    }
  }

  it should "return None when called asynchronously for a key which fails to load a value" in {
    cache.getAsync(205).failed map { _ shouldBe CacheException  }
  }

  it should "return null when called asynchronously for a key which fails to load a value without Option support" in {
    cache.get(206).failed map { _ shouldBe CacheException  }
  }

  object CacheException extends Exception

  private def cache = Scaffeine().asyncCache{ k: Int => println(""); k match {
    case i if i < 50              => Future.successful(Some(s"$k"))
    case i if i >= 50 && i < 100  => Future(Some(s"$k"))(ExecutionContext.global)
    case i if i >= 100 && i < 200 => Future.successful(None)
    case i if i >= 200            => Future.failed(CacheException)
  }}(ExecutionContext.global)
}
