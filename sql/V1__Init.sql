CREATE TABLE blocks (
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
  votes BYTEA NOT NULL,
  equihash_solution INTEGER ARRAY NOT NULL
);

ALTER TABLE blocks OWNER TO ergo;

CREATE INDEX "blocks__parent_id" ON blocks (parent_id);

CREATE INDEX "blocks__height" ON blocks (height);

CREATE TABLE interlinks (
  id BIGSERIAL NOT NULL PRIMARY KEY,
  modifier_id VARCHAR(64) NOT NULL,
  block_id VARCHAR(64) NOT NULL REFERENCES blocks (id)
);

ALTER TABLE  interlinks OWNER TO ergo;

CREATE INDEX "interlinks__block_id" on interlinks (block_id);

CREATE TABLE transactions (
  id VARCHAR NOT NULL PRIMARY KEY,
  block_id VARCHAR(64) NOT NULL REFERENCES blocks (id),
  inputs BIGINT ARRAY NOT NULL,
  outputs BIGINT ARRAY NOT NULL
);

ALTER TABLE transactions OWNER to ergo;

CREATE INDEX "transactions__block_id" on transactions (block_id)
