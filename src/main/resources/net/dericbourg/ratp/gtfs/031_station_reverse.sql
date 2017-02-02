CREATE MATERIALIZED VIEW station_reverse (
    start_stop_id,
    start_stop_name,
    arrival_stop_id,
    arrival_stop_name,
    connection_duration,
    route
) AS (
  SELECT
    ss.id   AS start_stop_id,
    ss.name AS start_stop_name,
    ts.id   AS arrival_stop_id,
    ts.name AS arrival_stop_name,
    30      AS connection_duration,
    sri.short_name
  FROM stop ss
    JOIN stop ts ON ss."name" = ts."name" AND ss.id <> ts.id
    JOIN route_index sri ON sri.stop_id = ss.id
    JOIN route_index tri ON tri.stop_id = ts.id
  WHERE sri.short_name = tri.short_name
);
