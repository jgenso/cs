organization := "Genso Iniciativas Web"

name := "cs"

version := "0.1"

scalaVersion := "2.9.1"

libraryDependencies ++= {
  val liftVersion = "2.4"
  Seq(
    "net.liftweb" %% "lift-webkit" % liftVersion % "compile",
    "net.liftweb" %% "lift-mapper" % liftVersion % "compile",
    "org.eclipse.jetty" % "jetty-webapp" % "8.0.+" % "test",
    "junit" % "junit" % "4.10" % "test",
    "ch.qos.logback" % "logback-classic" % "0.9.26",
    "org.scala-tools.testing" %% "specs" % "1.6.9" % "test",
    "com.h2database" % "h2" % "1.3.166",
    "net.sf.opencsv" % "opencsv" % "2.1",
    "org.apache.commons" % "commons-lang3" % "3.1"
  )
}

seq(webSettings :_*)

libraryDependencies += "org.eclipse.jetty" % "jetty-webapp" % "8.0.+" % "container"
