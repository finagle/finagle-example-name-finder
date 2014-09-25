package com.twitter.finagle.examples.names

import com.twitter.finagle.examples.names.thriftscala.NameRecognizerException
import com.twitter.util.{Throw, Try}
import java.io.File
import opennlp.tools.namefind.TokenNameFinder
import opennlp.tools.sentdetect.SentenceDetector
import opennlp.tools.tokenize.{Tokenizer, TokenizerME}
import opennlp.tools.util.Span

/**
 * Processes text to extract names of people, places, and organizations. Note
 * that this class and its underlining OpenNLP processing tools are not
 * thread-safe.
 */
class NameRecognizer(
  val lang: String,
  sentenceDetector: SentenceDetector,
  tokenizer: Tokenizer,
  personalNameFinder: TokenNameFinder,
  locationNameFinder: TokenNameFinder,
  organizationNameFinder: TokenNameFinder) {

  /**
   * The default interface to the recognizer; finds names in a document and then
   * clears adaptive data that was gathered during the processing.
   */
  def findNames(document: String): NameResult = {
    val sentences = sentenceDetector.sentDetect(document)
    val tokenized = sentences map { sentence => tokenizer.tokenize(sentence) }
    val results = tokenized map { tokens => findNamesInTokens(tokens) }
    val result = NameResult.sum(results)

    clearAfterDocument()

    result
  }

  /**
   * In some cases the user may wish to process a single sentence out of
   * context and clear adaptive data immediately.
   */
  def findNamesInSentence(sentence: String): NameResult = {
    val tokenized = tokenizer.tokenize(sentence)
    val result = findNamesInTokens(tokenized)

    clearAfterDocument()

    result
  }

  protected def clearAfterDocument(): Unit = {
    personalNameFinder.clearAdaptiveData()
    locationNameFinder.clearAdaptiveData()
    organizationNameFinder.clearAdaptiveData()
  }

  protected def findNamesInTokens(tokens: Array[String]): NameResult = {
    val personalNames = identifyNames(personalNameFinder, tokens)
    val locationNames = identifyNames(locationNameFinder, tokens)
    val organizationNames = identifyNames(organizationNameFinder, tokens)

    NameResult(personalNames, locationNames, organizationNames)
  }

  protected def identifyNames(finder: TokenNameFinder, tokens: Array[String]): Seq[String] = {
    Span.spansToStrings(finder.find(tokens), tokens)
  }
}

object NameRecognizer extends ModelLoader(new File("models")) {
  /**
   * Creates a specified number of identical recognizers given a language
   * identifier and paths to the OpenNLP models.
   */
  def create(
    lang: String,
    count: Int,
    sentenceDetectorFile: File,
    tokenizerFile: File,
    personalNameFile: File,
    locationNameFile: File,
    organizationNameFile: File): Try[Seq[NameRecognizer]] = {

    for {
      sentenceDetectorModel       <- loadSentenceDetectorModel(sentenceDetectorFile)
      tokenizerModel              <- loadTokenizerModel(tokenizerFile)
      personalNameFinderModel     <- loadNameFinderModel(personalNameFile)
      locationNameFinderModel     <- loadNameFinderModel(locationNameFile)
      organizationNameFinderModel <- loadNameFinderModel(organizationNameFile)
    } yield {
      Seq.fill(count) {
        new NameRecognizer(
          lang,
          createSentenceDetector(sentenceDetectorModel),
          createTokenizer(tokenizerModel),
          createNameFinder(personalNameFinderModel),
          createNameFinder(locationNameFinderModel),
          createNameFinder(organizationNameFinderModel))
      }
    }
  } rescue {
    case ex: Throwable => Throw(
      NameRecognizerException(s"Unable to load models for language $lang")
    )
  }

  /**
   * Creates a specified number of identical recognizers given a language
   * identifier (using the default paths to the OpenNLP models).
   */
  def create(lang: String, count: Int): Try[Seq[NameRecognizer]] =
    create(
      lang,
      count,
      defaultSentenceDetectorModel(lang),
      defaultTokenizerModel(lang),
      defaultPersonalNameModel(lang),
      defaultLocationNameModel(lang),
      defaultOrganizationNameModel(lang))

  /**
   * Creates a recognizer given a language identifier (using the default paths
   * to the OpenNLP models).
   */
  def create(lang: String): Try[NameRecognizer] =
    create(lang, 1) map { recognizers => recognizers.head }
}
