#!/bin/bash

source .env
CONTAINER_ALREADY_STARTED="CONTAINER_ALREADY_STARTED_PLACEHOLDER"
if [ ! -e $CONTAINER_ALREADY_STARTED ]; then
    touch $CONTAINER_ALREADY_STARTED
    echo "-- First container startup --"
    /usr/bin/supervisord -c /app/supervisord.conf
else
    /usr/bin/supervisord -c /app/supervisord.conf
    echo "-- Not first container startup --"
fi
