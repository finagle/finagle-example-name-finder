package com.twitter.finagle.examples.names.thriftscala

import com.twitter.finagle.examples.names.NameRecognizer
import com.twitter.util.{Future, Return, Throw, Try}
import scala.collection.mutable

/**
 * A naive service definition that implements the trait defined by Scrooge.
 *
 * There are several serious problems with this implementation! First of all,
 * it's not thread-safe: for each language we're using a single NameRecognizer
 * instance (which maintains internal state during processing). It will also
 * block a Finagle thread while attempting to read models for an unknown
 * language from disk.
 */
class NaiveNameRecognizerService(recognizers: Map[String, NameRecognizer])
  extends NameRecognizerService[Future] {

  def getRecognizer(lang: String): Future[NameRecognizer] =
    Future {
      recognizers.get(lang)
    } flatMap {
      case Some(rec) => Future.value(rec)
      case None => Future.const(NameRecognizer.create(lang))
    }

  def findNames(lang: String, document: String): Future[NameRecognizerResult] =
    getRecognizer(lang) map { recognizer =>
      val result = recognizer.findNames(document)

      new NameRecognizerResult {
        val persons = result.persons
        val locations = result.locations
        val organizations = result.organizations
      }
    }
}

object NaiveNameRecognizerService {
  /**
   * A simple constructor that synchronously creates a service with an initial
   * set of language models, encapsulating errors in a Try.
   */
  def create(langs: Seq[String]): Try[NameRecognizerService[Future]] = {
    val recognizersByName: Seq[Try[(String, NameRecognizer)]] = langs map { lang =>
      NameRecognizer.create(lang) map { recognizer =>
        lang -> recognizer
      }
    }

    Try.collect(recognizersByName) map { recognizers =>
      new NaiveNameRecognizerService(recognizers.toMap)
    }
  }
}
