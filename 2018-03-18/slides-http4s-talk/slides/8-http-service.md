## Http Service

We can define our endpoints as partial functions with a parametric `HttpService[F]`. In this case "/languages":

```scala
import cats.Monad
import org.http4s._
import org.http4s.dsl.Http4sDsl

class LanguagesHttpEndpoint[F[_]: Monad] extends Http4sDsl[F] {

  val service: HttpService[F] = HttpService {
    case GET -> Root / "languages" =>
      Ok(List("haskell", "idris", "scala"))
  }

}
```

Http Services are composable using the `SemigroupK` instance:

```scala
import cats.syntax.semigroupk._

val httpServices: HttpService[F] = (
  userHttpEndpoint <+> languagesHttpEndpoint
  <+> invoiceHttpEndpoint <+> salesHttpEndpoint
)
```
