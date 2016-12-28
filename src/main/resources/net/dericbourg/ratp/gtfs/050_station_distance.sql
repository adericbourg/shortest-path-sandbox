CREATE TABLE station_distance (
  source           BIGINT NOT NULL,
  target           BIGINT NOT NULL,
  weight           INT    NOT NULL,
  amortized_weight INT    NOT NULL,
  trip             TEXT   NOT NULL,
  CONSTRAINT pk_station_distance PRIMARY KEY (source, target)
);
