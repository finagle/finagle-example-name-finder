package com.twitter.finagle.examples.names

import com.twitter.logging.Logger
import com.twitter.util.{Return, Throw, Try}
import java.io.{File, FileInputStream, FileNotFoundException, InputStream}
import opennlp.tools.namefind.{NameFinderME, TokenNameFinder, TokenNameFinderModel}
import opennlp.tools.sentdetect.{SentenceDetector, SentenceDetectorME, SentenceModel}
import opennlp.tools.tokenize.{Tokenizer, TokenizerME, TokenizerModel}

/**
 * A helper class that allows us to keep most of the gritty details about how
 * our models are deserialized out of the definition of
 * [[com.twitter.finagle.examples.names.NameRecognizer]].
 *
 * Note that for some languages we may not have model files for sentence
 * boundary detection or tokenization; in these cases we fall back to the
 * English-language models.
 */
class ModelLoader(baseDir: File) {
  private val log = Logger.get(getClass)

  /**
   * A utility method that uses a provided function to do something with an
   * input stream that it is guaranteed to close.
   */
  private[this] def loadModel[M](file: File)(f: InputStream => M): Try[M] =
    Try {
      new FileInputStream(file)
    } flatMap { stream =>
      Try {
        f(stream)
      } ensure {
        stream.close()
      }
    }

  protected def loadSentenceDetectorModel(file: File): Try[SentenceModel] =
    loadModel[SentenceModel](file) { stream => new SentenceModel(stream) }

  protected def loadTokenizerModel(file: File): Try[TokenizerModel] =
    loadModel[TokenizerModel](file) { stream => new TokenizerModel(stream) }

  protected def loadNameFinderModel(file: File): Try[TokenNameFinderModel] =
    loadModel[TokenNameFinderModel](file) { stream => new TokenNameFinderModel(stream) }

  protected def createSentenceDetector(model: SentenceModel): SentenceDetector =
    new SentenceDetectorME(model)

  protected def createTokenizer(model: TokenizerModel): Tokenizer =
    new TokenizerME(model)

  protected def createNameFinder(model: TokenNameFinderModel): TokenNameFinder =
    new NameFinderME(model)

  protected def defaultSentenceDetectorModel(lang: String): File = {
    val langModel = new File(baseDir, s"$lang-sent.bin")

    if (!langModel.exists || !langModel.isFile) {
      log.info(s"$langModel does not exist for language $lang; using English model.")
      new File(baseDir, "en-sent.bin")
    } else {
      langModel
    }
  }

  protected def defaultTokenizerModel(lang: String): File = {
    val langModel = new File(baseDir, s"$lang-token.bin")

    if (!langModel.exists || !langModel.isFile) {
      log.info(s"$langModel does not exist for language $lang; using English model.")
      new File(baseDir, "en-token.bin")
    } else {
      langModel
    }
  }

  protected def defaultPersonalNameModel(lang: String): File = {
    new File(baseDir, s"$lang-ner-person.bin")
  }

  protected def defaultLocationNameModel(lang: String): File = {
    new File(baseDir, s"$lang-ner-location.bin")
  }

  protected def defaultOrganizationNameModel(lang: String): File = {
    new File(baseDir, s"$lang-ner-organization.bin")
  }
}
