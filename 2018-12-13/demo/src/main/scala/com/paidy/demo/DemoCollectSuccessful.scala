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

object DemoCollectSuccessful extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    program.as(ExitCode.Success)

  import Demo._

  val program: IO[Unit] = {
    val ioa = IO.sleep(1.second) *> IO.pure("a")
    val iob = IO.pure("b")
//    val ioc = IO.pure("c")
    val iod = IO.sleep(3.seconds) *> IO.pure("d")
    val ioe = IO.sleep(2.seconds) *> IO.pure("e")

    val failure = IO.sleep(1.second) *> IO.raiseError(new Exception("boom"))

//    val list1 = List(ioa, iob, ioc, iod, ioe)
    val list1 = List(ioa, iob, failure, iod, ioe)

    ParTask.collectSuccessful(list1).flatMap(putStrLn)
  }

}
