CREATE OR REPLACE VIEW all_links (
    start_stop_id,
    start_stop_name,
    arrival_stop_id,
    arrival_stop_name,
    connection_duration
)
AS (
  SELECT
    start_stop_id,
    start_stop_name,
    arrival_stop_id,
    arrival_stop_name,
    avg(connection_duration) AS connection_duration
  FROM (
         SELECT
           start_stop_id,
           start_stop_name,
           arrival_stop_id,
           arrival_stop_name,
           connection_duration
         FROM enriched_connection
         UNION
         SELECT
           start_stop_id,
           start_stop_name,
           arrival_stop_id,
           arrival_stop_name,
           connection_duration
         FROM enriched_transfer
         UNION
         SELECT
           start_stop_id,
           start_stop_name,
           arrival_stop_id,
           arrival_stop_name,
           connection_duration
         FROM station_reverse
       ) u
  GROUP BY start_stop_id, arrival_stop_id, start_stop_name, arrival_stop_name
);
