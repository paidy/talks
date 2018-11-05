## A more complex example

#### Consider this program

```scala
val generateChart: IO[Unit] =
  for {
    b  <- getBandsFromFile // IO[List[Band]]
    _  <- IO.race(longProcess1(b), longProcess2(b))
    id <- generateId
    _  <- publishRadioChart(id, SortedSet(b: _*))
    _  <- publishTvChart(id, SortedSet(b: _*))
  } yield ()
```


## A more complex example

#### Possible implementations

```scala
val getBandsFromFile: IO[List[Band]] =
  IO {
    val file = new File(this.getClass.getClassLoader.getResource("bands.txt").getFile)
    new BufferedReader(new FileReader(file))
  }.flatMap { br =>
    import scala.collection.JavaConverters._
    val bands = br.lines.collect(Collectors.toList()).asScala.toList.map(Band)
    IO.pure(bands) <* IO(br.close())
  }
```

```scala
def generateId: IO[UUID] = IO(UUID.randomUUID())

def longProcess1(bands: List[Band]): IO[Unit] =
  putStrLn("Starting process 1") *> IO.sleep(3.seconds) *> putStrLn("Process 1 DONE")

def longProcess2(bands: List[Band]): IO[Unit] =
  putStrLn("Starting process 2") *> IO.sleep(2.seconds) *> putStrLn("Process 2 DONE")
```


## A more complex example

#### Possible implementations

```scala
def publishRadioChart(id: UUID, bands: SortedSet[Band]): IO[Unit] =
  putStrLn(s"Radio Chart for $id: ${bands.map(_.value).mkString(", ")}")

def publishTvChart(id: UUID, bands: SortedSet[Band]): IO[Unit] =
  putStrLn(s"TV Chart for $id: ${bands.take(5).map(_.value).mkString(", ")}")
```


## A more complex example

#### Consider this program

```scala
val generateChart: IO[Unit] =
  for {
    b  <- getBandsFromFile  // IO[List[Band]]
    _  <- IO.race(longProcess1(b), longProcess2(b))
    id <- generateId
    _  <- publishRadioChart(id, SortedSet(b: _*))
    _  <- publishTvChart(id, SortedSet(b: _*))
  } yield ()
```

<!-- .element: class="fragment" data-fragment-index="1" --> Can we do better?


## A more complex example

#### Algebras

```scala
trait DataSource[F[_]] {
  def bands: F[List[Band]]
}

trait IdGen[F[_]] {
  def generate: F[UUID]
}

trait InternalProcess[F[_]] {
  def process(bands: SortedSet[Band]): F[Unit]
}

trait RadioChart[F[_]] {
  def publish(id: UUID, bands: SortedSet[Band]): F[Unit]
}

trait TvChart[F[_]] {
  def publish(id: UUID, bands: SortedSet[Band]): F[Unit]
}
```


## A more complex example

#### Program

```scala
class Charts[F[_]: Monad](
    source: DataSource[F],
    internal: InternalProcess[F],
    idGen: IdGen[F],
    radioChart: RadioChart[F],
    tvChart: TvChart[F]
) {

  def generate: F[Unit] =
    for {
      b <- source.bands.map(xs => SortedSet(xs: _*))
      _ <- internal.process(b)
      id <- idGen.generate
      _ <- radioChart.publish(b)
      _ <- tvChart.publish(b)
    } yield ()

}
```


## A more complex example

#### Interpreters

```scala
class MemRadioChart[F[_]: Sync] extends RadioChart[F] {
  override def publish(id: UUID, bands: SortedSet[Band]) =
    Sync[F].delay {
      println(s"Radio Chart for $id: ${bands.map(_.value).mkString(", ")}")
    }
}
```

```scala
class MemTvChart[F[_]: Sync] extends TvChart[F] {
  override def publish(id: UUID, bands: SortedSet[Band]) =
    Sync[F].delay {
      println(s"TV Chart for $id: ${bands.map(_.value).take(3).mkString(", ")}")
    }
}
```

```scala
class LiveIdGen[F[_]: Sync] extends IdGen[F] {
  override def generate: F[UUID] = Sync[F].delay(UUID.randomUUID())
}
```


## A more complex example

#### Interpreters

```scala
class FileDataSource[F[_]](implicit F: Sync[F]) extends DataSource[F] {
  override def bands: F[List[Band]] = {
    val acquire = F.delay {
      val file = new File(getClass.getClassLoader.getResource("bands.txt").getFile)
      new BufferedReader(new FileReader(file))
    }

    acquire.bracket { br =>
      import scala.collection.JavaConverters._
      br.lines.collect(Collectors.toList()).asScala.toList.map(Band).pure[F]
    }(br => F.delay(br.close()))
  }
}
```


## A more complex example

#### Interpreters

```scala
class LiveInternalProcess[F[_]](
  implicit F: Concurrent[F],
           T: Timer[F]
) extends InternalProcess[F] {

  private def putStrLn(str: String): F[Unit] = F.delay(println(str))

  private def longProcess1(bands: List[Band]): F[Unit] =
    putStrLn("Starting process 1") *> T.sleep(3.seconds) *> putStrLn("Process 1 DONE")

  private def longProcess2(bands: List[Band]): F[Unit] =
    putStrLn("Starting process 2") *> T.sleep(2.seconds) *> putStrLn("Process 2 DONE")

  override def process(bands: List[Band]): F[Unit] =
    F.race(longProcess1(bands), longProcess2(bands)).void

}
```


## A more complex example

#### Let's run it!

```scala
val source: DataSource[IO]        = new FileDataSource[IO]
val idGen: IdGen[IO]              = new LiveIdGen[IO]
val internal: InternalProcess[IO] = new LiveInternalProcess[IO]
val radioChart: RadioChart[IO]    = new MemRadioChart[IO]
val tvChart: TvChart[IO]          = new MemTvChart[IO]

val charts = new Charts[IO](source, idGen, internal, radioChart, tvChart)

charts.generate.unsafeRunSync()
```


## A more complex example

#### Program

```scala
class Charts[F[_]: Monad](
    source: DataSource[F],
    internal: InternalProcess[F],
    idGen: IdGen[F],
    radioChart: RadioChart[F],
    tvChart: TvChart[F]
) {

  def generate: F[Unit] =
    for {
      b <- source.bands.map(xs => SortedSet(xs: _*))
      _ <- internal.process(b)
      id <- idGen.generate
      _ <- radioChart.publish(b)
      _ <- tvChart.publish(b)
    } yield ()

}
```

<!-- .element: class="fragment" data-fragment-index="1" --> Can we do even better?


## A more complex example

#### Final Program

```scala
class Charts[F[_]: Concurrent: Par](
    source: DataSource[F],
    internal: InternalProcess[F],
    radioChart: RadioChart[F],
    tvChart: TvChart[F]
) {

  def generate: F[Unit] =
    for {
      b <- source.bands.map(xs => SortedSet(xs: _*))
      _ <- internal.process(b).start
      id <- idGen.generate
      _ <- (radioChart.publish(id, b), tvChart.publish(id, b)).parTupled.void
    } yield ()

}
```

