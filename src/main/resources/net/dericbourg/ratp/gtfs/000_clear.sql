TRUNCATE TABLE route CASCADE;
TRUNCATE TABLE station CASCADE;
TRUNCATE TABLE stop CASCADE;
TRUNCATE TABLE stop_time CASCADE;
TRUNCATE TABLE transfer CASCADE;
TRUNCATE TABLE trip CASCADE;
TRUNCATE TABLE station_distance CASCADE;

DROP VIEW all_links;
DROP VIEW enriched_connection;
DROP VIEW enriched_transfer;
DROP VIEW station_reverse;
DROP VIEW route_index;

