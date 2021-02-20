import JupiterKeys._

val scala3Version = "3.0.0-RC1"

name := "case-class-string-beautifier"
version := "0.0.1"
scalaVersion := scala3Version

libraryDependencies += "net.aichler" % "jupiter-interface" % jupiterVersion.value % Test
libraryDependencies += "org.junit.jupiter" % "junit-jupiter-params" % junitJupiterVersion.value % Test
