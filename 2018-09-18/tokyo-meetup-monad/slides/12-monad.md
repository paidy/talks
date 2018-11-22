We now have an interface called Monad.

```scala
trait Monad[F[_]] {
  def map(a: F[A])(f: A => B): F[B]
  def flatMap(a: F[A])(f: A => F[B]): F[B]
  def pure(a: A): F[A]
}
```

Monad is just an interface for generic types specifically allowing chaining of effectful operations.