## Ref-based Cache

```scala
trait Cache[F[_], K, V] {
  def get(key: K): F[Option[V]]
  def put(key: K, value: V): F[Unit]
}
```

```scala
private class RefCache[F[_]: Clock: Monad, K, V](
    state: Ref[F, Map[K, (OffsetDateTime, V)]],
    expiresIn: FiniteDuration
) extends Cache[F, K, V] {

  import Cache._

  def get(key: K): F[Option[V]] =
    state.get.map(_.get(key).map { case (_, v) => v })

  def put(key: K, value: V): F[Unit] =
    DateTime[F](CacheOffset).flatMap { now =>
      state.update(_.updated(key, now.plusNanos(expiresIn.toNanos) -> value))
    }

}
```


## Ref-based Cache

```scala
object Cache {
  def of[F[_]: Clock: Concurrent, K, V](
      expiresIn: FiniteDuration,
      checkOnExpirationsEvery: FiniteDuration
  )(implicit T: Timer[F]): F[Cache[F, K, V]] = {
    def runExpiration(state: Ref[F, Map[K, (OffsetDateTime, V)]]): F[Unit] = {
      val process =
        DateTime[F](CacheOffset).flatMap { now =>
          state.get.map(_.filter {
            case (_, (exp, _)) => exp.isAfter(now.minusNanos(expiresIn.toNanos))
          }).flatTap(state.set)
        }
      T.sleep(checkOnExpirationsEvery) >> process >> runExpiration(state)
    }

    Ref.of[F, Map[K, (OffsetDateTime, V)]](Map.empty)
      .flatTap(runExpiration(_).start.void)
      .map(ref => new RefCache[F, K, V](ref, expiresIn))
  }
}
```

