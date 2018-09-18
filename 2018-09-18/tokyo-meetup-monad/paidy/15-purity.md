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

<!-- .element: class="fragment" data-fragment-index="1" --> The values describing business logic should only **DESCRIBE** the operation, not **DO** anything

<!-- .element: class="fragment" data-fragment-index="1" --> Referential Transparency

<!-- .element: class="fragment" data-fragment-index="2" --> Whole point is to decouple logic with an execution strategy


<!-- .element: class="fragment" data-fragment-index="3" --> Any `Monad` implementation needs to pass certain tests.

<!-- .element: class="fragment" data-fragment-index="3" --> Monad Laws


For example, using `scala.concurrent.Future` to do IO or other side effects breaks referential transparency.

<!-- .element: class="fragment" data-fragment-index="5" --> It is not possible to even test lawfulness in such cases.


Why is RT and law important?
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
