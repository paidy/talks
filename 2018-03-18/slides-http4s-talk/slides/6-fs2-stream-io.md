## Fs2

Integration with Cats Effect:

```scala
import cats.effect.IO
import fs2._

val stream: Stream[IO, String] = Stream.eval {  IO.pure("Hello World!") }
```

And some transformation functions:

```scala
val pipe: Pipe[IO, String, List[String]] = _.map(_.split(" ").toList)
```

```scala
val sink: Sink[IO, List[String]] = _.evalMap(x => IO(x.foreach(println)))
```

```scala
val program: Stream[IO, Unit] = stream through pipe to sink
```
