#!/bin/bash

# Get the PIDs of the running instances
pids=$(ps aux | grep 'rpc-test/target/rpc-test-1.0-SNAPSHOT-exec.jar' | grep -v grep | awk '{print $2}')

# Kill each PID
for pid in $pids; do
  kill -15 $pid
  echo "Stopped instance with PID $pid"
done