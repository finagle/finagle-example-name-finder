scalaVersion := "2.10.5"

// Necessary for finagle-stats, which depends on com.twitter.common.metrics
resolvers += "Twitter's Repository" at "https://maven.twttr.com/"

scalacOptions ++= Seq("-feature", "-language:higherKinds")

autoAPIMappings := true

libraryDependencies ++= Seq(
  "com.twitter" %% "finagle-core" % "6.29.0",
  "com.twitter" %% "finagle-stats" % "6.29.0",
  "com.twitter" %% "finagle-thrift" % "6.29.0",
  "com.twitter" %% "scrooge-core" % "4.1.0",
  "com.twitter" %% "twitter-server" % "1.14.0",
  "org.apache.opennlp" % "opennlp-tools" % "1.6.0",
  "org.scalatest" %% "scalatest" %"2.2.5" % "test"
)

