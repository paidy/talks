## Http Middleware

An Http Middleware is just a plain function:

```scala
type HttpMiddleware[F[_]] = HttpService[F] => HttpService[F]
```

Http4s provides some middlewares out of the box and they are composable:

```scala
val middleware: HttpMiddleware[F] = {
  { (service: HttpService[F]) =>
    AutoSlash(service)
  } compose { service: HttpService[F] =>
    CORS(service)
  } compose { service =>
    Timeout(2.seconds)(service)
  }
}
```

All you need to do is function application!

```scala
private val endpoints: HttpService[F] = ???
val httpServices: HttpService[F] = middleware(endpoints)
```
