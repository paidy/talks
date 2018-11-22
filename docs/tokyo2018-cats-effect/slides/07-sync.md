## Synchronous Computations

#### Delay

```scala
def delay[A](thunk: => A): F[A] = suspend(pure(thunk))
```

```scala
import cats.effect.{IO, Sync}

Sync[IO].delay(println("Hey!")) <-> IO(println("Hey!"))
```

#### Suspend

```scala
def suspend[A](thunk: => F[A]): F[A]
```

```scala
val expr = IO(loop)
Sync[IO].suspend(expr) <-> IO.suspend(expr)
```
