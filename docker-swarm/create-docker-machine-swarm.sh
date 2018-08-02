#!/usr/bin/env bash
echo "=> Creating DUI swarm cluster"
TOKEN="$(curl -s -XPOST https://discovery.hub.docker.com/v1/clusters)"
echo "=> Retrieved token: ${TOKEN}"
echo "=> Create dui-1"
# Make sure this is the first and only virtualbox docker-machine VM being created/running
docker-machine create --driver virtualbox --swarm --swarm-discovery=token://${TOKEN} --swarm-experimental --swarm-master --swarm-addr 192.168.99.100  dui-1
echo "=> Create dui-2"
docker-machine create --driver virtualbox --swarm --swarm-discovery=token://${TOKEN} --swarm-experimental dui-2
echo "=> Create dui-3"
docker-machine create --driver virtualbox --swarm --swarm-discovery=token://${TOKEN} --swarm-experimental dui-3

./init-swarm.sh
./init-weave--net.sh

echo "=> Prepare dui-2"
eval "$(docker-machine env dui-2)"
docker-compose build

echo "=> Prepare dui-3"
eval "$(docker-machine env dui-3)"
docker-compose build

echo "=> Prepare dui-1"
eval "$(docker-machine env dui-1)"
docker-compose build

#echo "=> Run container"
#docker run --name dui-1 --network=dui dui

