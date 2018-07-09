CREATE TABLE node_headers (
  id VARCHAR(64) NOT NULL PRIMARY KEY,
  parent_id VARCHAR(64) NOT NULL,
  version SMALLINT NOT NULL,
  height BIGINT NOT NULL,
  n_bits BIGINT NOT NULL,
  difficulty BIGINT NOT NULL,
  timestamp BIGINT NOT NULL,
  state_root VARCHAR(66) NOT NULL,
  ad_proofs_root VARCHAR(64) NOT NULL,
  transactions_root VARCHAR(64) NOT NULL,
  extension_hash VARCHAR(64) NOT NULL,
  equihash_solutions VARCHAR NOT NULL,
  interlinks VARCHAR ARRAY NOT NULL,
  main_chain BOOLEAN NOT NULL
);

CREATE INDEX "node_headers__parent_id" ON node_headers (parent_id);
CREATE INDEX "node_headers__height" ON node_headers (height);
CREATE INDEX "node_headers__ts" ON node_headers (timestamp);
CREATE INDEX "node_headers__main_chain" ON node_headers (main_chain);

ALTER TABLE node_headers OWNER TO ergo;

CREATE TABLE blocks_info (
  header_id VARCHAR(64) NOT NULL PRIMARY KEY,
  timestamp BIGINT NOT NULL,
  height BIGINT NOT NULL,
  difficulty BIGINT NOT NULL,
  block_size BIGINT NOT NULL,
  block_coins BIGINT NOT NULL,
  block_mining_time BIGINT NOT NULL,
  txs_count BIGINT NOT NULL,
  txs_size BIGINT NOT NULL,
  miner_address VARCHAR NOT NULL,
  miner_reward BIGINT NOT NULL,
  miner_revenue BIGINT NOT NULL,
  block_fee BIGINT NOT NULL,
  block_chain_total_size BIGINT NOT NULL,
  total_txs_count BIGINT NOT NULL,
  total_coins_issued BIGINT NOT NULL,
  total_mining_time BIGINT NOT NULL,
  total_fees BIGINT NOT NULL,
  total_miners_reward BIGINT NOT NULL,
  total_coins_in_txs BIGINT NOT NULL
);

CREATE INDEX "blocks_info__height" ON node_headers (height);
CREATE INDEX "blocks_info__ts" ON node_headers (timestamp);

CREATE TABLE node_transactions (
  id VARCHAR(64) NOT NULL PRIMARY KEY,
  header_id VARCHAR(64) NOT NULL,
  coinbase BOOLEAN NOT NULL,
  timestamp BIGINT NOT NULL,
  size BIGINT NOT NULL
);

ALTER TABLE node_transactions OWNER TO ergo;

CREATE INDEX "node_transactions__header_id" on node_transactions (header_id);
CREATE INDEX "node_transactions__timestamp" on node_transactions (timestamp);

CREATE TABLE node_inputs (
  box_id VARCHAR(64) NOT NULL,
  tx_id VARCHAR(64) NOT NULL,
  proof_bytes VARCHAR NOT NULL,
  extension JSON NOT NULL,
  PRIMARY KEY (box_id, tx_id)
);

ALTER TABLE node_inputs OWNER TO ergo;

CREATE INDEX "node_inputs__tx_id" on node_inputs (tx_id);
CREATE INDEX "node_inputs__box_id" on node_inputs (box_id);

CREATE TABLE node_outputs (
  box_id VARCHAR(64) NOT NULL PRIMARY KEY,
  tx_id VARCHAR(64) NOT NULL,
  value BIGINT NOT NULL,
  index INTEGER NOT NULL,
  proposition VARCHAR NOT NULL,
  hash VARCHAR NOT NULL,
  additional_registers JSON NOT NULL
);

ALTER TABLE node_outputs OWNER to ergo;

CREATE INDEX "node_outputs__tx_id" on node_outputs (tx_id);
CREATE INDEX "node_outputs__hash" on node_outputs (hash);

CREATE TABLE node_ad_proofs (
  header_id VARCHAR(64) NOT NULL PRIMARY KEY,
  proof_bytes VARCHAR NOT NULL,
  digest VARCHAR NOT NULL
);

ALTER TABLE node_ad_proofs OWNER TO ergo;

CREATE TABLE known_miners (
  miner_address VARCHAR NOT NULL PRIMARY KEY,
  miner_name VARCHAR NOT NULL
);

ALTER TABLE known_miners OWNER TO ergo;

INSERT INTO known_miners(miner_address, miner_name) VALUES
('cd0702774a99fa3041fa8d441ac81217c8f1d715c83bac4f7fcc2be3f0d454c6c908fe', 'catena_serv'),
('cd07026146d4305b4ee16e3b2e0ccabaab8d2bb3962ab3603b7b1b8a2aec757b54f7c3', 'ergo-hetzber'),
('cd070307bb8e6ae6a4ccb778ce5e1511f2c8596cc505d762409ef8b3d5b684bf585184', 'mine1.187'),
('cd070337fe6d98e1c9d50f8f67dabc4ec4518eea7d6fff926add74dfd188666cb2565a', 'mine2.210');
