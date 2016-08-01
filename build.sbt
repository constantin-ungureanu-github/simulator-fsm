name := "simulator"
version := "0.8"
scalaVersion := "2.11.8"

compileOrder := CompileOrder.JavaThenScala
mainClass in (Compile, packageBin) := Some("simulator.Simulator")

EclipseKeys.withSource := true
EclipseKeys.withJavadoc := true
enablePlugins(JavaAppPackaging)

javacOptions in compile ++= Seq("-encoding", "UTF-8", "-source", "1.8", "-target", "1.8", "-Xlint")
javacOptions in doc ++= Seq("-encoding", "UTF-8", "-source", "1.8")
testOptions += Tests.Argument(TestFrameworks.JUnit, "-v", "-a")

libraryDependencies ++= Seq(
  "com.typesafe.akka" % "akka-actor_2.11" % "2.4.8",
  "org.apache.logging.log4j" % "log4j-core" % "2.6.2",
  "org.apache.logging.log4j" % "log4j-api" % "2.6.2",
  "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.6.2",
  "com.lmax" % "disruptor" % "3.3.5",
  "org.apache.commons" % "commons-csv" % "1.4",
  "com.typesafe.akka" % "akka-testkit_2.11" % "2.4.8" % "test",
  "junit" % "junit" % "4.12" % "test",
  "com.novocode" % "junit-interface" % "0.11" % "test")