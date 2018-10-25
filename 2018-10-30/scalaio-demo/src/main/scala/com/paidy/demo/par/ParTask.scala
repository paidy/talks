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
package par

import cats.Traverse
import cats.effect.Concurrent
import cats.effect.concurrent.Deferred
import cats.effect.syntax.concurrent._
import cats.syntax.all._
import cats.temp.par._

object ParTask {

  /**
    * Runs N computations concurrently and either waits for all of them to complete or just return as
    * soon as any of them fail while keeping the remaining computations running in the background
    * just for their effects.
    * */
  def parFailFast[F[_]: Concurrent: Logger: Par, G[_]: Traverse, A](gfa: G[F[A]]): F[Either[Throwable, G[A]]] = {
    val handler: PartialFunction[Throwable, F[A]] = {
      case e => Logger[F].info(s"parFailFast-handler: $e") *> e.raiseError
    }
    parFailFastWithHandler[F, G, A](gfa, handler)
  }

  private[par] def parFailFastWithHandler[F[_]: Concurrent: Par, G[_]: Traverse, A](
      gfa: G[F[A]],
      handler: PartialFunction[Throwable, F[A]]
  ): F[Either[Throwable, G[A]]] =
    gfa.parTraverse { fa =>
      Deferred[F, Either[Throwable, A]].flatMap { d =>
        fa.recoverWith(handler).attempt.flatMap(d.complete).start *> d.get.rethrow
      }
    }.attempt

}
