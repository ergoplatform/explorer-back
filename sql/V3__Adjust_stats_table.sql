ALTER TABLE blockchain_stats ADD COLUMN version VARCHAR DEFAULT  '0.2.4';
ALTER TABLE blockchain_stats ADD COLUMN supply BIGINT DEFAULT 0;
ALTER TABLE blockchain_stats ADD COLUMN market_cap BIGINT DEFAULT 0;
ALTER TABLE blockchain_stats ADD COLUMN hashrate BIGINT DEFAULT 0;
ALTER TABLE blockchain_stats ADD COLUMN market_price_usd BIGINT DEFAULT 0;
ALTER TABLE blockchain_stats ADD COLUMN market_price_btc BIGINT DEFAULT 0;

ALTER TABLE blockchain_stats ALTER COLUMN version DROP DEFAULT;
ALTER TABLE blockchain_stats ALTER COLUMN supply DROP DEFAULT;
ALTER TABLE blockchain_stats ALTER COLUMN market_cap DROP DEFAULT;
ALTER TABLE blockchain_stats ALTER COLUMN hashrate DROP DEFAULT;
ALTER TABLE blockchain_stats ALTER COLUMN market_price_usd DROP DEFAULT;
ALTER TABLE blockchain_stats ALTER COLUMN market_price_btc DROP DEFAULT;

ALTER TABLE blockchain_stats ALTER COLUMN version SET NOT NULL;
ALTER TABLE blockchain_stats ALTER COLUMN supply SET NOT NULL;
ALTER TABLE blockchain_stats ALTER COLUMN market_cap SET NOT NULL;
ALTER TABLE blockchain_stats ALTER COLUMN hashrate SET NOT NULL;
ALTER TABLE blockchain_stats ALTER COLUMN market_price_usd SET NOT NULL;
ALTER TABLE blockchain_stats ALTER COLUMN market_price_btc SET NOT NULL;