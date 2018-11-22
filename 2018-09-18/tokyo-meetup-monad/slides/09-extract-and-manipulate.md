### Back to our `echo` method...

```scala
// Caller of echo decides which strategy to use
def echo[F[_]](console: ConsoleIO[F]): ??? = {
  val input: ??? = console.readLine()
  //Hmm......
}
```

- what should the types be?
- <!-- .element: class="fragment" data-fragment-index="1" --> `F[Unit]`
- <!-- .element: class="fragment" data-fragment-index="1" --> `F[String]`


```scala
def echo[F[_]](console: ConsoleIO[F]): F[Unit] = {
  val input: F[String] = console.readLine()
  val appended = ??? //Hmm......
  console.printLine(appended) //Type mismatch
}
```

Ok, so we're stuck. 
- <!-- .element: class="fragment" data-fragment-index="1" --> How do we append the string?
- <!-- .element: class="fragment" data-fragment-index="2" --> How do we pass a `F[String]` to a method that receives a `String`?
- <!-- .element: class="fragment" data-fragment-index="4" --> We need some constraint for `F` (`F` needs to implement an interface.)


#### What methods should the interface have?

Manipulation of the data stored inside the context.

Let's just call this, `extractAndManipulate`.
<!-- .element: class="fragment" data-fragment-index="1" -->


```scala
def echo[F[_]](console: ConsoleIO[F]) = {
  val input: F[String] = console.readLine()
  val appended: ??? = input.extractAndManipulate(str => str + "monad")
  
  ???
}
```
<!-- .element: class="fragment" data-fragment-index="1" --> What is the type of `appended`?

<!-- .element: class="fragment" data-fragment-index="2" --> The context still stays the same.

<!-- .element: class="fragment" data-fragment-index="3" --> `F[String]`


```scala
def echo[F[_]](console: ConsoleIO[F]) = {
  val input: F[String] = console.readLine()
  val appended: F[String] = input.extractAndManipulate(str => str + "monad")
  
  ???
}
```