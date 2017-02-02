CREATE MATERIALIZED VIEW link (
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
