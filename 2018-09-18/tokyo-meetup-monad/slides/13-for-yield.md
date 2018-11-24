##### for-yield

Special syntax for `map` and `flatMap`

```scala
def echo[F[_]: Monad](console: ConsoleIO[F]): F[String] = 
  for {
    input <- console.readLine()
    appended = input + "monad"
    result <- if (appended == '#')
                console.printLine(result).map(_ => result)
              else 
                appended.pure
  } yield result
```
