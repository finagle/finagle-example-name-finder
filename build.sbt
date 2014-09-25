scalaVersion := "2.10.4"

resolvers += "twttr" at "http://maven.twttr.com/"

com.twitter.scrooge.ScroogeSBT.newSettings

scalacOptions ++= Seq("-feature", "-language:higherKinds")

libraryDependencies ++= Seq(
  "com.twitter" %% "finagle-core" % "6.20.0",
  "com.twitter" %% "finagle-stats" % "6.20.0",
  "com.twitter" %% "finagle-thrift" % "6.20.0",
  "com.twitter" %% "scrooge-core" % "3.16.3",
  "com.twitter" % "twitter-server_2.10" % "1.7.3",
  "org.apache.opennlp" % "opennlp-tools" % "1.5.3",
  "junit" % "junit" % "4.11" % "test",
  "org.scalatest" %% "scalatest" %"1.9.2" % "test"
)
