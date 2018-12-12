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

package com.paidy.demo.cache

import java.time.{ OffsetDateTime, ZoneOffset }

import cats.Monad
import cats.effect.concurrent.Ref
import cats.effect.syntax.concurrent._
import cats.effect.{ Clock, Concurrent, Timer }
import cats.syntax.flatMap._
import cats.syntax.functor._

import scala.concurrent.duration._

trait Cache[F[_], K, V] {
  def get(key: K): F[Option[V]]
  def put(key: K, value: V): F[Unit]
}

/**
  * A Ref-based Cache with for support for key expiration based on timestamped values.
  * */
private[cache] class RefCache[F[_]: Clock: Monad, K, V](
    state: Ref[F, Map[K, (OffsetDateTime, V)]],
    expiresIn: FiniteDuration
) extends Cache[F, K, V] {

  import Cache._

  def get(key: K): F[Option[V]] =
    state.get.map(_.get(key).map { case (_, v) => v })

  def put(key: K, value: V): F[Unit] =
    DateTime[F](CacheOffset).flatMap { now =>
      state.update(_.updated(key, now.plusNanos(expiresIn.toNanos) -> value))
    }

}

object Cache {

  private[cache] val CacheOffset = ZoneOffset.UTC

  /**
    * Creates a Ref-based Cache with a single key-expiration process running in the background.
    *
    * @param expiresIn: the expiration time of every key-value in the Cache.
    * @param checkOnExpirationsEvery: how often the expiration process should check for expired keys.
    *
    * @return an `[F[Cache[F, K, V]]` that will create a Cache with key-expiration support when evaluated.
    * */
  def of[F[_]: Clock: Concurrent: Timer, K, V](
      expiresIn: FiniteDuration,
      checkOnExpirationsEvery: FiniteDuration
  ): F[Cache[F, K, V]] = {
    def runExpiration(state: Ref[F, Map[K, (OffsetDateTime, V)]]): F[Unit] = {
      val process =
        DateTime[F](CacheOffset).flatMap { now =>
          state.get
            .map { data =>
              data.filter {
                case (_, (exp, _)) => exp.isAfter(now.minusNanos(expiresIn.toNanos))
              }
            }
            .flatTap(state.set)
        }

      Timer[F].sleep(checkOnExpirationsEvery) >> process >> runExpiration(state)
    }

    Ref
      .of[F, Map[K, (OffsetDateTime, V)]](Map.empty)
      .flatTap(runExpiration(_).start.void)
      .map(ref => new RefCache[F, K, V](ref, expiresIn))
  }

}
