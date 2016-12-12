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
  WITH route_index AS (
      SELECT
        tmp.stop_id,
        tmp.route_id,
        tmp.short_name,
        tmp.long_name
      FROM (
             SELECT
               st.stop_id,
               t.id,
               t.route_id,
               r.short_name,
               r.long_name,
               row_number()
               OVER (PARTITION BY st.stop_id) AS rn
             FROM stop_time st
               JOIN trip t ON t.id = st.trip_id
               JOIN route r ON r.id = t.route_id
           ) AS tmp
      WHERE rn = 1
  )
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
);

