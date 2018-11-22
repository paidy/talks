## Business logic

- <!-- .element: class="fragment" data-fragment-index="1" --> Get a String from the outside
- <!-- .element: class="fragment" data-fragment-index="2" --> Append "monad"
- <!-- .element: class="fragment" data-fragment-index="3" --> echo it back
- <!-- .element: class="fragment" data-fragment-index="4" --> We're using the console right now


```scala
import scala.io.StdIn

def echo(): Unit = {
  val input = StdIn.readLine()
  val appended = input + "monad"
  println(appended)
}
```
<!-- .element: class="fragment" data-fragment-index="4" --> What's wrong with this?

<!-- .element: class="fragment" data-fragment-index="5" --> Mixing business logic with implementation detail


- Testing is hard
- Changing is hard
- Reading is hard