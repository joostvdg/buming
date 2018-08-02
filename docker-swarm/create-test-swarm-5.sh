#!/usr/bin/env bash
docker-machine create --driver virtualbox dui-1
docker-machine create --driver virtualbox dui-2
docker-machine create --driver virtualbox dui-3
docker-machine create --driver virtualbox dui-4
docker-machine create --driver virtualbox dui-5

eval "$(docker-machine env dui-1)"
IP=192.168.99.100
docker swarm init --advertise-addr $IP
WORKER_TOKEN=$(docker swarm join-token -q worker)
MANAGER_TOKEN=$(docker swarm join-token -q manager)

eval "$(docker-machine env dui-2)"
docker swarm join --token ${MANAGER_TOKEN} ${IP}:2377

eval "$(docker-machine env dui-3)"
docker swarm join --token ${MANAGER_TOKEN} ${IP}:2377

eval "$(docker-machine env dui-4)"
docker swarm join --token ${WORKER_TOKEN} ${IP}:2377

eval "$(docker-machine env dui-5)"
docker swarm join --token ${WORKER_TOKEN} ${IP}:2377

eval "$(docker-machine env dui-1)"
docker node ls

echo "=> Prepare dui-2"
eval "$(docker-machine env dui-2)"
docker plugin install weaveworks/net-plugin:2.1.3 --grant-all-permissions
docker plugin disable weaveworks/net-plugin:2.1.3
docker plugin set weaveworks/net-plugin:2.1.3 WEAVE_MULTICAST=1
docker plugin enable weaveworks/net-plugin:2.1.3

echo "=> Prepare dui-3"
eval "$(docker-machine env dui-3)"
docker plugin install weaveworks/net-plugin:2.1.3 --grant-all-permissions
docker plugin disable weaveworks/net-plugin:2.1.3
docker plugin set weaveworks/net-plugin:2.1.3 WEAVE_MULTICAST=1
docker plugin enable weaveworks/net-plugin:2.1.3

echo "=> Prepare dui-4"
eval "$(docker-machine env dui-4)"
docker plugin install weaveworks/net-plugin:2.1.3 --grant-all-permissions
docker plugin disable weaveworks/net-plugin:2.1.3
docker plugin set weaveworks/net-plugin:2.1.3 WEAVE_MULTICAST=1
docker plugin enable weaveworks/net-plugin:2.1.3

echo "=> Prepare dui-5"
eval "$(docker-machine env dui-5)"
docker plugin install weaveworks/net-plugin:2.1.3 --grant-all-permissions
docker plugin disable weaveworks/net-plugin:2.1.3
docker plugin set weaveworks/net-plugin:2.1.3 WEAVE_MULTICAST=1
docker plugin enable weaveworks/net-plugin:2.1.3

echo "=> Prepare dui-1"
eval "$(docker-machine env dui-1)"
docker plugin install weaveworks/net-plugin:2.1.3 --grant-all-permissions
docker plugin disable weaveworks/net-plugin:2.1.3
docker plugin set weaveworks/net-plugin:2.1.3 WEAVE_MULTICAST=1
docker plugin enable weaveworks/net-plugin:2.1.3
docker network create --driver=weaveworks/net-plugin:2.1.3 --opt works.weave.multicast=true --attachable dui1
docker network create --driver=weaveworks/net-plugin:2.1.3 --opt works.weave.multicast=true --attachable dui2
docker network create --driver=weaveworks/net-plugin:2.1.3 --opt works.weave.multicast=true --attachable dui3
docker network create --driver=overlay --attachable dui-ovl
