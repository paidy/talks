## Effects vs Side Effects

- What are effects?


## Effects vs Side Effects

### Effects

- <!-- .element: class="fragment" data-fragment-index="1" --> **Option[A]**: May or may not produce a value A.
- <!-- .element: class="fragment" data-fragment-index="2" --> **Either[A, B]**: Either produces a value A or a value B.
- <!-- .element: class="fragment" data-fragment-index="3" --> **List[A]**: Produces Zero, One or Many elements of type A.
- <!-- .element: class="fragment" data-fragment-index="4" --> **IO[A]**: Produces a value A, fails or never terminate.


## Effects vs Side Effects

### Side Effects

- <!-- .element: class="fragment" data-fragment-index="1" --> **println("Hey!")**: Writes to the console immediately.
- <!-- .element: class="fragment" data-fragment-index="2" --> **scala.io.StdIn.readLine()**: Reads from the console immediately.
- <!-- .element: class="fragment" data-fragment-index="3" --> **System.nanoTime()**: Retrieves current time from the VM immediately.
- <!-- .element: class="fragment" data-fragment-index="4" --> **Future(deleteDB)**: Deletes database immediately.
