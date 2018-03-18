package com.paidy.talks.http4s

import cats.MonadError
import cats.data.OptionT
import cats.effect.{Effect, IO, Sync}
import cats.syntax.all._
import com.paidy.talks.http4s.model._
import fs2.{Stream, StreamApp}
import io.circe.generic.auto._
import io.circe.generic.extras.decoding.UnwrappedDecoder
import io.circe.generic.extras.encoding.UnwrappedEncoder
import io.circe.{Decoder, Encoder}
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.blaze.BlazeBuilder

import scala.concurrent.ExecutionContext.Implicits.global

object Server extends HttpServer[IO]

class HttpServer[F[_]: Effect] extends StreamApp[F] {

  override def stream(args: List[String], requestShutdown: F[Unit]): Stream[F, StreamApp.ExitCode] =
    for {
      userRepository <- Stream(new InMemoryUserRepository[F])
      userService    <- Stream(new UserService[F](userRepository))
      usersHttpEndpoint <- Stream(new UsersHttpEndpoint[F](userService))
      exitCode <- BlazeBuilder[F]
        .bindHttp(8080, "0.0.0.0")
        .mountService(usersHttpEndpoint.service, "/users")
        .serve
    } yield exitCode

}

class UsersHttpEndpoint[F[_]: Sync](userService: UserService[F]) extends Http4sDsl[F] with JsonCodecs[F] {

  val service: HttpService[F] = HttpService {
    case GET -> Root =>
      Ok(userService.findAll)

    case GET -> Root / username =>
      userService.find(Username(username)).flatMap { maybeUser =>
        maybeUser.fold(NotFound(s"User with username: $username"))(Ok(_))
      }

    case req @ POST -> Root =>
      req.decode[User] { user =>
        userService.persist(user)
          .flatMap(_ => Created(s"User created with username: ${user.username.value}"))
          .handleErrorWith {
            case UserAlreadyExists(username) => Conflict(s"Existent user with username: ${username.value}")
          }
      }

    case DELETE -> Root / username =>
      userService.delete(Username(username)).flatMap { maybeUser =>
        maybeUser.fold(NotFound(s"User with username: $username"))(_ => NoContent())
      }
  }

}

trait JsonCodecs[F[_]] {
  implicit def valueClassEncoder[A: UnwrappedEncoder]: Encoder[A] = implicitly
  implicit def valueClassDecoder[A: UnwrappedDecoder]: Decoder[A] = implicitly

  implicit def jsonDecoder[A <: Product : Decoder](implicit F: Sync[F]): EntityDecoder[F, A] = jsonOf[F, A]
  implicit def jsonEncoder[A <: Product : Encoder](implicit F: Sync[F]): EntityEncoder[F, A] = jsonEncoderOf[F, A]
}

class UserService[F[_]](repo: UserRepository[F])(implicit F: MonadError[F, Throwable]) {
  def findAll: F[List[User]] = repo.findAll
  def find(username: Username): F[Option[User]] = repo.find(username)
  def persist(user: User): F[Unit] =
    find(user.username).flatMap { maybeUser =>
      maybeUser.fold(repo.persist(user)) { _ =>
        F.raiseError(UserAlreadyExists(user.username))
      }
    }
  def delete(username: Username): F[Option[User]] = {
    find(username).flatMap { maybeUser =>
      OptionT.fromOption[F](maybeUser).flatMapF { _ =>
        repo.delete(username) *> F.pure(maybeUser)
      }.value
    }
  }
}

trait UserRepository[F[_]] {
  def findAll: F[List[User]]
  def find(username: Username): F[Option[User]]
  def persist(user: User): F[Unit]
  def delete(username: Username): F[Unit]
}

class InMemoryUserRepository[F[_]](implicit F: Sync[F]) extends UserRepository[F] {
  private val users = scala.collection.mutable.Map(
    Username("gvolpe") -> User(Username("gvolpe"), Some(Age(30))),
    Username("msabin") -> User(Username("msabin"), None)
  )
  override def findAll: F[List[User]] = F.delay(users.values.toList)
  override def find(username: Username): F[Option[User]] = F.delay(users.get(username))
  override def persist(user: User): F[Unit] = F.delay(users += (user.username -> user)) *> F.unit
  override def delete(username: Username): F[Unit] = F.delay(users -= username)
}

object model {
  case class Username(value: String) extends AnyVal
  case class Age(value: Int) extends AnyVal
  case class User(username: Username, age: Option[Age])

  case class UserAlreadyExists(username: Username) extends Exception(username.value)
}
