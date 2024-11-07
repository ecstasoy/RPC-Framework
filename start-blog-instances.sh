#!/bin/bash

# Loop to start instances from 1 to 5
for i in {1..5}; do
  export SERVER_PORT=$((8094 + i))
  export NETTY_PORT=$((50008 + i))

  # Run each instance in the background with a unique profile
  java -jar \
    -DSERVER_PORT=${SERVER_PORT} \
    -DNETTY_PORT=${NETTY_PORT} \
    rpc-example/rpc-blog-service/target/rpc-blog-service-1.0-SNAPSHOT.jar \
    --spring.profiles.active=instance$i &

  echo "Started instance $i with SERVER_PORT=${SERVER_PORT} and NETTY_PORT=${NETTY_PORT}"
done

# Wait for all background processes to finish
wait