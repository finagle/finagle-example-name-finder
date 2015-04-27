scalaVersion := "2.10.5"

// Necessary for finagle-stats, which depends on com.twitter.common.metrics
resolvers += "Twitter's Repository" at "http://maven.twttr.com/"

scalacOptions ++= Seq("-feature", "-language:higherKinds")

autoAPIMappings := true

libraryDependencies ++= Seq(
  "com.twitter" %% "finagle-core" % "6.25.0",
  "com.twitter" %% "finagle-stats" % "6.25.0",
  "com.twitter" %% "finagle-thrift" % "6.25.0",
  "com.twitter" %% "scrooge-core" % "3.18.0",
  "com.twitter" %% "twitter-server" % "1.10.0",
  "org.apache.opennlp" % "opennlp-tools" % "1.5.3",
  "org.scalatest" %% "scalatest" %"2.2.4" % "test"
)

