## Http4s main data types

`Kleisli` is at the core of Http4s.

```scala
type HttpService[F] = Kleisli[OptionT[F, ?], Request[F], Response[F]]
```

```scala
type HttpMiddleware[F[_]] = HttpService[F] => HttpService[F]
```

***This is changing in dev version 0.19.***

```scala
type Http[F[_], G[_]] = Kleisli[F, Request[G], Response[G]]
```

```scala
type HttpRoutes[F[_]] = Http[OptionT[F, ?], F]
```

