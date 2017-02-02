CREATE MATERIALIZED VIEW enriched_connection (
    start_stop_id,
    leave_time,
    start_stop_name,
    arrival_stop_id,
    arrival_time,
    arrival_stop_name,
    connection_duration,
    route_id,
    route_short_name,
    route_long_name)
AS (
  WITH CTE AS (
      SELECT
        *,
        row_number()
        OVER (PARTITION BY trip_id
          ORDER BY departure_time ASC) rownum
      FROM stop_time
  )
  SELECT
    dep.stop_id                                  AS start_stop_id,
    dep.arrival_time                             AS leave_time,
    dep_stop.name                                AS start_stop_name,
    arr.stop_id                                  AS arrival_stop_id,
    arr.arrival_time                             AS arrival_time,
    arr_stop.name                                AS arrival_stop_name,
    (arr.arrival_time - dep.arrival_time) / 1000 AS connection_duration,
    r.id                                         AS route_id,
    r.short_name                                 AS route_short_name,
    r.long_name                                  AS route_long_name
  FROM CTE dep
    INNER JOIN CTE arr
      ON dep.rownum + 1 = arr.rownum
         AND dep.trip_id = arr.trip_id
    JOIN stop dep_stop ON dep_stop.id = dep.stop_id
    JOIN stop arr_stop ON arr_stop.id = arr.stop_id
    JOIN trip t ON t.id = dep.trip_id
    JOIN route r ON r.id = t.route_id
);
