## Error Handling: Http Responses

Given your definition of your business errors:

```scala
sealed trait ApiError extends Throwable
case class UserNotFound(username: Username) extends ApiError
case class UserAlreadyExists(username: Username) extends ApiError
```

You can make use of `handleErrorWith` to transform them into the appropiated Http Response:

```scala
class UserHttpEndpoint[F[_]](implicit M: MonadError[F, Throwable]) extends Http4sDsl[F] {

  def retrieveUsers: F[List[User]] = ???

  val service: HttpService[F] = HttpService {
    case GET -> Root / "users" =>
      retrieveUsers.flatMap(users => Ok(users).handleErrorWith {
        case UserNotFound(u) => NotFound(s"User not found: ${u.value}")
        case UserAlreadyExists(u) => Conflict(s"User already exists: ${u.value}")
      }
  }

}
```
