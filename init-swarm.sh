#!/usr/bin/env bash
eval "$(docker-machine env dui-1)"
echo "=> Init Swarm on dui-1"
IP=192.168.99.100
echo "=> Initializing on IP=${IP}"
docker swarm init --advertise-addr $IP
TOKEN=$(docker swarm join-token -q worker)

echo "=> Join swarm on dui-2"
eval "$(docker-machine env dui-2)"
docker swarm join --token ${TOKEN} ${IP}:2377

echo "=> Join swarm on dui-3"
eval "$(docker-machine env dui-3)"
docker swarm join --token ${TOKEN} ${IP}:2377

echo "=> Check swarm on dui-1"
eval "$(docker-machine env dui-1)"
docker node ls
