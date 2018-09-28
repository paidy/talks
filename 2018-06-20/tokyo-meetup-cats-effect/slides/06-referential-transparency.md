## Referential Transparency

#### Are these two programs the same?

```scala
val expr = 123
(expr, expr)
```

```scala
(123, 123)
```


## Referential Transparency

#### Are these two programs the same?

```scala
val expr = println("Hey!")
(expr, expr)
```

```scala
(println("Hey!"), println("Hey!"))
```


## Referential Transparency

#### Are these two programs the same?

```scala
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
```

```scala
val expr = Future(println("Hey!"))
(expr, expr)
```

```scala
(Future(println("Hey!")), Future(println("Hey!")))
```

![trolling](assets/troll-face.png) <!-- .element: class="fragment" -->


## Referential Transparency

#### Are these two programs the same?

```scala
import cats.effect.IO
```

```scala
val expr = IO(println("Hey!"))
(expr, expr)
```

```scala
(IO(println("Hey!")), IO(println("Hey!")))
```

![winner](assets/gentleman.png) <!-- .element: class="fragment" -->


## Referential Transparency

`IO[A]`

Represents the intention to perform a side effect
