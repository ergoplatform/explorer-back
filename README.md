# Ergo Explorer Backend

To run backend locally you need:

0. Checkout this repo and open terminal in the root of this project.

1. Install docker

2. Run PostgresDB via docker compose `docker-compose up`

3. Migrate DB using `sbt flywayMigrate`

4. Generate some random data. To do this just run `sbt runMain org.ergoplatform.explorer.DbDataGenerator`.
In case if you wanna delete all this data you can use another command `sbt runMain org.ergoplatform.explorer.DbDataGenerator clear`
