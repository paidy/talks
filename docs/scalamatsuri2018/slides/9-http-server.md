## Http Server

```scala
import cats.effect._
import fs2.{ Stream, StreamApp }

class HttpServer[F[_]: Effect] extends StreamApp[F] {

  override def stream(args: List[String], requestShutdown: F[Unit]): Stream[F, StreamApp.ExitCode] =
    for {
      languagesHttpEndpoint <- Stream(new LanguagesHttpEndpoint[F])
      exitCode <- BlazeBuilder[F]
        .bindHttp(8080, "0.0.0.0")
        .mountService(languagesHttpEndpoint)
        .serve
    } yield exitCode

}
```

And only one place to define your concrete implementation of `cats.effect.Effect`.

```scala
object Server extends HttpServer[IO]
```
