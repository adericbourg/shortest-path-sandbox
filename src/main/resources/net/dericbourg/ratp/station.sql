CREATE TABLE station (
  id bigint primary key,
  name varchar(255),
  description varchar(2048),
  address varchar(1024),
  location geography(POINT, 4326),
  insee_code varchar(10),
  department varchar(5)
);