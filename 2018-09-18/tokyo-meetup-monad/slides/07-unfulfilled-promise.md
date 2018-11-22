New business requirement: 


`async`


No problem:
```scala
class SomeAsyncIO(path: String) extends ConsoleIO {
  def readLine(): Future[String] = //...
  def printLine(str: String): Future[Unit] = //...
}
```


Hmm...
```scala
trait ConsoleIO {
  def readLine(): String
  def printLine(str: String): Unit
}
```


Should we change the interface?
- <!-- .element: class="fragment" data-fragment-index="1" --> Application code must be rewritten. (okay..?)
- <!-- .element: class="fragment" data-fragment-index="2" --> Test for business logic must be rewritten into async. (Arrgh)