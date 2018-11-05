package com.paidy.demo

import cats.effect.concurrent.Ref
import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.apply._
import cats.syntax.functor._
import com.paidy.demo.par.ParTask

import scala.concurrent.duration._

object DemoCollectSuccessful extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    program.as(ExitCode.Success)

  val program: IO[Unit] = {
    val ioa = IO.sleep(1.second) *> IO.pure("a")
    val iob = IO.pure("b")
//    val ioc = IO.pure("c")
    val iod = IO.sleep(3.seconds) *> IO.pure("d")
    val ioe = IO.sleep(2.seconds) *> IO.pure("e")

    val failure = IO.sleep(1.second) *> IO.raiseError(new Exception("boom"))

//    val list1 = List(ioa, iob, ioc, iod, ioe)
    val list1 = List(ioa, iob, failure, iod, ioe)

    Ref
      .of[IO, List[String]](List.empty)
      .flatMap { ref =>
        ParTask.collectSuccessful(list1, ref)
      }
      .flatMap { result =>
        IO(println(result))
      }
  }

}
