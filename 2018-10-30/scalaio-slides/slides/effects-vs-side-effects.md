## Effects vs Side-effects

- What are effects?


## Effects vs Side-effects

### Effects

- <!-- .element: class="fragment" data-fragment-index="1" --> **Option[A]**: May or may not produce a value A.
- <!-- .element: class="fragment" data-fragment-index="2" --> **Either[A, B]**: Either produces a value A or a value B.
- <!-- .element: class="fragment" data-fragment-index="3" --> **List[A]**: Produces Zero, One or Many elements of type A.
- <!-- .element: class="fragment" data-fragment-index="4" --> **IO[A]**: Produces a value A, fails or never terminates.


## Effects vs Side-effects

#### Side-effects

- <!-- .element: class="fragment" data-fragment-index="1" --> **println("Hey!")**: Writes to the console immediately.
- <!-- .element: class="fragment" data-fragment-index="2" --> **scala.io.StdIn.readLine()**: Reads from the console immediately.
- <!-- .element: class="fragment" data-fragment-index="3" --> **System.nanoTime()**: Retrieves current time from the JVM immediately.
- <!-- .element: class="fragment" data-fragment-index="4" --> **Future(deleteDB)**: Deletes database immediately.


"Side-effects are bugs". Rob Norris.

![tpolecat](assets/tpolecat.png)
