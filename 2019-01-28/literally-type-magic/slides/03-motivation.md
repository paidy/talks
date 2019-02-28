## Stringly typed APIs

#### Simple and typesafe csv library

```scala
import CsvLibrary._

case class Todo(id: Int, txt: String)

val csv: Csv[Todo] = loadCsv[Todo]("file.csv")
val todoId: Int = csv.firstValue(_.id)
```


#### Not my type

```scala
import CsvLibrary._

case class Todo(id: Int, txt: String)

val csv: Csv = loadCsv[Todo]("file.csv")
val todoId: Int = csv.firstValue("id").asInstanceOf[Int]
```
<!-- .element: class="fragment" data-fragment-index="1" -->
```scala
import CsvLibrary._

val csv: Csv = loadCsv("file.csv")
val todoId: Int = csv.firstValue("id").asInstanceOf[Int]
```
<!-- .element: class="fragment" data-fragment-index="2" -->

```scala
csv.firstValue("id")  // 0
csv.firstValue("txt") // "first todo"
csv.firstValue("foo") // Exception
```
<!-- .element: class="fragment" data-fragment-index="3" -->


#### Use actual English words!

<pre>
<span style="color:red; font-family:'Courier New'">-- case class Todo(id: Int, txt: String)</span>
<span style="color:green; font-family:'Courier New'">++ case class Todo(id: Int, text: String)</span>
</pre>
<!-- .element: class="fragment" data-fragment-index="1" -->

```scala
csv.firstValue("txt") // Boom
```
<!-- .element: class="fragment" data-fragment-index="2" -->


#### What can we do?

- Use better library
<!-- .element: class="fragment" data-fragment-index="1" -->
- Use a wrapper-library
<!-- .element: class="fragment" data-fragment-index="2" -->
- Deal with it (write a zillion tests)
<!-- .element: class="fragment" data-fragment-index="3" -->
- ...or selectively fix the worst problems!
<!-- .element: class="fragment" data-fragment-index="4" -->
