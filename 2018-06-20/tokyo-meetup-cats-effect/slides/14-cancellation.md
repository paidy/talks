## Cancellation

```scala
trait Concurrent[F[_]] extends Async[F] {
  def cancelable[A](k: (Either[Throwable, A] => Unit) => IO[Unit]): F[A]
}
```

```scala
trait Bracket[F[_], E] extends MonadError[F, E] {
  def uncancelable[A](fa: F[A]): F[A]
}
```

#### Cancelable

```scala
val sc: ScheduledExecutorService = Executors.newScheduledThreadPool(2)

def cioSleep(d: FiniteDuration) = IO.cancelable[Unit] { cb =>
  val runnable: Runnable = () => { cb(Right(())) }
  val task = sc.schedule(runnable, d.length, d.unit)

  putStrLn("Triggering cancellation") *> IO(task.cancel(false))
}

val sleep3s      = cioSleep(3.seconds) *> putStrLn("not today")
val cancelableIO = sleep3s.runCancelable(_ => putStrLn("done"))

cancelableIO.flatMap(token => IO.sleep(1.second) *> token)
```


## Cancellation

#### Uncancelable

```scala
val nope = IO.sleep(10.seconds).uncancelable.runCancelable(_ => IO.unit)
nope.flatMap(token => IO.sleep(1.second) *> token)
```

#### On Cancel Raise Error

```scala
val fa = putStrLn("infinite") *> IO.never

fa.onCancelRaiseError(new Exception("Process Cancelled!"))
  .handleErrorWith(e => putStrLn(e.getMessage))
  .runCancelable(_ => putStrLn("done"))
  .flatMap(token => IO.sleep(2.seconds) *> token *> IO.sleep(100.millis))
```

```scala
IO.onCancelRaiseError <-> acquire.bracketCase(use) {
                            case (_, ExitCase.Canceled) => IO.raiseError
                          }
 ```


## Cancellation

#### Cancel Boundary

```scala
def cancelableLoop(acc: Int): IO[Unit] =
  IO.suspend {
    val next  = cancelableLoop(acc + 1)
    val sleep = IO.shift *> IO(Thread.sleep(100)) // IO.sleep is cancelable

    if (acc % 10 == 0) {
      putStrLn(s"#$acc >> Checking cancellation status") *> IO.cancelBoundary *> next
    } else {
      putStrLn(s"#$acc") *> sleep *> next
    }
  }

val ioa = cancelableLoop(1).runCancelable(_ => putStrLn("done"))
ioa.flatMap(token => IO.sleep(1.second) *> token <* IO.sleep(1.second))
```


## Cancellation

#### Takeaways

- <!-- .element: class="fragment" data-fragment-index="1" --> **IO[IO[Unit]]** represents a task that when run it will start the evaluation of the effects giving you back a cancellation token of type **IO[Unit]**.
- <!-- .element: class="fragment" data-fragment-index="2" --> **Fiber[IO, Unit]** gives you control over an effect to either cancel it or wait for its completion.
- <!-- .element: class="fragment" data-fragment-index="3" --> **uncancelable** changes the nature of any `cancelable` task.
- <!-- .element: class="fragment" data-fragment-index="4" --> **cancelBoundary** adds a "cancellation status check" step to any computation, useful in loops.
- <!-- .element: class="fragment" data-fragment-index="5" --> **onCancelRaiseError** forces possibly non-terminating tasks to end by raising the given error.
