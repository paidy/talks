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

import cats.Applicative
import cats.effect.IO
import cats.effect.concurrent.Ref
import cats.syntax.all._
import com.paidy.demo.Demo.{ gPrg, Console }
import org.scalatest.AsyncFunSuite

class ConsoleSpec extends AsyncFunSuite {

  class TestConsole[F[_]: Applicative](state: Ref[F, List[String]]) extends Console[F] {
    override def putStrLn(str: String): F[Unit] = state.update(_ :+ str)
    override def readLn: F[String]              = "test".pure[F]
  }

  test("Console") {
    val test =
      for {
        state <- Ref.of[IO, List[String]](List.empty[String])
        implicit0(c: Console[IO]) = new TestConsole[IO](state)
        _ <- gPrg[IO]
        st <- state.get
        as <- IO { assert(st == List("Enter your name:", "Hello test!")) }
      } yield as

    test.unsafeToFuture()
  }

}
