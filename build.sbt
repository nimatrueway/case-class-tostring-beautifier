import JupiterKeys._

name := "case-class-string-beautifier"
version := "0.0.1"
scalaVersion := "2.13.4"

libraryDependencies += "com.github.scopt" %% "scopt" % "4.0.0"
libraryDependencies += "net.aichler" % "jupiter-interface" % jupiterVersion.value % Test
libraryDependencies += "org.junit.jupiter" % "junit-jupiter-params" % junitJupiterVersion.value % Test

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-Xfatal-warnings")

Compile / mainClass := Some("io.github.nimatrueway.caseclass.Main")

nativeImageOptions ++= Seq("--initialize-at-build-time", "--no-fallback")

enablePlugins(NativeImagePlugin)