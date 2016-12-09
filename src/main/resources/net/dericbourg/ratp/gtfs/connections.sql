WITH CTE AS (
    SELECT
      *,
      row_number()
      OVER (PARTITION BY trip_id
        ORDER BY departure_time ASC) rownum
    FROM stop_time
)
SELECT
  dep.stop_id,
  dep.arrival_time,
  dep_stop.name,
  arr.stop_id,
  arr.arrival_time,
  arr_stop.name
FROM CTE dep
  INNER JOIN CTE arr
    ON dep.rownum + 1 = arr.rownum
       AND dep.trip_id = arr.trip_id
  JOIN stop dep_stop ON dep_stop.id = dep.stop_id
  JOIN stop arr_stop ON arr_stop.id = arr.stop_id;
