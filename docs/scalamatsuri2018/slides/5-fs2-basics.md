## Fs2

Streaming library built on top of `Cats Effect`.

Its main type is:

```scala
class Stream[F[_], I]
```

And two other functions based on the main type:

```scala
type Pipe[F[_], I, O] = Stream[F, I] => Stream[F, O]
type Sink[F[_], I] = Pipe[F, I, Unit]
```
