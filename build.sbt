scalaVersion := "2.10.4"

// Necessary for finagle-stats, which depends on com.twitter.common.metrics
resolvers += "twttr" at "http://maven.twttr.com/"

com.twitter.scrooge.ScroogeSBT.newSettings

scalacOptions ++= Seq("-feature", "-language:higherKinds")

libraryDependencies ++= Seq(
  "com.twitter" %% "finagle-core" % "6.22.0",
  "com.twitter" %% "finagle-stats" % "6.22.0",
  "com.twitter" %% "finagle-thrift" % "6.22.0",
  "com.twitter" %% "scrooge-core" % "3.17.0",
  "com.twitter" %% "twitter-server" % "1.8.0",
  "org.apache.opennlp" % "opennlp-tools" % "1.5.3",
  "junit" % "junit" % "4.11" % "test",
  "org.scalatest" %% "scalatest" %"2.2.2" % "test"
)
