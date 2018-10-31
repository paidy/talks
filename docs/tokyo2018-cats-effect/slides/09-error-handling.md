## Error Handling: Monad Error

```scala
def attempt[A](fa: F[A]): F[Either[E, A]]
def rethrow[A](fa: F[Either[E, A]]): F[A]
def handleErrorWith[A](fa: F[A])(f: E => F[A]): F[A]
def recoverWith[A](fa: F[A])(pf: PartialFunction[E, F[A]]): F[A]
```

```scala
MonadError[IO, Throwable].raiseError <-> IO.raiseError
MonadError[IO, Throwable].attempt <-> IO.attempt
```

#### Raise and Attempt

```scala
val boom: IO[String] = IO.raiseError[String](new Exception("boom"))
val safe: IO[Either[Throwable, String]] = boom.attempt
```

#### Handling errors

```scala
val keepGoing: IO[String] = boom.handleErrorWith {
  case NonFatal(e) => IO(println(e.getMessage)) *> IO.pure("Keep going ;)")
}
```
