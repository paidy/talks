## Re-thinking our last code

```scala
val charts = new Charts[IO](source, idGen, internal, radioChart, tvChart)

charts.generate.unsafeRunSync()
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
val chartsRetry = {
  def resilient(retries: Int): IO[Unit] =
    charts.generate.handleErrorWith { e =>
      putStrLn(s"Failed: ${e.getMessage}. Retries left: $retries") >> {
        if (retries > 0) IO.sleep(5.seconds) >> resilient(retries - 1)
        else putStrLn("Program failed after many retries")
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
