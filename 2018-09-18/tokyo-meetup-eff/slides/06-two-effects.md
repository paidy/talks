## How do I use two effects?

#### **Either[Error, _]** + **Future[_]**

```scala
case class User(email: String)
case class Error(message: String)

def isValidEmail(email: String): Either[Error, String] =
  Either.cond(email.contains("@neopets.com"), email, Error("Lame email address"))

def getUserByEmail(email: String): Future[Either[Error, User]] =
  Future.successful(Right(User(email)))
```
<!-- .element: class="fragment" data-fragment-index="1" -->

```scala
def getUser(email: String): Future[Either[Error, User]] = for {
  validEmail <- isValidEmail(email)
  user <- getUserByEmail(validEmail)
} yield user

getUser("mary@neopets.com") // Success(Right(User("mary@neopets.com")))
```
<!-- .element: class="fragment" data-fragment-index="2" -->

#### Not possible <!-- .element: class="fragment" data-fragment-index="3" -->


## A few solutions

- Monad transformers
<!-- .element: class="fragment" data-fragment-index="1" -->

- Free Monad
<!-- .element: class="fragment" data-fragment-index="2" -->

### Eff!
<!-- .element: class="fragment" data-fragment-index="3" -->
