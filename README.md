# Ergo Explorer Backend [![Build Status](https://travis-ci.org/ergoplatform/explorer-back.svg?branch=master)](https://travis-ci.org/ergoplatform/explorer-back)

(!mainnet branch!)

To run backend locally you need:

0. Checkout this repo and open terminal in the root of this project.

1. Install docker and add config file into `~/env/app.env` with two variables `DB_PASS=pass` `DB_USER=ergo` and `~/env/db.env` with two variables `POSTGRES_USER=ergo` `POSTGRES_PASSWORD=pass`. Example of files are stored in this project in the `env` folder.

2. Run db via `docker-compose up postgres` in the terminal in the root folder of this project

3. Run `App.scala` or use `sbt run` command.
