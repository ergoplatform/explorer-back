CREATE TABLE mempool_size (
  id   BIGSERIAL NOT NULL PRIMARY KEY,
  size BIGINT    NOT NULL,
  ts   BIGINT    NOT NULL
);

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
  block_mining_time BIGINT NOT NULL
);

CREATE INDEX "blockchain_stats__ts" ON blockchain_stats (ts);


