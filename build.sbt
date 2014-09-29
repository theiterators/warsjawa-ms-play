name := "warsjawa-ms-play"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

resolvers += "rediscala" at "http://dl.bintray.com/etaty/maven"

libraryDependencies ++= Seq(
  cache,
  ws,
  "org.jsoup" % "jsoup" % "1.7.3",
  "com.etaty.rediscala" %% "rediscala" % "1.3.1"
)
