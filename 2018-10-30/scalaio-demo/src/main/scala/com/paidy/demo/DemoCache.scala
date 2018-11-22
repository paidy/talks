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
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.paidy.demo.cache.Cache

import scala.concurrent.duration._
import scala.util.Random

object DemoCache extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    Cache
      .of[IO, Int, String](expiresIn = 5.seconds, checkOnExpirationsEvery = 500.millis)
      .flatMap { cache =>
        def loop: IO[Unit] =
          IO.sleep(1.second) >> cache.put(Random.nextInt(100), Random.nextString(5)) >> loop
        loop
      }
      .as(ExitCode.Success)

}
