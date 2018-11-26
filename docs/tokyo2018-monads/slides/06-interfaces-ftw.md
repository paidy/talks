## Interfaces!

```scala
trait ConsoleIO {
  def readLine(): String
  def printLine(str: String): Unit
}

object Terminal extends ConsoleIO {
  import scala.io.StdIn
  def readLine(): String = StdIn.readLine()
  def printLine(str: String): Unit = println(str)
}

def echo(console: ConsoleIO): Unit = {
  val input = console.readLine()
  val appended = input + "monad"
  console.printLine(appended)
}
```


### More implementations!

```scala
object DummyIO extends ConsoleIO {
  /* ... */
}

class FileIO(path: String) extends ConsoleIO {
  /* ... */
}
```