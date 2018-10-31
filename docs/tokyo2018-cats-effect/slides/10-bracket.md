## Bracket

```scala
def bracket[A, B](acquire: F[A])(use: A => F[B])
  (release: A => F[Unit]): F[B]
```

#### Safe Resource Acquisition

```scala
val acquireResource: IO[FileOutputStream] =
  IO { new FileOutputStream("test.txt") }

val useResource: FileOutputStream => IO[Unit] =
  fos => IO { fos.write("test data".getBytes()) }

val releaseResource: FileOutputStream => IO[Unit] =
  fos => IO { fos.close() }

acquireResource.bracket(useResource)(releaseResource)
```


## Bracket

#### Caveats

Nested resources get messy very quick

```scala
def putStrLn(str: String): IO[Unit] = IO(println(str))

def acquire(s: String) = putStrLn(s"Acquiring $s") *> IO.pure(s)
def release(s: String) = putStrLn(s"Releasing $s")

acquire("one").bracket { r1 =>
  putStrLn(s"Using $r1") *> acquire("two").bracket { r2 =>
    putStrLn(s"Using $r2") *> acquire("three").bracket { r3 =>
      putStrLn(s"Using $r3")
    } { r3 => release(r3) }
  } { r2 => release(r2) }
} { r1 => release(r1)}
```
