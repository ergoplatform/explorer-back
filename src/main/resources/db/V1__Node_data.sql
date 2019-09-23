CREATE TABLE node_headers (
    id VARCHAR(64) PRIMARY KEY,
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
    miner_pk VARCHAR NOT NULL,
    w VARCHAR NOT NULL,
    n VARCHAR NOT NULL,
    d VARCHAR NOT NULL,
    votes VARCHAR NOT NULL,
    main_chain BOOLEAN NOT NULL
);

CREATE INDEX "node_headers__parent_id" ON node_headers (parent_id);
CREATE INDEX "node_headers__height" ON node_headers (height);
CREATE INDEX "node_headers__ts" ON node_headers (timestamp);
CREATE INDEX "node_headers__main_chain" ON node_headers (main_chain);
CREATE INDEX "node_headers__d" ON node_headers (d);

CREATE TABLE node_extensions (
    header_id VARCHAR(64) PRIMARY KEY REFERENCES node_headers (id),
    digest VARCHAR(64) NOT NULL,
    fields JSON NOT NULL
);

CREATE TABLE node_ad_proofs (
    header_id VARCHAR(64) PRIMARY KEY REFERENCES node_headers (id),
    proof_bytes VARCHAR NOT NULL,
    digest VARCHAR NOT NULL
);

/*
    Block stats
 */
CREATE TABLE blocks_info (
    header_id VARCHAR(64) PRIMARY KEY REFERENCES node_headers (id),
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

/*
    Stats table indexes. By height and ts.
 */
CREATE INDEX "blocks_info__height" ON node_headers (height);
CREATE INDEX "blocks_info__ts" ON node_headers (timestamp);

CREATE TABLE node_transactions (
    id VARCHAR(64) NOT NULL,
    header_id VARCHAR(64) REFERENCES node_headers (id),
    coinbase BOOLEAN NOT NULL,
    timestamp BIGINT NOT NULL,
    size BIGINT NOT NULL,
    PRIMARY KEY(id, header_id)
);

CREATE INDEX "node_transactions__header_id" on node_transactions (header_id);
CREATE INDEX "node_transactions__timestamp" on node_transactions (timestamp);

/*
    Table that represents inputs in ergo transactions.
    Has tx_id field that point to the tx where this input was spent.
 */
CREATE TABLE node_inputs (
    box_id VARCHAR(64) NOT NULL,
    tx_id VARCHAR(64) NOT NULL,
    proof_bytes VARCHAR NOT NULL,
    extension JSON NOT NULL
);

/*
    Indexes that being used by inputs table.
 */
CREATE INDEX "node_inputs__tx_id" on node_inputs (tx_id);
CREATE INDEX "node_inputs__box_id" on node_inputs (box_id);

/*
    Table that represents outputs in ergo transactions.
    Has tx_id field that point to the tx where this output was created.
 */
CREATE TABLE node_outputs (
    box_id VARCHAR(64) NOT NULL,
    tx_id VARCHAR(64) NOT NULL,
    value BIGINT NOT NULL,
    creation_height INTEGER NOT NULL,
    index INTEGER NOT NULL,
    ergo_tree VARCHAR NOT NULL,
    address VARCHAR NOT NULL,
    additional_registers JSON NOT NULL,
    timestamp BIGINT NOT NULL
);

CREATE INDEX "node_outputs__box_id" on node_outputs (box_id);
CREATE INDEX "node_outputs__tx_id" on node_outputs (tx_id);
CREATE INDEX "node_outputs__address" on node_outputs (address);
CREATE INDEX "node_outputs__ts" on node_outputs (timestamp);

CREATE TABLE node_assets (
    id VARCHAR(64) NOT NULL,
    box_id VARCHAR(64) NOT NULL,
    value BIGINT NOT NULL,
    PRIMARY KEY (id, box_id)
);

CREATE INDEX "node_assets__box_id" on node_assets (box_id);

/*
    Table for storing names for known miners.
 */
CREATE TABLE known_miners (
    miner_address VARCHAR PRIMARY KEY,
    miner_name VARCHAR NOT NULL
);

