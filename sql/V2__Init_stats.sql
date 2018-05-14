/*
  Total blockchain size on a particular time
  size - size in bytes
  ts - timestamp
 */
CREATE TABLE total_size (
  id   BIGSERIAL NOT NULL PRIMARY KEY,
  size BIGINT    NOT NULL,
  ts   BIGINT    NOT NULL
);

ALTER TABLE total_size OWNER TO ergo;

CREATE INDEX "totale_size__ts" ON total_size (ts);

/*
  Number of txs per block
  txs_per_block_count - number of txs in block
  ts - timestamp
 */
CREATE TABLE txs_per_block (
  id                  BIGSERIAL NOT NULL PRIMARY KEY,
  txs_per_block_count BIGINT    NOT NULL,
  ts                  BIGINT    NOT NULL
);

ALTER TABLE txs_per_block OWNER TO ergo;

CREATE INDEX "txs_per_block__ts" ON txs_per_block (ts);

/*
  Difficulty on a current timestamp
 */
CREATE TABLE difficulty (
  id         BIGSERIAL NOT NULL PRIMARY KEY,
  difficulty BIGINT    NOT NULL,
  ts         BIGINT    NOT NULL
);

ALTER TABLE difficulty OWNER TO ergo;

CREATE INDEX "difficulty__ts" ON difficulty (ts);

/*
  Total block cost (sum of all txs)
  cost - cost
  ts - timestamp
 */
CREATE TABLE block_cost (
  id   BIGSERIAL NOT NULL PRIMARY KEY,
  cost BIGINT    NOT NULL,
  ts   BIGINT    NOT NULL
);

ALTER TABLE block_cost OWNER TO ergo;

CREATE INDEX "block_cost__ts" ON block_cost (ts);

/*
  Total block's fee
  fee - the sum of all txs fees (total reward)
  ts - timestamp
 */
CREATE TABLE block_fee (
  id BIGSERIAL NOT NULL PRIMARY KEY,
  fee BIGINT NOT NULL,
  ts BIGINT
);

ALTER TABLE block_fee OWNER TO ergo;

CREATE INDEX "block_fee__ts" ON block_fee (ts);

/*
  Mempool size
  size - number of unapproved txs in mempool
  ts - timestamp
 */
CREATE TABLE mempool_size (
  id   BIGSERIAL NOT NULL PRIMARY KEY,
  size BIGINT    NOT NULL,
  ts   BIGINT    NOT NULL
);

ALTER TABLE mempool_size OWNER TO ergo;

CREATE INDEX "mempool_size__ts" ON mempool_size (ts);

/*
  Total number of apptoved txs in blockchain
  count - count of all approved txs
  ts - timestamp
 */
CREATE TABLE total_tx_count (
  id    BIGSERIAL NOT NULL PRIMARY KEY,
  count BIGINT    NOT NULL,
  ts    BIGINT    NOT NULL
);

ALTER TABLE total_tx_count OWNER TO ergo;

CREATE INDEX "total_tx_count__ts" ON total_tx_count (ts);

/*
  Quick block info, usefull to show on main page as "last blocks"
 */
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
