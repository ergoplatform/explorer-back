CREATE TABLE headers (
  id  VARCHAR(64) NOT NULL PRIMARY KEY,
  parent_id VARCHAR(64) NOT NULL,
  version SMALLINT NOT NULL,
  height INTEGER NOT NULL,
  ad_proofs_root VARCHAR(64) NOT NULL,
  state_root VARCHAR(66) NOT NULL,
  transactions_root VARCHAR(64) NOT NULL,
  ts BIGINT NOT NULL,
  n_bits BIGINT NOT NULL,
  extension_hash VARCHAR(64) NOT NULL,
  block_size BIGINT NOT NULL,
  equihash_solution INTEGER ARRAY NOT NULL,
  ad_proofs BYTEA,
  tx_count BIGINT NOT NULL DEFAULT 0,
  miner_name VARCHAR NOT NULL,
  miner_address VARCHAR NOT NULL
);

ALTER TABLE headers OWNER TO ergo;

CREATE INDEX "headers__parent_id" ON headers (parent_id);

CREATE INDEX "headers__height" ON headers (height);

CREATE TABLE interlinks (
  id BIGSERIAL NOT NULL PRIMARY KEY,
  modifier_id VARCHAR(64) NOT NULL,
  block_id VARCHAR(64) NOT NULL
);

ALTER TABLE  interlinks OWNER TO ergo;

CREATE INDEX "interlinks__block_id" ON interlinks (block_id);

CREATE TABLE transactions (
  id VARCHAR(64) NOT NULL PRIMARY KEY,
  block_id VARCHAR(64) NOT NULL REFERENCES headers (id),
  is_coinbase BOOLEAN NOT NULL,
  ts BIGINT NOT NULL
);

ALTER TABLE transactions OWNER to ergo;

CREATE INDEX "transactions__block_id" on transactions (block_id);

CREATE TABLE outputs (
  id VARCHAR(64) NOT NULL PRIMARY KEY,
  tx_id VARCHAR(64) NOT NULL REFERENCES transactions (id),
  value BIGINT NOT NULL,
  script VARCHAR NOT NULL,
  hash VARCHAR NOT NULL
);

ALTER TABLE outputs OWNER to ergo;

CREATE INDEX "outputs__tx_id" on outputs (tx_id);

CREATE TABLE inputs (
  id VARCHAR(64) NOT NULL PRIMARY KEY,
  tx_id VARCHAR(64) NOT NULL REFERENCES transactions (id),
  output VARCHAR(64) NOT NULL,
  signature VARCHAR NOT NULL
);

ALTER TABLE inputs OWNER to ergo;

CREATE INDEX "inputs__tx_id" on inputs (tx_id);

CREATE TABLE blockchain_stats (
  id BIGSERIAL NOT NULL PRIMARY KEY,
  ts BIGINT NOT NULL,
  block_size BIGINT NOT NULL,
  total_size BIGINT NOT NULL,
  txs_count BIGINT NOT NULL,
  txs_total_count BIGINT NOT NULL,
  blocks_count BIGINT NOT NULL,
  difficulty BIGINT NOT NULL,
  block_coins BIGINT NOT NULL,
  total_coins BIGINT NOT NULL,
  block_value BIGINT NOT NULL,
  block_fee BIGINT NOT NULL,
  total_mining_time BIGINT NOT NULL,
  block_mining_time BIGINT NOT NULL,
  version VARCHAR NOT NULL,
  supply BIGINT NOT NULL,
  market_cap BIGINT NOT NULL,
  hashrate BIGINT NOT NULL
);

CREATE INDEX "blockchain_stats__ts" ON blockchain_stats (ts);

