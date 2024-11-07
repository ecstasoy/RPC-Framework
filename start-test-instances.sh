#!/bin/bash

# Loop to start instances from 1 to 10
for i in {1..10}; do
  # Run each instance in the background with a unique profile
  java -jar rpc-test/target/rpc-test-1.0-SNAPSHOT-exec.jar --spring.profiles.active=instance$i &
  echo "Started instance $i with profile instance$i"
done

# Wait for all background processes to finish
wait