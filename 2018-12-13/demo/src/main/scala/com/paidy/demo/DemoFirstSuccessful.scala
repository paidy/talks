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

import cats.effect.{ ExitCode, IO, IOApp }
import cats.syntax.apply._
import cats.syntax.functor._
import com.paidy.demo.par.ParTask

import scala.concurrent.duration._

object DemoFirstSuccessful extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    program.as(ExitCode.Success)

  import Demo._

  val program: IO[Unit] = {
    val io1 = IO.sleep(1.second) *> IO.raiseError[String](new Exception("error 1"))
    val io2 = IO.sleep(1.1.seconds) *> IO.raiseError[String](new Exception("error 2"))
    val io3 = IO.sleep(1.2.seconds) *> IO.pure("success")
    val io4 = IO.sleep(1.4.seconds) *> IO.pure("slower success")

    import cats.instances.string.catsKernelStdMonoidForString

//    val tasks = List(io1, io2) // It will time out because there's no successful value
    val tasks = List(io1, io2, io3, io4)

    ParTask.firstSuccessful(tasks).flatMap(putStrLn)
  }

}
