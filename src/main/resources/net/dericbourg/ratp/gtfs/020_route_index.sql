CREATE MATERIALIZED VIEW route_index (
    stop_id,
    route_id,
    short_name,
    long_name
)
AS
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
  WHERE rn = 1;
