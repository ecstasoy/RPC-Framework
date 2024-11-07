#!/bin/bash

for i in $(seq 8090 8099); do
  curl -X GET http://localhost:"$i"/monitor/server/statistics
  sleep 1
done