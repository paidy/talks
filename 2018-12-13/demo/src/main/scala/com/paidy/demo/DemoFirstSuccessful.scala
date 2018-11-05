package com.paidy.demo

import cats.effect.concurrent.Deferred
import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.apply._
import cats.syntax.functor._
import com.paidy.demo.par.ParTask

import scala.concurrent.duration._

object DemoFirstSuccessful extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    program.as(ExitCode.Success)

  val program: IO[Unit] = {
    val io1 = IO.sleep(1.second) *> IO.raiseError[String](new Exception("error 1"))
    val io2 = IO.sleep(1.1.seconds) *> IO.raiseError[String](new Exception("error 2"))
    val io3 = IO.sleep(1.2.seconds) *> IO.pure("success")
    val io4 = IO.sleep(1.4.seconds) *> IO.pure("slower success")

    import cats.instances.string.catsKernelStdMonoidForString

//    val tasks = List(io1, io2) // It will time out because there's no successful value
    val tasks = List(io1, io2, io3, io4)

    Deferred[IO, String]
      .flatMap { promise =>
        ParTask.firstSuccessful(promise)(tasks)
      }
      .flatMap { result =>
        IO(println(result))
      }
  }

}
