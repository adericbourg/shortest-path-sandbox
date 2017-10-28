#!/usr/bin/env bash

PGHOST=${PGHOST:-localhost}
PGPORT=${PGPORT:-5432}

PGUSER=${PGUSER:-postgres}
PGPASSWORD=${PGPASSWORD:-postgres}
PGDB=${PGDB:-postgres}

PGPASSWORD=${PGPASSWORD} psql -h ${PGHOST} -p ${PGPORT} -U ${PGUSER} -d ${PGDB} -a -f $1 net/dericbourg/ratp/gtfs/010_db.sql
PGPASSWORD=${PGPASSWORD} psql -h ${PGHOST} -p ${PGPORT} -U ${PGUSER} -d ${PGDB} -a -f $1 net/dericbourg/ratp/gtfs/020_route_index.sql
PGPASSWORD=${PGPASSWORD} psql -h ${PGHOST} -p ${PGPORT} -U ${PGUSER} -d ${PGDB} -a -f $1 net/dericbourg/ratp/gtfs/030_connections.sql
PGPASSWORD=${PGPASSWORD} psql -h ${PGHOST} -p ${PGPORT} -U ${PGUSER} -d ${PGDB} -a -f $1 net/dericbourg/ratp/gtfs/031_station_reverse.sql
PGPASSWORD=${PGPASSWORD} psql -h ${PGHOST} -p ${PGPORT} -U ${PGUSER} -d ${PGDB} -a -f $1 net/dericbourg/ratp/gtfs/032_transfers.sql
PGPASSWORD=${PGPASSWORD} psql -h ${PGHOST} -p ${PGPORT} -U ${PGUSER} -d ${PGDB} -a -f $1 net/dericbourg/ratp/gtfs/040_link.sql
PGPASSWORD=${PGPASSWORD} psql -h ${PGHOST} -p ${PGPORT} -U ${PGUSER} -d ${PGDB} -a -f $1 net/dericbourg/ratp/gtfs/050_station_distance.sql
