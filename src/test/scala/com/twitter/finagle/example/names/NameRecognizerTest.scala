package com.twitter.finagle.examples.names

import com.twitter.util.{Return, Try}
import org.scalatest.{BeforeAndAfter, FunSuite}

class NameRecognizerTest extends FunSuite {
  val recognizer: Try[NameRecognizer] = NameRecognizer.create("en")

  test("NameRecognizer should load successfully") {
    assert(recognizer.isReturn)
  }

  test("NameRecognizer.findNames should find names in a document") {
    val document = """
      John Adams found his residence abroad rather irksome and unpleasant, and
      he longed to return to his happy home. But his services as a diplomatist
      were needed in England.
    """

    val result = recognizer.map(_.findNames(document))

    val expected = NameResult(Seq("John Adams"), Seq("England"), Seq.empty)

    assert(result === Return(expected))
  }

  test("NameRecognizer.findNamesInSentence should find names in a sentence") {
    val sentence = """
      He led the Assembly, as Henry Clay afterwards led the Senate, and Canning
      led the House of Commons, by that inspired logic which few could resist.
    """

    val result = recognizer.map(_.findNamesInSentence(sentence))

    val expected = NameResult(
      Seq("Henry Clay", "Canning"),
      Seq.empty,
      Seq("Assembly", "Senate", "House of Commons"))

    assert(result === Return(expected))
  }
}
