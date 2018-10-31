## Resource

- Nested resources are released in reverse order of acquisition.
- Outer resources are released even if an inner use or release fails.

```scala
def allocate: F[(A, F[Unit])]
def use[B, E](f: A => F[B])(implicit F: Bracket[F, E]): F[B] =
  F.bracket(allocate)(a => f(a._1))(_._2)
```

#### Nested Resource Acquisition

```scala
def mkResource(s: String): Resource[IO, String] = {
  val acquire = IO(println(s"Acquiring $s")) *> IO.pure(s)
  def release(s: String) = IO(println(s"Releasing $s"))
  Resource.make[IO, String](acquire)(release)
}

val r = for {
  outer <- mkResource("outer")
  inner <- mkResource("inner")
} yield (outer, inner)

r.use { case (a, b) => IO(println(s"Using $a and $b")) }
```
