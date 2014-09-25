package com.twitter.finagle.examples.names

/**
 * Represents the result of running name recognition on a text.
 */
case class NameResult(persons: Seq[String], locations: Seq[String], organizations: Seq[String]) {
  lazy val personCounts: Map[String, Int] = countOccurrences(persons)
  lazy val locationCounts: Map[String, Int] = countOccurrences(locations)
  lazy val organizationCounts: Map[String, Int] = countOccurrences(organizations)

  protected def countOccurrences(names: Seq[String]): Map[String, Int] = {
    names.groupBy(identity) map {
      case (name, occurrences) => name -> occurrences.size
    }
  }

  /**
   * Combine with another set of results.
   */
  def ++(other: NameResult): NameResult = {
    NameResult(
      persons ++ other.persons,
      locations ++ other.locations,
      organizations ++ other.organizations)
  }
}

object NameResult {
  val Empty = NameResult(Seq.empty, Seq.empty, Seq.empty)

  /**
   * We often want to combine partial results as we process a body of text.
   */
  def sum(results: Seq[NameResult]) = results.foldLeft(Empty) {
    case (acc, result) => acc ++ result
  }
}
