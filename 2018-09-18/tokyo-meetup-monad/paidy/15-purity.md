So, anything that defines `pure` and `flatMap` may be called a `Monad`?


No.
```scala
/**
 * Monad.
 *
 * Allows composition of dependent effectful functions.
 *
 * See: [[http://homepages.inf.ed.ac.uk/wadler/papers/marktoberdorf/baastad.pdf Monads for functional programming]]
 *
 * Must obey the laws defined in cats.laws.MonadLaws.
 */
@typeclass trait Monad[F[_]] extends FlatMap[F] with Applicative[F] {
```


- There's something more to `Monad`s than just exposing sequencing interfaces. 
- Any effect type `F` that implements the `Monad` interface needs to be pure values, meaning **ALL** effect, should be contained within `F`.
- This is called Monad Laws.

<!-- .element: class="fragment" data-fragment-index="1" -->  For example, using `scala.concurrent.Future` to do IO or other side effects makes it unlawful.


Why is that law important?
```scala
//printAndReturnOne: scala.concurrent.Future[Int]
for {
  first  <- printAndReturnOne
  second <- printAndReturnOne
} yield first + second
```


```scala
def printAndReturnOne: Future[Int] =
  Future { println(1); 1 }

for {
  first  <- printAndReturnOne
  second <- printAndReturnOne
} yield first + second
```


```scala
val printAndReturnOne: Future[Int] =
  Future { println(1); 1 }

for {
  first  <- printAndReturnOne
  second <- printAndReturnOne
} yield first + second
```


Hard to reason about that.


```scala
import cats.effect.IO

def printAndReturnOne: IO[Int] =
  IO { println(1); 1 }

for {
  first  <- printAndReturnOne
  second <- printAndReturnOne
} yield first + second
```
What happens?

<!-- .element: class="fragment" data-fragment-index="1" --> Nothing.


```scala
import cats.effect.IO

def printAndReturnOne: IO[Int] =
  IO { println(1); 1 }

val program = for {
  first  <- printAndReturnOne
  second <- printAndReturnOne
} yield first + second

program.unsafeRunSync()
// 1
// 1
```
