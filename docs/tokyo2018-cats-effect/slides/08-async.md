## Asynchronous Computations

#### Async

```scala
def async[A](k: (Either[Throwable, A] => Unit) => Unit): F[A]
```

```scala
Async[IO].async <-> IO.async
```

#### Never

```scala
def never[A]: F[A] = async(_ => ())
```


## Asynchronous Computations

```scala
val iof = IO(myFuture)

val fromFuture: IO[Unit] =
  iof.flatMap { f =>
    IO.async[Unit] { cb =>
      f.onComplete{
        case Success(a) => cb(Right(a))
        case Failure(e) => cb(Left(e))
      }
    }
  }
```

```scala
val f = IO.fromFuture(iof)
```
