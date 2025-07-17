#!/bin/bash
while true; do
  docker-compose restart web-server-1
  sleep 300
  docker-compose restart web-server-2
  sleep 300
done 