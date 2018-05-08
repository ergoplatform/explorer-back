# Ergo Explorer Backend

To run backend locally you need:

1. Install docker

2. Run PostgresDB via docker compose `docker-compose up`

3. Migrate DB using `sbt flywayMigrate`

    a. In case if you want some data you can download sample dump from [here](https://drive.google.com/open?id=1zCIB71iJcCtZthnxyPWInY-2YOIVsUly) and restore it via Pg Admin.
