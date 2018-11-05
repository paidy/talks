package com.paidy.demo.par

import cats.effect.concurrent.{Deferred, Ref}
import cats.effect.syntax.concurrent._
import cats.effect.{Concurrent, Timer}
import cats.kernel.Monoid
import cats.syntax.all._
import cats.temp.par._
import cats.{Applicative, MonadError, Traverse}

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

  def collectSuccessful[F[_]: MonadError[?[_], Throwable]: Par](
      list: List[F[String]],
      ref: Ref[F, List[String]]
  ): F[List[String]] = {
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

  def firstSuccessful[F[_]: Concurrent: Par: Timer, A: Monoid](d: Deferred[F, A])(list: List[F[A]]): F[A] = {
    import cats.instances.list._
    list.parTraverse(tryComplete[F, A](d)).attempt *> d.get.timeout(2.seconds)
  }

}
