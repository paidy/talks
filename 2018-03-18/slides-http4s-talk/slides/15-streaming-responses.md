## Streaming Responses

`Fs2` resides at the core of `Http4s`. Because of this, streaming responses are very simple. Consider a service returning a stream of users:

```scala
import fs2._

val streamOfUsers: Stream[F, User] = ??? // A call to a DB maybe?
```

And an endpoint making use of it:

```scala
import org.http4s._
import org.http4s.dsl._

class UserHttpEndpoint[F[_]] extends Http4sDsl[F] {

  val service: HttpService[F] = HttpService {
    case GET -> Root / "users" =>
      Ok(streamOfUsers)
  }

}
```
