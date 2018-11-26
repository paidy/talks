### What is the problem?

- What's so special about **going async**?
- Language assumes synchronous execution!
- How to describe **pure** business logic without an execution strategy?


### Encoding Effects into types

```scala
trait ConsoleIO[F[_]] {
  def readLine(): F[String]
  def printLine(str: String): F[Unit]
}

object SyncTerminalIO extends ConsoleIO[Id] { //No-op
  def readLine(): String = //...
  def printLine(str: String): Unit = //...
}

object AsyncTerminalIO extends ConsoleIO[Future] {
  def readLine(): Future[String] = //...
  def printLine(str: String): Future[Unit] = //...
}
```