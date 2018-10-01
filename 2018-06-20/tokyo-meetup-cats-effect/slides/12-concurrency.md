## Concurrency

#### Start

```scala
def start[A](fa: F[A]): F[Fiber[F, A]
```

```scala
trait Fiber[F[_], A] {
  def cancel: F[Unit]
  def join: F[A]
}
```

#### Non-deterministic / Concurrent execution

```scala
for {
  fb1 <- ioa.start
  fb2 <- iob.start
  _   <- fb2.cancel
  rs  <- fb1.join
} yield rs
```


## Concurrency

```scala
def race[A, B](fa: F[A], fb: F[B]): F[Either[A, B]]
def racePair[A,B](fa: F[A], fb: F[B]): F[Either[(A, Fiber[F, B]), (Fiber[F, A], B)]]
```

```scala
Concurrent[IO].race <-> IO.race
```

#### Can you guess the result?

```scala
val ioa = IO.sleep(1.second) *> IO(println("A"))
val iob = IO(println("B"))

IO.race(ioa, iob)
IO.racePair(ioa, iob)
```


#### Can you guess the result?

```scala
val ioa = IO.sleep(1.second) *> IO(println("A"))
val iob = IO(println("B"))

IO.race(ioa, iob)       // winner is always B, A gets canceled
IO.racePair(ioa, iob)   // gets a Fiber to join / cancel the loser
```


## Concurrency

#### Timeout

```scala
case class TimeOutException(message: String) extends Exception(message)

def timeout[A](ioa: IO[A], after: FiniteDuration): IO[A] = {
  IO.race(ioa, IO.sleep(after)).flatMap {
    case Left(x)  => IO.pure(x)
    case Right(_) => IO.raiseError(TimeOutException(s"Timeout after $after"))
  }
}

val delayedIO = IO.sleep(3.seconds) *> IO(println("delayed io!"))

timeout(delayedIO, 1.second) // TimeOutException: Timeout after 1 second!
```

```scala
def timeout(duration: FiniteDuration)(implicit timer: Timer[IO]): IO[A]
```
