organization := "org.ergoplatform"

name := "ergo-explorer"

version := "0.0.1"

scalaVersion := "2.12.5"

lazy val doobieVersion = "0.5.2"

libraryDependencies ++= Seq(
  "com.github.pureconfig" %% "pureconfig" % "0.9.1",
  "org.scorexfoundation" %% "scrypto" % "2.1.1",
  "org.tpolecat" %% "doobie-core"     % doobieVersion,
  "org.tpolecat" %% "doobie-postgres" % doobieVersion,
  "org.tpolecat" %% "doobie-specs2"   % doobieVersion,
  "io.monix" %% "monix" % "3.0.0-RC1",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",
)



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
  "-feature",
  "-unchecked",
//  "-Xfatal-warnings",
  "-Xfuture",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard")
