## Cats Effect

In addition, it provides a concrete implementation for all these typeclasses, namely the `IO` Monad.

```scala
import cats.effect._

val ioa: IO[Unit] = IO(println("Hello World!"))
```

This being equivalent to:

```scala
Sync[IO].delay(println("Hello World!"))
```

And some useful functions such as:

```scala
IO.fromFuture(IO {
  Future(println("Side effect!"))
})
```
