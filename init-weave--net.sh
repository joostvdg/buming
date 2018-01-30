#!/usr/bin/env bash
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

echo "=> Prepare dui-1"
eval "$(docker-machine env dui-1)"
docker plugin install weaveworks/net-plugin:2.1.3 --grant-all-permissions
docker plugin disable weaveworks/net-plugin:2.1.3
docker plugin set weaveworks/net-plugin:2.1.3 WEAVE_MULTICAST=1
docker plugin enable weaveworks/net-plugin:2.1.3
docker network create --driver=weaveworks/net-plugin:2.1.3 --opt works.weave.multicast=true --attachable dui

