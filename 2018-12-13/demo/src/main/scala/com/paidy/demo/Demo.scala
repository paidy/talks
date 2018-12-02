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

import java.io.{ BufferedReader, File, FileReader }
import java.util.UUID
import java.util.stream.Collectors

import cats.effect._
import cats.effect.syntax.all._
import cats.syntax.all._
import cats.temp.par._
import cats.{ Applicative, Monad }

import scala.collection.SortedSet
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{ Failure, Random, Success }

object Demo extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    p3.as(ExitCode.Success)

  // ---------------------------------- INTERACTING WITH I/O -----------------------------------

  val prg1: IO[Unit] =
    for {
      _ <- IO(println("Enter your name:"))
      n <- IO(scala.io.StdIn.readLine)
      _ <- IO(println(s"Hello $n!"))
    } yield ()

  /*

  Enter your name:
    Gabriel
  Hello Gabriel!

   */

  trait Console[F[_]] {
    def putStrLn(str: String): F[Unit]
    def readLn: F[String]
  }

  class StdConsole[F[_]: Sync] extends Console[F] {
    def putStrLn(str: String) = Sync[F].delay(println(str))
    def readLn                = Sync[F].delay(scala.io.StdIn.readLine)
  }

  object HttpClient {
    def get: Future[String]             = Future.successful(Random.nextString(10))
    def post(str: String): Future[Unit] = Future(println(s">>> REMOTE: $str"))
  }

  class RemoteConsole[F[_]: Async] extends Console[F] {
    private def fromFuture[A](fa: F[Future[A]]): F[A] =
      fa.flatMap { future =>
        Async[F].async { cb =>
          future.onComplete {
            case Success(x) => cb(Right(x))
            case Failure(e) => cb(Left(e))
          }
        }
      }

    override def putStrLn(str: String) = fromFuture(Sync[F].delay(HttpClient.post(str)))
    override def readLn                = fromFuture(Sync[F].delay(HttpClient.get))
  }

  def gPrg[F[_]: Monad](implicit C: Console[F]): F[Unit] =
    for {
      _ <- C.putStrLn("Enter your name:")
      n <- C.readLn
      _ <- C.putStrLn(s"Hello $n!")
    } yield ()

  val gPrg2: IO[Unit] = {
    implicit val console: Console[IO] = new StdConsole[IO]
    (gPrg[IO], gPrg[IO](Monad[IO], new RemoteConsole[IO])).parTupled.void
  }

  // ---------------------------------- A MORE COMPLEX EXAMPLE -----------------------------------

  case class Band(value: String) extends AnyVal

  implicit val bandOrdering: Ordering[Band] = (x: Band, y: Band) => x.value.compareTo(y.value)

  /**
    * Note: this can be implemented in an easier way like this:
    *
    * IO(scala.io.Source.fromResource("bands.txt").getLines.toList.map(Band))
    *
    * But the idea is to illustrate that in real cases we need to safely manage resources (eg. open / close socket)
    * */
  val getBandsFromFile: IO[List[Band]] =
    IO {
      val file = new File(this.getClass.getClassLoader.getResource("bands.txt").getFile)
      new BufferedReader(new FileReader(file))
    }.flatMap { br =>
      import scala.collection.JavaConverters._
      val bands = br.lines.collect(Collectors.toList()).asScala.toList.map(Band)
      IO.pure(bands) <* IO(br.close())
    }

  def putStrLn[A](a: A): IO[Unit] = IO(println(a))

  def publishRadioChart(id: UUID, bands: SortedSet[Band]): IO[Unit] =
    putStrLn(s"Radio Chart for $id: ${bands.map(_.value).mkString(", ")}")

  def publishTvChart(id: UUID, bands: SortedSet[Band]): IO[Unit] =
    putStrLn(s"TV Chart for $id: ${bands.take(5).map(_.value).mkString(", ")}")

  def generateId: IO[UUID] = IO(UUID.randomUUID())

  def longProcess1(bands: List[Band]): IO[Unit] =
    putStrLn("Starting process 1") *> IO.sleep(3.seconds) *> putStrLn("Process 1 DONE")

  def longProcess2(bands: List[Band]): IO[Unit] =
    putStrLn("Starting process 2") *> IO.sleep(2.seconds) *> putStrLn("Process 2 DONE")

  /**
    * We have this program that reads the bands from a file, sorts them alphabetically and publishes
    * to both the Radio and TV charts.
    *
    * We are wrapping all the side-effects in IO and everything works but... can we do better? YES!
    *
    * Issues:
    * - Hard to test, highly coupled logic.
    * - We are not handling resources properly.
    * - We do not need to wait for the internal long processes to finish in order to publish the charts.
    * - Publishing both Radio and TV charts are independent actions, can be done in parallel.
    * - Errors are not being considered.
    *
    * New business requirements:
    * - We should be able to read either from file or from database.
    *
    * */
  val generateChart: IO[Unit] =
    for {
      b <- getBandsFromFile
      _ <- IO.race(longProcess1(b), longProcess2(b))
      id <- generateId
      _ <- publishRadioChart(id, SortedSet(b: _*))
      _ <- publishTvChart(id, SortedSet(b: _*))
    } yield ()

  // ----------------------------------------------------

  trait DataSource[F[_]] {
    def bands: F[List[Band]]
  }

  trait IdGen[F[_]] {
    def generate: F[UUID]
  }

  trait InternalProcess[F[_]] {
    def process(bands: SortedSet[Band]): F[Unit]
  }

  trait RadioChart[F[_]] {
    def publish(id: UUID, bands: SortedSet[Band]): F[Unit]
  }

  trait TvChart[F[_]] {
    def publish(id: UUID, bands: SortedSet[Band]): F[Unit]
  }

  class Charts[F[_]: Monad](
      source: DataSource[F],
      idGen: IdGen[F],
      internal: InternalProcess[F],
      radioChart: RadioChart[F],
      tvChart: TvChart[F]
  ) {

    def generate: F[Unit] =
      for {
        b <- source.bands.map(xs => SortedSet(xs: _*))
        _ <- internal.process(b)
        id <- idGen.generate
        _ <- radioChart.publish(id, b)
        _ <- tvChart.publish(id, b)
      } yield ()

  }

  class ChartsV2[F[_]: Concurrent: Par](
      source: DataSource[F],
      idGen: IdGen[F],
      internal: InternalProcess[F],
      radioChart: RadioChart[F],
      tvChart: TvChart[F]
  ) {

    def generate: F[Unit] =
      for {
        b <- source.bands.map(xs => SortedSet(xs: _*))
        _ <- internal.process(b).start
        id <- idGen.generate
        _ <- (radioChart.publish(id, b), tvChart.publish(id, b)).parTupled.void
      } yield ()

  }

  // ----- interpreters ------

  class FileDataSource[F[_]](implicit F: Sync[F]) extends DataSource[F] {
    override def bands: F[List[Band]] = {
      val acquire = F.delay {
        val file = new File(getClass.getClassLoader.getResource("bands.txt").getFile)
        new BufferedReader(new FileReader(file))
      }

      acquire.bracket { br =>
        import scala.collection.JavaConverters._
        br.lines.collect(Collectors.toList()).asScala.toList.map(Band).pure[F]
      }(br => F.delay(br.close()))
    }
  }

  // Let's pretend this data actually comes from a DB
  class DBDataSource[F[_]: Applicative] extends DataSource[F] {
    override def bands: F[List[Band]] =
      List("AC/DC", "Iron Maiden", "Metallica").map(Band).pure[F]
  }

  class MemRadioChart[F[_]: Sync] extends RadioChart[F] {
    override def publish(id: UUID, bands: SortedSet[Band]) =
      Sync[F].delay(println(s"Radio Chart: ${bands.map(_.value).mkString(", ")}"))
  }

  class MemTvChart[F[_]: Sync] extends TvChart[F] {
    override def publish(id: UUID, bands: SortedSet[Band]) =
      Sync[F].delay(println(s"TV Chart: ${bands.map(_.value).take(3).mkString(", ")}"))
  }

  class LiveIdGen[F[_]: Sync] extends IdGen[F] {
    override def generate: F[UUID] = Sync[F].delay(UUID.randomUUID())
  }

  class LiveInternalProcess[F[_]](implicit F: Concurrent[F], T: Timer[F]) extends InternalProcess[F] {
    private def putStrLn(str: String): F[Unit] = F.delay(println(str))

    def longProcess1(bands: SortedSet[Band]): F[Unit] =
      putStrLn("Starting process 1") *> T.sleep(3.seconds) *> putStrLn("Process 1 DONE")

    def longProcess2(bands: SortedSet[Band]): F[Unit] =
      putStrLn("Starting process 2") *> T.sleep(2.seconds) *> putStrLn("Process 2 DONE")

    override def process(bands: SortedSet[Band]): F[Unit] =
      F.race(longProcess1(bands), longProcess2(bands)).void
//      F.raiseError(new Exception("boom"))
  }

  val source: DataSource[IO]        = new FileDataSource[IO]
  val idGen: IdGen[IO]              = new LiveIdGen[IO]
  val internal: InternalProcess[IO] = new LiveInternalProcess[IO]
  val radioChart: RadioChart[IO]    = new MemRadioChart[IO]
  val tvChart: TvChart[IO]          = new MemTvChart[IO]

  val charts = new Charts[IO](source, idGen, internal, radioChart, tvChart)

  val chartsV2 = new ChartsV2[IO](source, idGen, internal, radioChart, tvChart)

  // ---------------------------------- ERROR HANDLING -----------------------------------

  // logging the error in case of failure
  val p1 = charts.generate.attempt.flatMap {
    case Right(_) => IO.unit
    case Left(e)  => putStrLn(s"Failed to generate charts: ${e.getMessage}")
  }

  val p2 = charts.generate.handleErrorWith { e =>
    putStrLn(s"Failed to generate charts: ${e.getMessage}")
  }

  // logging error and retry
  lazy val p3 = {
    def resilient(retries: Int): IO[Unit] =
      charts.generate.handleErrorWith { e =>
        putStrLn(s"Failed to generate charts: ${e.getMessage}. Retries left: $retries") >> {
          if (retries > 0) IO.sleep(5.seconds) >> resilient(retries - 1)
          else putStrLn("Program failed after many retries")
        }
      }

    resilient(3)
  }

}
