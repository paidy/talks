## Error Handling: Generic Handler

But it's so common that makes sense to have a single error handler:

```scala
class HttpErrorHandler[F[_] : Monad] extends Http4sDsl[F] {

  val handle: ApiError => F[Response[F]] = {
    case UserNotFound(u) => NotFound(s"User not found: ${u.value}")
    case UserAlreadyExists(u) => Conflict(s"User already exists: ${u.value}")
  }

}
```

And make use of it in any Http Service:

```scala
class UserHttpEndpoint[F[_]](implicit H: HttpErrorHandler[F],
                                      M: MonadError[F, Throwable]) extends Http4sDsl[F] {

  def retrieveUsers: F[List[User]] = ???

  val service: HttpService[F] = HttpService {
    case GET -> Root / "users" =>
      retrieveUsers.flatMap(users => Ok(users).handleErrorWith(H.handle)
  }

}
```
