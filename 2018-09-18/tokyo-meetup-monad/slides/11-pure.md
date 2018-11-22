## Is this all?

New business logic:

- <!-- .element: class="fragment" data-fragment-index="1" --> Echo and return the string. 
- <!-- .element: class="fragment" data-fragment-index="2" --> But if the string starts with '#', don't echo and just return the string.


```scala
def echo[F[_]](console: ConsoleIO[F]): F[String] = {
  val input: F[String] = console.readLine()
  val appended = input.extractAndManipulate(str => str + "monad")
  appended.extractAndDo { (result: String) => 
    if (result.head == '#') console.printLineAndReturn(result)
    else result //uh oh
  }
}
```


Need a way to lift a preexisting value into the context.


```scala
def pure(a: A): F[A]
```


```scala
def echo[F[_]](console: ConsoleIO[F]): F[String] = {
  val input: F[String] = console.readLine()
  val appended = input.map(str => str + "monad")
  appended.flatMap { (result: String) => 
    if (result.head == '#') 
      console.printLine(result).map(_ => result)
    else result.pure
  }
}
```
