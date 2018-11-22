## How do I use one effect?

#### Of the same type: **Option[A]**

```scala
for {
  person1 <- Some("James")
  person2 <- Some("Mary")
  person3 <- Some("Bob")
} yield s"Hello $person1, $person2, and $person3"

// Some("Hello James, Mary, and Bob")
```
<!-- .element: class="fragment" data-fragment-index="1" -->

```scala
for {
  person1 <- Some("James")
  person2 <- None
  person3 <- Some("Bob")
} yield s"Hello $person1, $person2, and $person3"

// None
```
<!-- .element: class="fragment" data-fragment-index="2" -->


## How do I use one effect?

#### Of the same type (**Future[A]**)

```scala
for {
  person1 <- Future.successful("James")
  person2 <- Future.successful("Mary")
  person3 <- Future.successful("Bob")
} yield s"Hello $person1, $person2, and $person3"

// Success("Hello James, Mary, and Bob")
```
<!-- .element: class="fragment" data-fragment-index="1" -->

```scala
for {
  person1 <- Future.successful("James")
  person2 <- Future.failed(new Exception("Person2 not found"))
  person3 <- Future.successful("Bob")
} yield s"Hello $person1, $person2, and $person3"

// Failure(Exception("Person2 not found"))
```
<!-- .element: class="fragment" data-fragment-index="2" -->

