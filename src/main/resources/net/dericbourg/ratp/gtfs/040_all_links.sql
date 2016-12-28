CREATE OR REPLACE VIEW all_links (
    start_stop_id,
    start_stop_name,
    arrival_stop_id,
    arrival_stop_name,
    connection_duration,
    source
)
AS (
  SELECT
    start_stop_id,
    start_stop_name,
    arrival_stop_id,
    arrival_stop_name,
    avg(connection_duration) AS connection_duration,
    source
  FROM (
         SELECT
           start_stop_id,
           start_stop_name,
           arrival_stop_id,
           arrival_stop_name,
           connection_duration,
           'connection' AS source
         FROM enriched_connection
         UNION
         SELECT
           start_stop_id,
           start_stop_name,
           arrival_stop_id,
           arrival_stop_name,
           connection_duration,
           'transfer' AS source
         FROM enriched_transfer
         UNION
         SELECT
           start_stop_id,
           start_stop_name,
           arrival_stop_id,
           arrival_stop_name,
           connection_duration,
           'self' AS source
         FROM station_reverse
       ) u
  GROUP BY start_stop_id, arrival_stop_id, start_stop_name, arrival_stop_name, source
);

CREATE TABLE link (
  start_stop_id       INT           NOT NULL,
  start_stop_name     VARCHAR(1024) NULL,
  arrival_stop_id     INT           NOT NULL,
  arrival_stop_name   VARCHAR(1024) NULL,
  connection_duration INT           NULL,
  source              VARCHAR(10)   NULL
);

INSERT INTO link
  SELECT *
  FROM all_links;
