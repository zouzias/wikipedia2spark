name := "spark-wikipedia"
organization := "org.zouzias"
scalaVersion := "2.11.8"
crossScalaVersions := Seq("2.11.8")
licenses := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.html"))
homepage := Some(url("https://github.com/zouzias/spark-lucenerdd"))

scalacOptions ++= Seq("-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-unchecked",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-language:implicitConversions")

javacOptions ++= Seq("-Xlint", "-Xms512M", "-Xmx2048M", "-XX:MaxPermSize=2048M", "-XX:+CMSClassUnloadingEnabled")


libraryDependencies ++= Seq(
  "org.apache.hadoop" % "hadoop-client" % "2.6.0",
  "info.bliki.wiki"   % "bliki-core" % "3.1.0",
  "net.sourceforge.htmlcleaner" % "htmlcleaner" % "2.16",
  "org.apache.spark"  %% "spark-core" % "2.0.2" % "provided",
  "org.apache.spark"  %% "spark-sql" % "2.0.2" % "provided" ,
  "org.scala-lang"     % "scala-library" % scalaVersion.value % "compile"
)
