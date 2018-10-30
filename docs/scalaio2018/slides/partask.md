## Par Fail Fast

```scala
object Demo extends IOApp {

  val ioa: IO[String] = putStrLn("START >> ioa") *> IO.sleep(1.second) *>
                          IO.pure("a") <* putStrLn("DONE >> ioa")
  val iob: IO[String] = putStrLn("START >> iob") *> IO.sleep(5.seconds) *>
                          IO.pure("b") <* putStrLn("DONE >> iob")
  val ioc: IO[String] = putStrLn("START >> ioc") *> IO.sleep(3.seconds) *>
                          IO.pure("c") <* putStrLn("DONE >> ioc")

  override def run(args: List[String]): IO[ExitCode] =
    Slf4jLogger.create[IO].flatMap { implicit logger =>
      ParTask
        .parFailFast(List(ioa, iob, ioc))
        .flatMap(x => putStrLn(s"RESULT >> $x"))
        .flatMap(_ => IO.sleep(5.seconds))
        .as(ExitCode.Success)
    }

}
```


## Par Fail Fast

No failure. Behaviour equivalent to just using `parSequence`.

```
00:02:09.194 [global-17] INFO - START >> ioc
00:02:09.194 [global-18] INFO - START >> iob
00:02:09.196 [global-16] INFO - START >> ioa
00:02:10.202 [global-18] INFO - DONE >> ioa
00:02:12.198 [global-18] INFO - DONE >> ioc
00:02:14.198 [global-18] INFO - DONE >> iob
00:02:14.219 [global-18] INFO - RESULT >> Right(List(a, b, c))
```


## Par Fail Fast

```scala
object Demo extends IOApp {

  val ioa: IO[String] = putStrLn("START >> ioa") *> IO.sleep(1.second) *>
                          IO.raiseError(new Exception("ioa failed"))
  val iob: IO[String] = putStrLn("START >> iob") *> IO.sleep(5.seconds) *>
                          IO.pure("b") <* putStrLn("DONE >> iob")
  val ioc: IO[String] = putStrLn("START >> ioc") *> IO.sleep(3.seconds) *>
                          IO.raiseError(new Exception("ioc failed"))

  override def run(args: List[String]): IO[ExitCode] =
    Slf4jLogger.create[IO].flatMap { implicit logger =>
      ParTask
        .parFailFast(List(ioa, iob, ioc))
        .flatMap(x => putStrLn(s"RESULT >> $x"))
        .flatMap(_ => IO.sleep(5.seconds))
        .as(ExitCode.Success)
    }

}
```


## Par Fail Fast

On first failure it shorts-circuit but keeps processing the other computations in the background.

```
23:59:19.296 [global-11] INFO - START >> iob
23:59:19.296 [global-16] INFO - START >> ioa
23:59:19.296 [global-15] INFO - START >> ioc
23:59:20.304 [global-16] INFO - parFailFast-handler: java.lang.Exception: ioa failed
23:59:20.339 [global-16] INFO - RESULT >> Left(java.lang.Exception: ioa failed)
23:59:22.299 [global-16] INFO - parFailFast-handler: java.lang.Exception: ioc failed
23:59:24.299 [global-16] INFO - DONE >> iob
```


## Par Fail Fast

```scala
def parFailFast[F[_]: Concurrent: Logger: Par, G[_]: Traverse, A](
  gfa: G[F[A]]
): F[Either[Throwable, G[A]]] = {
  val handler: PartialFunction[Throwable, F[A]] = {
    case e => Logger[F].info(s"parFailFast-handler: $e") *> e.raiseError
  }
  parFailFastWithHandler[F, G, A](gfa, handler)
}

def parFailFastWithHandler[F[_]: Concurrent: Par, G[_]: Traverse, A](
  gfa: G[F[A]],
  handler: PartialFunction[Throwable, F[A]]
): F[Either[Throwable, G[A]]] =
 gfa.parTraverse { fa =>
   Deferred[F, Either[Throwable, A]].flatMap { d =>
     fa.recoverWith(handler).attempt.flatMap(d.complete).start *> d.get.rethrow
   }
 }.attempt
```
