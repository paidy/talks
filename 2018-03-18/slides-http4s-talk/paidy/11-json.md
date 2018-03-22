## Json Codecs

Http4s provides two interfaces for Json manipulation:

```scala
trait EntityDecoder[F[_], T]
```

```scala
trait EntityEncoder[F[_], A]
```

You can plugin your favorite Json library. However, `Circe` is the recommended one since it's integrated with `Cats` as well. You can define generic Json codecs like this for example:

```scala
trait JsonCodecs[F[_]] {
  implicit def jsonEncoder[A <: Product : Encoder](implicit F: Sync[F])
    : EntityEncoder[F, A] = jsonEncoderOf[F, A]
  implicit def jsonDecoder[A <: Product : Decoder](implicit F: Sync[F])
    : EntityDecoder[F, A] = jsonOf[F, A]
}
```
