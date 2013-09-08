organization := "havarunner"

name := "havarunner"

version := System.getProperty("releaseVersion", "0.1.0-SNAPSHOT")

scalaVersion := "2.10.2"

javacOptions ++= Seq("-source", "1.7")

libraryDependencies ++= Seq(
  "com.google.guava" % "guava" % "14.0.1",
  "cglib" % "cglib-nodep" % "2.2.2",
  "junit" % "junit" % "4.11",
  "com.novocode" % "junit-interface" % "0.9" % "test",
  "com.google.code.findbugs" % "jsr305" % "2.0.1"
)
