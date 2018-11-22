/*
 * Copyright (c) 2018 Paidy Inc
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.paidy.demo
package cache

import java.time.OffsetDateTime

import cats.effect.concurrent.Ref
import cats.effect.{ Clock, ContextShift, IO, Timer }
import org.scalatest.AsyncFunSuite

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class CacheSpec extends AsyncFunSuite {

  def testClock(ref: Ref[IO, Long]): Clock[IO] =
    new Clock[IO] {
      def realTime(unit: TimeUnit)  = ref.get.map(MILLISECONDS.convert(_, unit))
      def monotonic(unit: TimeUnit) = realTime(unit)
    }

  def testTimer(testClock: Clock[IO]): Timer[IO] =
    new Timer[IO] {
      override def sleep(duration: FiniteDuration) = IO.timer(ec).sleep(duration)
      override def clock                           = testClock
    }

  private val ec: ExecutionContext  = ExecutionContext.global
  implicit val cs: ContextShift[IO] = IO.contextShift(ec)

  private val cacheKeyExpiration = 12.hours
  private val expDelay           = 100.millis // Give it time to check on the expiration process

  // Needed since in the Cache implementation we take the current clock time minus the expiration time (12 hours here)
  private def forwardClockToReachExpirationTime(ref: Ref[IO, Long]): IO[Unit] =
    ref.update(_ + cacheKeyExpiration.toMillis)

  test("[Cache] Expires keys") {
    IOAssertion {
      for {
        now <- IO(OffsetDateTime.now(Cache.CacheOffset).toInstant.toEpochMilli)
        ref <- Ref.of[IO, Long](now)
        implicit0(timer: Timer[IO]) = testTimer(testClock(ref))
        cache <- Cache.of[IO, Int, String](expiresIn = cacheKeyExpiration, checkOnExpirationsEvery = 80.millis)
        _ <- cache.put(1, "foo")
        _ <- ref.update(_ + 5.hours.toMillis)
        _ <- cache.put(2, "bar")
        _ <- timer.sleep(expDelay)
        a1 <- cache.get(1)
        b1 <- cache.get(2)
        _ <- IO {
              assert(a1.contains("foo"))
              assert(b1.contains("bar"))
            }
        _ <- forwardClockToReachExpirationTime(ref)
        _ <- ref.update(_ + 7.hours.toMillis) // expiration timing reached
        _ <- timer.sleep(expDelay)
        a2 <- cache.get(1)
        b2 <- cache.get(2)
        rs <- IO {
               assert(a2.isEmpty) // not here
               assert(b2.contains("bar"))
             }
      } yield rs
    }
  }

  test("[Cache] Resets expiration") {
    IOAssertion {
      for {
        now <- IO(OffsetDateTime.now(Cache.CacheOffset).toInstant.toEpochMilli)
        ref <- Ref.of[IO, Long](now)
        implicit0(timer: Timer[IO]) = testTimer(testClock(ref))
        cache <- Cache.of[IO, Int, String](expiresIn = cacheKeyExpiration, checkOnExpirationsEvery = 80.millis)
        _ <- cache.put(1, "foo")
        _ <- ref.update(_ + 5.hours.toMillis)
        _ <- timer.sleep(expDelay)
        a1 <- cache.get(1)
        _ <- IO {
              assert(a1.contains("foo"))
            }
        _ <- cache.put(1, "bar")
        _ <- ref.update(_ + 7.hours.toMillis) // expiration time reached for first timestamp
        _ <- forwardClockToReachExpirationTime(ref)
        _ <- timer.sleep(expDelay)
        a2 <- cache.get(1)
        _ <- IO {
              assert(a2.contains("bar"))
            }
        _ <- ref.update(_ + 5.hours.toMillis) // expiration time reached for last timestamp
        _ <- timer.sleep(expDelay)
        a3 <- cache.get(1)
        rs <- IO {
               assert(a3.isEmpty)
             }
      } yield rs
    }
  }

}
