CREATE TABLE IF NOT EXISTS server_ping_history
(
    server_address String,
    online_players Int32,
    max_players Int32,
    latency_ms Int32,
    online UInt8,
    event_timestamp DateTime64(3)
)
ENGINE = MergeTree()
ORDER BY (server_address, event_timestamp);