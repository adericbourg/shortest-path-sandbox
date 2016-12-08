CREATE TABLE station (
  id bigint primary key,
  name varchar(255),
  description varchar(2048),
  latitude double precision,
  longitude double precision,
  post_code varchar(10),
  department varchar(5)
);