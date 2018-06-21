## Parallelism

```scala
trait Parallel[M[_], F[_]] {
  def apply: Apply[F]
  def flatMap: FlatMap[M]
  def sequential: F ~> M
  def parallel: M ~> F
}
```

#### Par Map N

```scala
val pio1 = IO(println("started pio1")) *> IO.sleep(1.second) *> IO.pure("P1")
val pio2 = IO(println("started pio2")) *> IO.pure("P2")

(pio1, pio2).parMapN { case (a, b) => IO(println(s"$a and $b"))}.flatten
```


## Parallelism

#### Par Traverse

```scala
List(pio1, pio2).parTraverse(io => io *> putStrLn("Traversing"))
```

#### Par Sequence

```scala
List(pio1, pio2).parSequence // IO(List(P1, P2))
```

