package com.twitter.finagle.examples.names.thriftscala

import com.twitter.concurrent.{AsyncQueue, NamedPoolThreadFactory}
import com.twitter.finagle.examples.names.NameRecognizer
import com.twitter.util.{Future, FuturePool, NonFatal}
import java.util.concurrent.Executors

/**
 * A simple service implementation that implements the trait defined by Scrooge.
 *
 * Each service owns a queue of recognizers for a set of languages. For each
 * request, if we have a queue for the given language, we grab a recognizer from
 * that queue (and return it when processing is finished). If the language is
 * unknown, we try to load the models for it in a future pool (in order to avoid
 * blocking the Finagle thread).
 */
class SafeNameRecognizerService(
  recognizers: Map[String, AsyncQueue[NameRecognizer]],
  futurePool: FuturePool)
  extends NameRecognizerService[Future] {

  def loadRecognizer(lang: String): Future[NameRecognizer] =
    futurePool {
      Future.const(NameRecognizer.create(lang))
    }.flatten

  def getRecognizer(lang: String): Future[NameRecognizer] =
    Future {
      recognizers.get(lang)
    } flatMap {
      case Some(queue) => queue.poll()
      case None => loadRecognizer(lang)
    }

  def findNames(lang: String, document: String): Future[NameRecognizerResult] =
    getRecognizer(lang) flatMap { recognizer =>
      Future {
        val result = recognizer.findNames(document)

        new NameRecognizerResult {
          val persons = result.persons
          val locations = result.locations
          val organizations = result.organizations
        }
      } ensure {
        recognizers.get(lang) foreach { queue =>
          queue.offer(recognizer)
        }
      }
    }
}

object SafeNameRecognizerService {
  val futurePoolName = "NameRecognizerServiceFuturePool"

  /**
   * An asynchronous constructor that creates a `NameRecognizerService` with a
   * future pool backed by an `ExecutorService` for blocking operations and with
   * pools of recognizers for a given set of languages.
   */
  def create(
    langs: Seq[String],
    numThreads: Int,
    numRecognizers: Int): Future[NameRecognizerService[Future]] = {

    Future.collect {
      langs map { lang =>
        Future.const {
          NameRecognizer.create(lang, numRecognizers) map { recognizers =>
            val queue = new AsyncQueue[NameRecognizer]

            recognizers foreach { recognizer => queue.offer(recognizer) }

            lang -> queue
          }
        }
      }
    } map { recognizers =>
      val futurePool = FuturePool(
        Executors.newFixedThreadPool(
          numThreads,
          new NamedPoolThreadFactory(futurePoolName, makeDaemons = true)
        )
      )

      new SafeNameRecognizerService(recognizers.toMap, futurePool)
    }
  }
}
