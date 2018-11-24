##### `map` in terms of `flatMap` and `pure` 

In Scala, fields or methods inside traits can have concrete implementations.

It turns out `map` can be defined only with `flatMap` and `pure`, making implementation of the `Monad` trait easier.

```scala
trait Monad[F[_]] {
  def flatMap(fa: F[A])(f: A => F[B]): F[B]
  def pure(a: A): F[A]
  
  //No need to reimplement
  def map(fa: F[A])(f: A => B): F[B] =
    fa.flatMap(a => f(a).pure)
}
```