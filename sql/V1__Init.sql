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
  nonce BIGINT NOT NULL,
  block_size BIGINT NOT NULL,
  votes BYTEA NOT NULL,
  equihash_solution INTEGER ARRAY NOT NULL,
  ad_proofs BYTEA
);

ALTER TABLE headers OWNER TO ergo;

CREATE INDEX "headers__parent_id" ON headers (parent_id);

CREATE INDEX "headers__height" ON headers (height);

CREATE TABLE interlinks (
  id BIGSERIAL NOT NULL PRIMARY KEY,
  modifier_id VARCHAR(64) NOT NULL REFERENCES headers (id),
  block_id VARCHAR(64) NOT NULL REFERENCES headers (id)
);

ALTER TABLE  interlinks OWNER TO ergo;

CREATE INDEX "interlinks__block_id" on interlinks (block_id);

CREATE TABLE transactions (
  id VARCHAR(64) NOT NULL PRIMARY KEY,
  block_id VARCHAR(64) NOT NULL REFERENCES headers (id),
  is_coinbase BOOLEAN NOT NULL
);

ALTER TABLE transactions OWNER to ergo;

CREATE INDEX "transactions__block_id" on transactions (block_id);

CREATE TABLE outputs (
  id VARCHAR(64) NOT NULL PRIMARY KEY,
  tx_id VARCHAR(64) NOT NULL REFERENCES transactions (id),
  value BIGINT,
  script VARCHAR
);

ALTER TABLE outputs OWNER to ergo;

CREATE INDEX "outputs__tx_id" on outputs (tx_id);

CREATE TABLE inputs (
  id VARCHAR(64) NOT NULL PRIMARY KEY,
  tx_id VARCHAR(64) NOT NULL REFERENCES transactions (id),
  output VARCHAR(64) NOT NULL REFERENCES outputs (id),
  signature VARCHAR NOT NULL
);

ALTER TABLE inputs OWNER to ergo;

CREATE INDEX "inputs__tx_id" on inputs (tx_id);

