## Sequencing operations

```scala
def echo[F[_]](console: ConsoleIO[F]): F[Unit] = {
  val input: F[String] = console.readLine()
  val appended = input.extractAndManipulate(str => str + "monad")
  // do Something with these two:
  // appended
  // console.printLine()
  
  ???
}
```


#### What methods should the interface have?

Shoving effectful data into an effectful operation

Let's just call this, `extractAndDo`.
<!-- .element: class="fragment" data-fragment-index="1" -->


```scala
def echo[F[_]](console: ConsoleIO[F]): F[Unit] = {
  val input: F[String] = console.readLine()
  val appended = input.extractAndManipulate(str => str + "monad")
  appended.extractAndDo(result => console.printLine(result))
}
```


In order to chain operations...

```scala
def extractAndManipulate(effectfulData: F[A])(manipulate: A => B): F[B]
def extractAndDo(effectfulData: F[A])(effectfulOperation: A => F[B]): F[B]
```


Real names!

```scala
def map(a: F[A])(f: A => B): F[B]
def flatMap(a: F[A])(f: A => F[B]): F[B]
```