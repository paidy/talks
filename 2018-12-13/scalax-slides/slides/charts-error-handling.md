## Re-thinking our last code

```scala
val charts = new Charts[IO](source, idGen, internal, radioChart, tvChart)

charts.generate.unsafeRunSync() // HINT: Use `IOApp` instead
```

<!-- .element: class="fragment" data-fragment-index="1" --> What about error handling?


## Error Handling

#### Logging errors in case of failure

```scala
charts.generate.attempt.flatMap {
  case Right(_) => IO.unit
  case Left(e)  => putStrLn(s"Failed to generate charts: ${e.getMessage}")
}
```

```scala
charts.generate.handleErrorWith { e =>
  putStrLn(s"Failed to generate charts: ${e.getMessage}")
}
```

#### Applicative Error

```scala
def attempt[A](fa: F[A]): F[Either[E, A]]
def handleErrorWith[A](fa: F[A])(f: E => F[A]): F[A]
```


## Error Handling

#### Logging errors and retry

```scala
def chartsRetry[F[_]: Logger: MonadError[?[_], Throwable]: Timer](
    charts: Charts[F]
): F[Unit] = {
  def resilient(retries: Int): F[Unit] =
    charts.generate.handleErrorWith { e =>
      Logger[F].info(s"Failed: ${e.getMessage}. Retries left: $retries") >> {
        if (retries > 0) Timer[F].sleep(5.seconds) >> resilient(retries - 1)
        else Logger[F].error("Program failed after many retries")
      }
    }

  resilient(3)
}
```

#### Possible outcome

```
Failed: boom. Retries left: 3
Failed: boom. Retries left: 2
Failed: boom. Retries left: 1
Failed: boom. Retries left: 0
Program failed after many retries
```


## Retry Combinators

- <!-- .element: class="fragment" data-fragment-index="1" --> Build it yourself using `Timer[F]`
- <!-- .element: class="fragment" data-fragment-index="2" --> Look into [cats-retry](https://cb372.github.io/cats-retry/) and its retry policies.
- <!-- .element: class="fragment" data-fragment-index="3" --> Use the high-level [fs2](http://fs2.io) combinators such as `retry`, `attempts`, `awakeEvery`, etc

