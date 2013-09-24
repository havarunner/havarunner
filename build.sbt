organization := "com.github.havarunner"

name := "havarunner"

version := System.getProperty("releaseVersion", "master-SNAPSHOT")

crossPaths := false

publishMavenStyle := true

publishTo <<= version { (v: String) =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

scalaVersion := "2.10.2"

javacOptions ++= Seq("-source", "1.7")

parallelExecution in Test := false

libraryDependencies ++= Seq(
  "junit" % "junit" % "4.11" % "provided, test",
  "com.google.guava" % "guava" % "14.0.1" % "provided",
  "com.novocode" % "junit-interface" % "0.9" % "test",
  "com.google.code.findbugs" % "jsr305" % "2.0.1"  % "test"
)

pomExtra :=
  <url>https://github.com/havarunner/havarunner</url>
  <licenses>
    <license>
      <name>The MIT Licence (MIT)</name>
      <url>http://www.opensource.org/licenses/mit-license.php</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:havarunner/havarunner.git</url>
    <connection>scm:git:git@github.com:havarunner/havarunner.git</connection>
    <developerConnection>git@github.com:havarunner/havarunner.git</developerConnection>
  </scm>
  <developers>
    <developer>
      <email>lauri.lehmijoki@iki.fi</email>
      <name>Lauri Lehmijoki</name>
    </developer>
  </developers>
