CREATE TABLE total_size (
  id   BIGSERIAL NOT NULL PRIMARY KEY,
  size BIGINT    NOT NULL,
  ts   BIGINT    NOT NULL
);

ALTER TABLE total_size OWNER TO ergo;

CREATE INDEX "totale_size__ts" ON total_size (ts);

CREATE TABLE txs_per_block (
  id                  BIGSERIAL NOT NULL PRIMARY KEY,
  txs_per_block_count BIGINT    NOT NULL,
  ts                  BIGINT    NOT NULL
);

ALTER TABLE txs_per_block OWNER TO ergo;

CREATE INDEX "txs_per_block__ts" ON txs_per_block (ts);

CREATE TABLE difficulty (
  id         BIGSERIAL NOT NULL PRIMARY KEY,
  difficulty BIGINT    NOT NULL,
  ts         BIGINT    NOT NULL
);

ALTER TABLE difficulty OWNER TO ergo;

CREATE INDEX "difficulty__ts" ON difficulty (ts);

CREATE TABLE block_cost (
  id   BIGSERIAL NOT NULL PRIMARY KEY,
  cost BIGINT    NOT NULL,
  ts   BIGINT    NOT NULL
);

ALTER TABLE block_cost OWNER TO ergo;

CREATE INDEX "block_cost__ts" ON block_cost (ts);

CREATE TABLE block_fee (
  id BIGSERIAL NOT NULL PRIMARY KEY,
  fee BIGINT NOT NULL,
  ts BIGINT
);

ALTER TABLE block_fee OWNER TO ergo;

CREATE INDEX "block_fee__ts" ON block_fee (ts);

CREATE TABLE mempool_size (
  id   BIGSERIAL NOT NULL PRIMARY KEY,
  size BIGINT    NOT NULL,
  ts   BIGINT    NOT NULL
);

ALTER TABLE mempool_size OWNER TO ergo;

CREATE INDEX "mempool_size__ts" ON mempool_size (ts);

CREATE TABLE total_tx_count (
  id    BIGSERIAL NOT NULL PRIMARY KEY,
  count BIGINT    NOT NULL,
  ts    BIGINT    NOT NULL
);

ALTER TABLE total_tx_count OWNER TO ergo;

CREATE INDEX "total_tx_count__ts" ON total_tx_count (ts);

CREATE TABLE block_summary (
  id       VARCHAR(64)  NOT NULL PRIMARY KEY,
  ts       BIGINT       NOT NULL,
  size     BIGINT       NOT NULL,
  tx_count INTEGER      NOT NULL,
  supply   BIGINT       NOT NULL,
  fee      BIGINT       NOT NULL,
  mainer   VARCHAR(128) NOT NULL,
  votes    BIGINT       NOT NULL,
  height   BIGINT       NOT NULL
);

ALTER TABLE block_summary OWNER TO ergo;

CREATE INDEX "block_summery__ts" ON block_summary (ts);
