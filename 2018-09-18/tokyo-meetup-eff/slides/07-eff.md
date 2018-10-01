## The Eff Monad

#### History

Introduced in the paper: [Freer monads, more extensible effects](http://okmij.org/ftp/Haskell/extensible/more.pdf)

Introduced to scala by: [atnos-org/eff](https://github.com/atnos-org/eff)


#### The problems it solves

- Combining multiple effects (which can be painful as we've seen)

- Declaratively specifying what your program _does_
  - By separating your program's _description_ from its _interpretation_
  - With effect handers


### How Eff works

You define a type-level list of "effect constructors"

```scala
import org.atnos.eff._
import org.atnos.eff.all._
import org.atnos.eff.syntax.all._
import monix.eval.Task // we use Task to wrap Futures

type EitherError[A] = Either[Error, A] // A monad is a single arity type constructor

type Stack = Fx.fx2[EitherError, Task] // Your effect stack

type _eitherError[R] = EitherError |= R // EitherError is a member of R
```
<!-- .element: class="fragment" data-fragment-index="1" -->


### Using Eff

```scala
import org.atnos.eff.addon.monix.task._

def getUser[R : _eitherError : _task](email: String): Eff[R, Either[Error, User]] = for {
  email <- fromEither(isValidEmail(email))
  user <- fromTask(Task.fromFuture(getUserByEmail(email)))
} yield user
```
<!-- .element: class="fragment" data-fragment-index="1" -->

```scala
val prog = getUser[Stack]("mary@neopets.com") // Eff.ImpureAp(...) - description

val task = prog.runEither.runAsync // Task[Either[Error, User]] - interpretation

Await.result(task.runAsync, 10.seconds) // Right(User("mary@neopets")) - result
```
<!-- .element: class="fragment fade-up" data-fragment-index="2" -->


#### Wat.
- Where did `fromEither` etc. come from?

- Eff provides these functions to lift your monad into `Eff`

- There are similar lifting functions for many commonly used types:
  - State
  - Reader
  - cats.effect.IO
  - etc.

- You can define your own effects!


### Easy mocking
- We can pass parameters to our interpreters

- A notorious problem in testing is how do you pass time into your app?
  - Using `Time.now()` in your app prevents you from mocking time in tests

```scala
import io.pjan.effx.time._

type Stack = Fx.fx2[EitherError, Time]

def emailAndTime[R : _eitherError : _time](email: String): Eff[R, Either[Error, (String, OffsetDateTime)]] = for {
  email <- fromEither(isValidEmail(email))
  time <- timestamp
} yield (email, time)

val prog = getUser[Stack]("mary@neopets.com")
```
<!-- .element: class="fragment" data-fragment-index="1" -->


### Defining time
Define what _time_ means in your app

In production
<!-- .element: class="fragment" data-fragment-index="1" -->
```scala
val liveResult = prog.runEither.runTime(Clock.live) // Pass the interpreter a live Clock
```
<!-- .element: class="fragment" data-fragment-index="1" -->

In tests
<!-- .element: class="fragment" data-fragment-index="2" -->

```scala
val newYearTime = OffsetDateTime.of(2018, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)

val frozenResult = prog.runEither.runTime(Clock.frozen(newYearTime)) // Pass it a frozen Clock
```
<!-- .element: class="fragment" data-fragment-index="2" -->

