Paidy Talks
===========

Public talks given by different speakers in representation of [Paidy Engineering](https://engineering.paidy.com/).

- [Eff: One Monad to control them all!]() by [James Carragher](https://github.com/jcarrag) at the [Tokyo Scala Developers Meetup](https://www.meetup.com/Tokyo-Scala-Developers/).
- [Introduction to Monads]() by [Haemin Yoo](https://github.com/yoohaemin) at the [Tokyo Scala Developers Meetup](https://www.meetup.com/Tokyo-Scala-Developers/).
- [Cats Effect: The IO Monad for Scala](2018-06-20/tokyo-meetup-cats-effect/) by [Gabriel Volpe](https://github.com/gvolpe) at the [Tokyo Scala Developers Meetup](https://www.meetup.com/Tokyo-Scala-Developers/).
- [Building a REST API using Http4s (Abstracting over the effect type)](2018-03-18/) by [Gabriel Volpe](https://github.com/gvolpe) at [Scala Matsuri 2018](http://2018.scalamatsuri.org/index_en.html).

Adding your own talk
=====

### Setup

- Copy `/template` to `/$year-month-day/$your-talk-title`.
- Take a look at the other templates to see how to structure `./index.html` and `*.md` files.
- Use `./assets` for local assets, and [`/static/assets`](https://github.com/paidy/talks/tree/master/static/assets) for 'global' Paidy assets.
- Add your talk to the list of talks in the [project README](https://github.com/paidy/talks/blob/master/README.md).

### How to start the server

You need to install `nodejs` and `npm`.

```bash
> cd /$date/$title
> npm install
> npm start
```

Server will start at http://localhost:8000.

### How to print your slides

Loading `http://localhost:8000/?print-pdf/gi` in a browser will render your slides in a format suitable for printing or PDF download
