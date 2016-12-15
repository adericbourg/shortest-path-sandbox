CREATE OR REPLACE VIEW enriched_transfer (
    start_stop_id,
    start_stop_name,
    arrival_stop_id,
    arrival_stop_name,
    connection_duration,
    from_route_short_name,
    from_route_long_name,
    to_route_short_name,
    to_route_long_name)
AS (
  WITH raw_transfers AS (
      SELECT DISTINCT
        t.from_stop_id      AS start_stop_id,
        sts.name            AS start_stop_name,
        t.to_stop_id        AS arrival_stop_id,
        ars.name            AS arrival_stop_name,
        t.min_transfer_time AS connection_duration,
        sri.short_name      AS from_route_short_name,
        sri.long_name       AS from_route_long_name,
        asi.short_name      AS to_route_short_name,
        asi.long_name       AS to_route_long_name
      FROM transfer t
        JOIN stop sts ON sts.id = t.from_stop_id
        JOIN route_index sri ON sri.stop_id = sts.id
        JOIN stop ars ON ars.id = t.to_stop_id
        JOIN route_index asi ON asi.stop_id = ars.id
  )
  (
    SELECT
      start_stop_id,
      start_stop_name,
      arrival_stop_id,
      arrival_stop_name,
      connection_duration,
      from_route_short_name,
      from_route_long_name,
      to_route_short_name,
      to_route_long_name
    FROM raw_transfers
  )
  UNION
  (
    SELECT
      arrival_stop_id       AS start_stop_id,
      arrival_stop_name     AS start_stop_name,
      start_stop_id         AS arrival_stop_id,
      start_stop_name       AS arrival_stop_name,
      connection_duration,
      to_route_short_name   AS from_route_short_name,
      to_route_long_name    AS from_route_long_name,
      from_route_short_name AS to_route_short_name,
      from_route_long_name  AS to_route_long_name
    FROM raw_transfers
  )
);
