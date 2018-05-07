CREATE TABLE blocks (
  id  VARCHAR(64) NOT NULL PRIMARY KEY,
  parent_id VARCHAR(64) REFERENCES blocks (id),
  version SMALLINT NOT NULL,
  height BIGINT NOT NULL,
  ad_proofs_root VARCHAR(64) NOT NULL,
  state_root VARCHAR(66) NOT NULL,
  transactions_root VARCHAR(64) NOT NULL,
  ts BIGINT NOT NULL,
  n_bits INTEGER NOT NULL,
  votes SMALLINT ARRAY,
  nonce BIGINT NOT NULL,
  equihash_solution INTEGER ARRAY
);

ALTER TABLE blocks OWNER TO ergo;

CREATE INDEX "blocks__parent_id" ON blocks (parent_id);

CREATE TABLE interlinks (
  id BIGSERIAL NOT NULL PRIMARY KEY,
  modifier_id VARCHAR(64) NOT NULL,
  block_id VARCHAR(64) REFERENCES blocks (id)
);

ALTER TABLE  interlinks OWNER TO ergo;

CREATE INDEX "interlinks__block_id" on interlinks (block_id);

CREATE TABLE transactions (
  id VARCHAR NOT NULL PRIMARY KEY,
  block_id VARCHAR REFERENCES blocks (id),
  inputs BIGINT ARRAY,
  outputs BIGINT ARRAY
);

ALTER TABLE transactions OWNER to ergo;

CREATE INDEX "transactions__block_id" on transactions (block_id)
