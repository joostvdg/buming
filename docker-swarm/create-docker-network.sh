
# https://stackoverflow.com/questions/46511409/docker-swarm-container-with-macvlan-network-gets-wrong-gateway-no-internet-acc
# docker network create -d macvlan --scope swarm -o encrypted dui
# docker network create -d macvlan --scope swarm --config-from swarm-multicast-config-only dui
docker plugin install weaveworks/net-plugin:latest_release
docker plugin disable weaveworks/net-plugin:latest_release
docker plugin set weaveworks/net-plugin:latest_release WEAVE_MULTICAST=1
docker plugin enable weaveworks/net-plugin:latest_release
# docker network create --driver=weaveworks/net-plugin:latest_release dui
