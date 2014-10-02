Example: Named-entity recognition with Finagle [![Build Status](https://secure.travis-ci.org/twitter/finagle-name-finder.png)](http://travis-ci.org/twitter/finagle-name-finder)
=====================================

This project is a demonstration of how you can use Finagle to build a service
that will identify names of people, places, and organizations in any text you
throw at it. It's currently a work in progress.

Quick start
-----------

You'll need to download the OpenNLP model files before you can run the project
tests or examples:

```
sh ./download-models.sh
```

Now when you run `./sbt console` from the project root, [Scrooge][1] will
generate our Thrift service and client traits, and then it'll
compile them along with the rest of our code and start a Scala console. Paste
the following lines to start a server running locally on port 9090:

``` scala
import com.twitter.finagle.Thrift
import com.twitter.finagle.examples.names.thriftscala._

val server = SafeNameRecognizerService.create(Seq("en"), 4, 4) map { service =>
  Thrift.serveIface("localhost:9090", service)
} onSuccess { _ =>
  println("Server started successfully")
} onFailure { ex =>
  println("Could not start the server: " + ex)
}
```

Now you can create a client to speak to the server:

``` scala
import com.twitter.finagle.Thrift
import com.twitter.finagle.examples.names.thriftscala._

val client =
  Thrift.newIface[NameRecognizerService.FutureIface]("localhost:9090")

val doc = """
An anomaly which often struck me in the character of my friend Sherlock Holmes
was that, although in his methods of thought he was the neatest and most
methodical of mankind, and although also he affected a certain quiet primness of
dress, he was none the less in his personal habits one of the most untidy men
that ever drove a fellow-lodger to distraction. Not that I am in the least
conventional in that respect myself. The rough-and-tumble work in Afghanistan,
coming on the top of a natural Bohemianism of disposition, has made me rather
more lax than befits a medical man.
"""

client.findNames("en", doc) onSuccess { response =>
  println("People: " + response.persons.mkString(", "))
  println("Places: " + response.locations.mkString(", "))
} onFailure { ex =>
  println("Something bad happened: " + ex.getMessage)
}
```

This will print the following:

```
People: Sherlock Holmes
Places: Afghanistan
```

As we'd expect. We can also attempt to find names in a Spanish document, since
while we didn't preload the Spanish models when we created our service, we did
download them, so the service will be able to load them if asked:

``` scala
val esDoc = """
Alrededor de 1902 fue el primero en aplicar una descarga eléctrica en un tubo
sellado y con gas neón con la idea de crear una lámpara. Inspirado en parte por
la invención de Daniel McFarlan Moore, la lámpara de Moore, Claude inventó la
lámpara de neón mediante la descarga eléctrica de un gas inerte comprobando que
el brillo era considerable.
"""

client.findNames("es", esDoc) onSuccess { response =>
  println("People: " + response.persons.mkString(", "))
  println("Places: " + response.locations.mkString(", "))
} onFailure { ex =>
  println("Something bad happened: " + ex.getMessage)
}
```

If you need more control over the configuration of the client, you can use the
more explicit `ClientBuilder` approach:

``` scala
import com.twitter.conversions.time._
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.finagle.thrift.ThriftClientFramedCodec

val transport = ClientBuilder()
  .name("nerServer")
  .hosts("localhost:9090")
  .codec(ThriftClientFramedCodec())
  .hostConnectionLimit(1)
  .timeout(1.second)
  .build()

val client = new NameRecognizerService.FinagledClient(transport)

client.findNames("en", doc) onSuccess { response =>
  println("People: " + response.persons.mkString(", "))
  println("Places: " + response.locations.mkString(", "))
} onFailure { ex =>
  println("Something bad happened: " + ex.getMessage)
}
```

This should produce the same result, but will fail if the server doesn't respond
within a second.

[1]: http://twitter.github.io/scrooge/
