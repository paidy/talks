## Par Traverse FTW!

#### Collect Successful

Par traverse until first failure and return successful computations

```scala
val ioa = IO.sleep(1.second) *> IO.pure("a")
val iob = IO.pure("b")
val ioc = IO.pure("c")
val iod = IO.sleep(3.seconds) *> IO.pure("d")
val ioe = IO.sleep(2.seconds) *> IO.pure("e")

val list = List(ioa, iob, ioc, iod, ioe)

for {
  ref <- Ref.of[IO, List[String]](List.empty)
  rs  <- TaskUtil.collectSuccessful(list1, ref)
  _   <- putStrLn(rs.toString)
} yield ()
```

```
res1: List[String] = List(a, b, c, d, e)
```


#### Collect Successful

Par traverse until first failure and return successful computations

```scala
val ioa = IO.sleep(1.second) *> IO.pure("a")
val iob = IO.pure("b")
val ioc = IO.sleep(1.second) *> IO.raiseError(new Exception("boom"))
val iod = IO.sleep(3.seconds) *> IO.pure("d")
val ioe = IO.sleep(2.seconds) *> IO.pure("e")

val list = List(ioa, iob, ioc, iod, ioe)

for {
  ref <- Ref.of[IO, List[String]](List.empty)
  rs  <- TaskUtil.collectSuccessful(list1, ref)
  _   <- putStrLn(rs.toString)
} yield ()
```

```
res1: List[String] = List(b, a)
```


#### Collect Successful

The implementation:

```scala
def generic[F[_]: MonadError[?[_], Throwable]: Par, G[_]: Traverse, A](
    gfa: G[F[A]],
    append: G[A] => A => G[A],
    ref: Ref[F, G[A]]
): F[G[A]] =
  gfa
    .parTraverse(_.attempt.flatTap {
      case Right(x) => ref.update(append(_)(x))
      case Left(_)  => Applicative[F].unit
    }.rethrow)
    .handleErrorWith(_ => ref.get)

def collectSuccessful[F[_]: MonadError[?[_], Throwable]: Par](
    list: List[F[String]],
    ref: Ref[F, List[String]]
): F[List[String]] = {
  import cats.instances.list._
  generic[F, List, String](list, g => x => g :+ x, ref)
}
```


#### First successful computation

Par traverse until first successful computation and return or timeout

```scala
val io1 = IO.sleep(1.second) *> IO.raiseError[String](new Exception("error 1"))
val io2 = IO.sleep(1.1.seconds) *> IO.raiseError[String](new Exception("error 2"))
val io3 = IO.sleep(1.2.seconds) *> IO.pure("success")
val io4 = IO.sleep(1.4.seconds) *> IO.pure("slower success")

val tasks = List(io1, io2, io3, io4)

for {
  promise <- Deferred[IO, String]
  result  <- TaskUtil.firstSuccessful(promise)(tasks)
  _       <- putStrLn(result.toString)
} yield ()
```

```
res1: String = success
```


#### First successful computation

Par traverse until first successful computation and return or timeout

```scala
val io1 = IO.sleep(1.second) *> IO.raiseError[String](new Exception("error 1"))
val io2 = IO.sleep(1.1.seconds) *> IO.raiseError[String](new Exception("error 2"))

val tasks = List(io1, io2) // It will time out because there's no successful value

for {
  promise <- Deferred[IO, String]
  result  <- TaskUtil.firstSuccessful(promise)(tasks)
  _       <- putStrLn(result.toString)
} yield ()
```

```
res2 = java.util.concurrent.TimeoutException: 2 seconds
```


#### First successful computation

The implementation:

```scala
def tryComplete[F[_]: MonadError[?[_], Throwable], A: Monoid](
    d: Deferred[F, A]
)(fa: F[A]): F[A] =
  fa.attempt.flatMap {
    case Right(x) => d.complete(x).attempt *> x.pure[F] <* new Throwable().raiseError
    case Left(_)  => Monoid[A].empty.pure[F] // Ignore the errors
  }

def firstSuccessful[F[_]: Concurrent: Par: Timer, A: Monoid](
    d: Deferred[F, A]
)(list: List[F[A]]): F[A] = {
  import cats.instances.list._
  list.parTraverse(tryComplete[F, A](d)).attempt *> d.get.timeout(2.seconds)
}
```
