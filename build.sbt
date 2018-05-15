organization := "org.ergoplatform"

name := "ergo-explorer"

version := "0.0.1"

scalaVersion := "2.12.5"

resolvers += Resolver.bintrayRepo("hseeberger", "maven")

lazy val doobieVersion = "0.5.2"
lazy val akkaHttpVersion = "10.1.1"
lazy val akkaVersion = "2.5.12"

lazy val doobieDeps = Seq(
  "org.tpolecat" %% "doobie-core"     % doobieVersion,
  "org.tpolecat" %% "doobie-postgres" % doobieVersion,
  "org.tpolecat" %% "doobie-specs2"   % doobieVersion
)

lazy val loggingDeps = Seq(
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0"
)

lazy val akkaDeps = Seq(
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test
)

lazy val otherDeps = Seq(
  "com.github.pureconfig" %% "pureconfig" % "0.9.1",
  "org.scorexfoundation" %% "scrypto" % "2.1.1",
  "io.monix" %% "monix" % "3.0.0-RC1",
  "de.heikoseeberger" %% "akka-http-circe" % "1.20.1"
)

lazy val testDeps = Seq(
  "org.scalacheck" %% "scalacheck" % "1.14.0",
  "org.scalactic" %% "scalactic" % "3.0.5",
  "org.scalatest" %% "scalatest" % "3.0.5" % Test
)

libraryDependencies ++= (otherDeps ++ doobieDeps ++ loggingDeps ++ akkaDeps ++ testDeps)

enablePlugins(FlywayPlugin)

flywayDriver := "org.postgresql.Driver"
flywayUrl := "jdbc:postgresql://localhost:5432/explorer"

flywayUser := "ergo"
flywayPassword := "pass"
flywaySchemas := Seq("public")
flywayTable := "schema_history"
flywayLocations := Seq("filesystem:sql")
flywaySqlMigrationSeparator := "__"

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-language:experimental.macros",
  "-language:postfixOps",
  "-feature",
  "-unchecked",
//  "-Xfatal-warnings",
  "-Xfuture",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard")
