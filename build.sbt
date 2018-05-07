organization := "org.ergoplatform"

name := "ergo-explorer"

version := "0.0.1"

scalaVersion := "2.12.5"

libraryDependencies ++= Seq(
  "org.scorexfoundation" %% "scrypto" % "2.1.1",
  "org.postgresql" % "postgresql" % "42.2.2"
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
