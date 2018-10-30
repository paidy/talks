## What we've seen so far

- Composing programs using IO
- Sync[F]: StdConsole[F]
- Async[F]: RemoteConsole[F]
- Testing: TestConsole[F] + Ref[F, A]
- <!-- .element: class="fragment" data-fragment-index="1" --> Resource management: Bracket[F, E]
- <!-- .element: class="fragment" data-fragment-index="2" --> Concurrency: F.race, F.start
- <!-- .element: class="fragment" data-fragment-index="3" --> Parallelism: F.parTupled
- <!-- .element: class="fragment" data-fragment-index="4" --> Implicit Cancellation: F.race, F.parTupled

