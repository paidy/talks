## Interacting with I/O

#### Consider this program

```scala
val program: IO[Unit] =
  for {
    _ <- IO(println("Enter your name:"))
    n <- IO(scala.io.StdIn.readLine)
    _ <- IO(println(s"Hello $n!"))
  } yield ()
```

#### A possible interaction

```
> Enter your name:
    Gabriel
> Hello Gabriel!
```


## Interacting with I/O

#### Tagless Final

```scala
trait Console[F[_]] {
  def putStrLn(str: String): F[Unit]
  def readLn: F[String]
}
```

```scala
def program[F[_]: Monad](implicit C: Console[F]): F[Unit] =
  for {
    _ <- C.putStrLn("Enter your name:")
    n <- C.readLn
    _ <- C.putStrLn(s"Hello $n!")
  } yield ()
```


## Interacting with I/O

#### Standard I/O Console

```scala
class StdConsole[F[_]: Sync] extends Console[F] {
  def putStrLn(str: String) = Sync[F].delay(println(str))
  def readLn = Sync[F].delay(scala.io.StdIn.readLine)
}
```

#### Remote Console

```scala
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

  def putStrLn(str: String) = fromFuture(Sync[F].delay(HttpClient.post(str)))
  def readLn = fromFuture(Sync[F].delay(HttpClient.get))
}
```


## Interacting with I/O

#### Test Console

```scala
class TestConsole[F[_]: Applicative](state: Ref[F, List[String]]) extends Console[F] {
  def putStrLn(str: String): F[Unit] = state.update(_ :+ str)
  def readLn: F[String]              = "test".pure[F]
}
```

```scala
test("Console") {
  val spec =
    for {
      state <- Ref.of[IO, List[String]](List.empty[String])
      implicit0(c: Console[IO]) = new TestConsole[IO](state)
      _ <- program[IO]
      st <- state.get
      as <- IO { assert(st == List("Enter your name:", "Hello test!")) }
    } yield as

  spec.unsafeToFuture()
}
```
