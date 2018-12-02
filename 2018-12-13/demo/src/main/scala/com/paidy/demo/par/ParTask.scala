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

package com.paidy.demo.par

import cats.effect.concurrent.{ Deferred, Ref }
import cats.effect.syntax.concurrent._
import cats.effect.{ Concurrent, Sync, Timer }
import cats.kernel.Monoid
import cats.syntax.all._
import cats.temp.par._
import cats.{ Applicative, MonadError, Traverse }

import scala.concurrent.duration._

object ParTask {

  def abstractCollectSuccessful[F[_]: MonadError[?[_], Throwable]: Par, G[_]: Traverse, A](
      gfa: G[F[A]],
      append: G[A] => A => G[A],
      ref: Ref[F, G[A]]
  ): F[G[A]] =
    gfa
      .parTraverse(_.attempt.flatTap {
        case Right(x) => ref.update(append(_)(x))
        case Left(_)  => Applicative[F].unit
      }.rethrow)
      .handleErrorWith(_ => ref.get)

  def collectSuccessful[F[_]: Par: Sync](
      list: List[F[String]]
  ): F[List[String]] =
    Ref.of[F, List[String]](List.empty).flatMap { ref =>
      import cats.instances.list._
      abstractCollectSuccessful[F, List, String](list, g => x => g :+ x, ref)
    }

  // ----------------------------

  def tryComplete[F[_]: MonadError[?[_], Throwable], A: Monoid](
      d: Deferred[F, A]
  )(fa: F[A]): F[A] =
    fa.attempt.flatMap {
      case Right(x) => d.complete(x).attempt *> x.pure[F] <* new Throwable().raiseError // short-circuit
      case Left(_)  => Monoid[A].empty.pure[F] // Ignore the errors
    }

  def firstSuccessful[F[_]: Concurrent: Par: Timer, A: Monoid](list: List[F[A]]): F[A] =
    Deferred[F, A].flatMap { d =>
      import cats.instances.list._
      list.parTraverse(tryComplete[F, A](d)).attempt *> d.get.timeout(2.seconds)
    }

}
