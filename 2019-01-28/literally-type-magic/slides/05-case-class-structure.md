## HList

#### Generic type of case classes

```scala
case class Todo(id: Int, txt: String)
```

Use shapeless library to get a recursively structured type for case class:
<!-- .element: class="fragment" data-fragment-index="1" -->
```scala
import shapeless._

case class Todo(id: Int, txt: String)

Generic[Todo].to(Todo(1, "example todo"))
```
<!-- .element: class="fragment" data-fragment-index="1" -->
```scala
val result: Int :: String :: HNil = 1 :: "example todo" :: HNil 
```
<!-- .element: class="fragment" data-fragment-index="2" -->
```scala
::[
  Int,
  ::[
    String,
    HNil
  ]
] = ...
```
<!-- .element: class="fragment" data-fragment-index="3" -->
```Scala
(Int, (String, (Double, (String, Nothing)))) = ...
```
<!-- .element: class="fragment" data-fragment-index="4" -->


#### Field names

```scala
import shapeless._

case class Todo(id: Int, txt: String)

LabelledGeneric[Todo].to(Todo(1, "example todo"))
```
<!-- .element: class="fragment" data-fragment-index="1" -->

```scala
val result: Int with shapeless.labelled.KeyTag[Symbol with shapeless.tag.Tagged[String("id")],Int] :: String with shapeless.labelled.KeyTag[Symbol with shapeless.tag.Tagged[String("txt")],String] :: shapeless.HNil = 1 :: "example todo" :: HNil
```
<!-- .element: class="fragment" data-fragment-index="2" -->

This actually means:
<!-- .element: class="fragment" data-fragment-index="3" -->
```scala
ClassField['id, Int] :: ClassField['txt, String] :: HNil
```
<!-- .element: class="fragment" data-fragment-index="3" -->


## Find the literal type

The magical `field` function combines HList and literal types:

```scala
case class Todo(id: Int, txt: String)

// compiles and returns "id"
field[Todo]('id)

// compiles and returns "text"
field[Todo]('txt)

// alternative:
field[Todo, 'id]
```
<!-- .element: class="fragment" data-fragment-index="1" -->

```scala
field[Todo]('text)
// Compile error! Literal type 'text can't be found in the fields of Todo!
```
<!-- .element: class="fragment" data-fragment-index="2" -->


#### Under the hood

```scala
  import shapeless._
  import shapeless.ops.record.Keys
  import shapeless.ops.hlist.Selector
  
  def field[Clazz] = new {
    def apply[FieldName <: Symbol, LabelledGeneric <: HList, FieldNames <: HList]
    (fieldName: Witness.Lt[FieldName])
    ( implicit
      lg: LabelledGeneric.Aux[Clazz, LabelledGeneric],
      keys: Keys.Aux[LabelledGeneric, FieldNames],
      checkField: Selector[FieldNames, FieldName]
    ): String = fieldName.value.name
  }
```
<!-- .element: class="fragment" data-fragment-index="1" -->

Execution scheme:
<!-- .element: class="fragment" data-fragment-index="2" -->
```scala
// returns new anonymous class istance with an .apply method
val applyObject = field[Todo]

// Type parameters are inferrend and implicits are resolved...
applyObject.apply('someFieldName)
```
<!-- .element: class="fragment" data-fragment-index="2" -->


#### Applying it (before/after)

```scala
val todoId: Int = csv.firstValue(       "id"       ).asInstanceOf[Int]
val todoId: Int = csv.firstValue( field[Todo]('id) ).asInstanceOf[Int]
```

<img class="fragment" data-fragment-index="1" style="height:17em;" src="http://localhost:8000/assets/fireworks.jpg">
<!-- .element: class="fragment" data-fragment-index="1" -->
