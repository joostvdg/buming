#!/usr/bin/env bash
docker stack deploy --compose-file docker-stack-0.yml buming-0
docker stack deploy --compose-file docker-stack-1.yml buming-1
docker stack deploy --compose-file docker-stack-2.yml buming-2
docker stack deploy --compose-file docker-stack-3.yml buming-3

