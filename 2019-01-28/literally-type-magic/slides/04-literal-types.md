## Quiz

#### Can you tell the types?
<!-- .element: class="fragment" data-fragment-index="1" -->
```scala
val _: ??? = "Tokyo"
```
<!-- .element: class="fragment" data-fragment-index="1" -->

<input type="text" size="80" style="font-size:80%"/>
<!-- .element: class="fragment" data-fragment-index="1" -->

Solution:
<!-- .element: class="fragment" data-fragment-index="2" -->
```scala
val _: String = "Tokyo"
val _: Any = "Tokyo"
val _: AnyRef = "Tokyo" //= java.lang.Object
val _: Comparable[_] = "Tokyo"
val _: CharSequence = "Tokyo"
val _: Serializable = "Tokyo"
```
<!-- .element: class="fragment" data-fragment-index="2" -->

Is that all?
<!-- .element: class="fragment" data-fragment-index="3" -->


#### Scala's type hierarchy

```java
public final class String
    implements java.io.Serializable, Comparable<String>, CharSequence { ...
```

![lambda](assets/classhierarchy.png)


#### The hidden types in between

Introducing Scala's literal types:

```scala
val string: "Tokyo"   = "Tokyo"
val symbol: 'Hamburg  = 'Hamburg
val character: 'x'    = 'x'
val int: 42           = 42
val double: 1.2D      = 1.2D
val float: 3.4F       = 3.4F
val long: 5L          = 5L
val boolean: true     = true
```
