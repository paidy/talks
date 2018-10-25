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
import cats.instances.list._
import cats.syntax.apply._
import cats.syntax.functor._
import com.paidy.demo.par.ParTask

import scala.concurrent.duration._

object DemoParTask extends IOApp {

  def ioa(implicit L: Logger[IO]): IO[String] =
    L.info("START >> ioa") *> IO.sleep(1.second) *> IO.raiseError(new Exception("ioa failed")) //IO.pure("a") <* L.info("DONE >> ioa")
  def iob(implicit L: Logger[IO]): IO[String] =
    L.info("START >> iob") *> IO.sleep(5.seconds) *> IO.pure("b") <* L.info("DONE >> iob")
  def ioc(implicit L: Logger[IO]): IO[String] =
    L.info("START >> ioc") *> IO.sleep(3.seconds) *> IO.pure("c") <* L.info("DONE >> ioc")

  // When all the computations are successful it is the same as using `parSequence` instead
  override def run(args: List[String]): IO[ExitCode] =
    ParTask
      .parFailFast(List(ioa, iob, ioc))
      .flatMap(x => Logger[IO].info(s"RESULT >> $x"))
      .flatMap(_ => IO.sleep(5.seconds))
      .as(ExitCode.Success)

}
