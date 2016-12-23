#!/bin/sh

# Development Postgres instance
docker run --name ratp_stations -e POSTGRES_PASSWORD=postgres -p127.0.0.1:5432:5432 -d mdillon/postgis
