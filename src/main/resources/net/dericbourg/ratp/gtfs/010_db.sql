CREATE TABLE route (
  id          BIGINT PRIMARY KEY,
  short_name  VARCHAR(50)   NOT NULL,
  long_name   VARCHAR(1024) NOT NULL,
  description VARCHAR(2048) NOT NULL
);

CREATE TABLE stop (
  id             BIGINT PRIMARY KEY,
  name           VARCHAR(1024)    NOT NULL,
  description    VARCHAR(2048),
  latitude       DOUBLE PRECISION NOT NULL,
  longitude      DOUBLE PRECISION NOT NULL,
  location_type  BIGINT           NOT NULL,
  parent_station VARCHAR(1024)
);

SELECT AddGeometryColumn('stop', 'coordinates', '4326', 'POINT', 2);

UPDATE stop s
SET coordinates = comp.coordinates
FROM (SELECT
        id,
        ST_GeomFromText('POINT(' || latitude || ' ' || longitude || ')', 4326) AS coordinates
      FROM stop) AS comp
WHERE comp.id = s.id;

CREATE TABLE trip (
  id         BIGINT PRIMARY KEY,
  route_id   BIGINT        NOT NULL,
  service_id BIGINT        NOT NULL,
  short_name VARCHAR(48)   NOT NULL,
  long_name  VARCHAR(1024) NOT NULL,
  CONSTRAINT fk_trip_route FOREIGN KEY (route_id) REFERENCES route (id)
);


CREATE TABLE stop_time (
  id             BIGSERIAL PRIMARY KEY,
  trip_id        BIGINT NOT NULL,
  arrival_time   BIGINT NOT NULL,
  departure_time BIGINT NOT NULL,
  stop_id        BIGINT NOT NULL,
  CONSTRAINT fk_stop_time_stop FOREIGN KEY (stop_id) REFERENCES stop (id),
  CONSTRAINT fk_stop_time_trip FOREIGN KEY (trip_id) REFERENCES trip (id)
);

CREATE TABLE transfer (
  id                BIGSERIAL PRIMARY KEY,
  from_stop_id      BIGINT NOT NULL,
  to_stop_id        BIGINT NOT NULL,
  transfer_type     VARCHAR(16),
  min_transfer_time BIGINT,
  CONSTRAINT fk_transfer_from_stop FOREIGN KEY (from_stop_id) REFERENCES stop (id),
  CONSTRAINT fk_transfer_to_stop FOREIGN KEY (to_stop_id) REFERENCES stop (id)
);

