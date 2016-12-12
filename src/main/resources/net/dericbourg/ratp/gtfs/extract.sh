#!/usr/bin/env bash

set -x

# Extracts only metro gtfs data
# ./extract.sh ~/tmp/gtfs_ratp METRO

GTFS_ROOT_DIR="$1"
TYPE="$2"
FILE_PATTERN="RATP_GTFS_$TYPE*.zip"

if [[ -z "$GTFS_ROOT_DIR" ]] ; then
	echo "No GTFS root directory specified. Exiting..."
	exit 1
fi

cd "$GTFS_ROOT_DIR"

for zipfile in $FILE_PATTERN; do
	directory=${zipfile%.zip}
	echo "Create directory $directory"
	mkdir -p "$directory"
	cd "$directory"
	echo "Unzipping file $zipfile"
	unzip ../$zipfile
	cd "$GTFS_ROOT_DIR"
done
