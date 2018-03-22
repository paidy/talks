## Error Handling: Monad Error

Http4s makes use of the instance provided by `Cats Effect`.

```scala
import cats.effect.IO

val boom: IO[String] = IO.raiseError[String](new Exception("boom"))
val safe: IO[Either[Throwable, String]] = boom.attempt
```

Equivalent to:

```scala
import cats.MonadError

val M = MonadError[IO, Throwable]

val boom2: IO[String] = M.raiseError[String](new Exception("boom"))
val safe2: IO[Either[Throwable, String]] = M.attempt(boom2)
```

Handling errors:

```scala
import cats.syntax.all._

val keepGoing: IO[String] = boom.handleErrorWith {
  case e: NonFatal => IO(println(e.getMessage)) *> IO.pure("Keep going ;)")
}
```
