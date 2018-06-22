organization := "org.ergoplatform"

name := "ergo-explorer"

version := "0.0.1"

scalaVersion := "2.12.5"

resolvers += Resolver.bintrayRepo("hseeberger", "maven")

lazy val doobieVersion = "0.5.3"
lazy val akkaHttpVersion = "10.1.1"
lazy val akkaVersion = "2.5.12"
lazy val catsVersion = "1.1.0"
lazy val circeVersion = "0.9.3"

lazy val doobieDeps = Seq(
  "org.tpolecat" %% "doobie-core"      % doobieVersion,
  "org.tpolecat" %% "doobie-postgres"  % doobieVersion,
  "org.tpolecat" %% "doobie-scalatest" % doobieVersion,
  "org.tpolecat" %% "doobie-hikari"    % doobieVersion
)

lazy val catsDeps = Seq(
  "org.typelevel" %% "cats-core" % catsVersion
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
  "de.heikoseeberger" %% "akka-http-circe" % "1.20.1",
  "org.scalaj" %% "scalaj-http" % "2.4.0",
  "org.flywaydb" % "flyway-core" % "5.1.1",
  "com.github.blemale" %% "scaffeine" % "2.5.0"
)
lazy val circeDeps = Seq(
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion
)

lazy val testDeps = Seq(
  "org.scalacheck" %% "scalacheck" % "1.14.0",
  "org.scalactic" %% "scalactic" % "3.0.5",
  "org.scalatest" %% "scalatest" % "3.0.5" % Test,
  "org.testcontainers" % "postgresql" % "1.7.3" % Test,
  "com.dimafeng" %% "testcontainers-scala" % "0.18.0" % Test
)

libraryDependencies ++= (otherDeps ++ doobieDeps ++ catsDeps ++ loggingDeps ++ akkaDeps ++ circeDeps ++ testDeps)

enablePlugins(FlywayPlugin)

flywayDriver := "org.postgresql.Driver"
flywayUrl := "jdbc:postgresql://0.0.0.0:5432/explorer"

flywayUser := sys.env.getOrElse("DB_USER", "ergo")
flywayPassword := sys.env.getOrElse("DB_PASS", "pass")
flywaySchemas := Seq("public")
flywayTable := "schema_history"
flywayLocations := Seq("filesystem:sql")
flywaySqlMigrationSeparator := "__"

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-language:experimental.macros",
  "-language:higherKinds",
  "-language:postfixOps",
  "-feature",
  "-unchecked",
  "-Xfuture",
  "-Yno-adapted-args",
  "-Ywarn-numeric-widen"
)

test in assembly := {}

mainClass in assembly := Some("org.ergoplatform.explorer.App")
