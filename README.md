# buming 不明
Attempting to build a Concurrent, Modular, Domain Driven, Java 9 based Network application utilizing Streams &amp; Lambda's app. But its not clear if that is what it is going to be.

## TODO

* Gossip protocol for sending data

### Membership protocol

* Membership protocol should allow a process to leave
    * requires graceful shutdown!
* How do we start the membership lists, as we don't know each other: Broadcast or Multicast
    * [Baeldun UDP](http://www.baeldung.com/udp-in-java)
    * [Baeldung Multicast](http://www.baeldung.com/java-broadcast-multicast)
    * Broadcast for the initial discovery phase --> or can Multicast with a specific group work fine as well?
        * Means we need to reply to this and return our name (and counter?)
    * Multicast for the subsequent membership message
* Add internal message counter
* Add "system" message counter --> for the consensus part

## Run docker stack

### Create docker machines

If you already have a multi-node swarm, you can skip this step.

```bash
docker-machine create --driver virtualbox dui1
docker-machine create --driver virtualbox dui2
docker-machine create --driver virtualbox dui3
```

```bash
eval $(docker-machine env dui1)
```

```bash
docker swarm init --advertise-addr eth0
```

```bash
docker swarm join-token worker
```

```bash
eval $(docker-machine env dui2)
docker swarm join \
    --token SWMTKN-1-3bba2lvfpfzp1z2cqbf1u57vgyrpgia6qmeaa02kz7i2lp657h-azxhbo7f4n269ee7utl6ccqq0 \
    192.168.99.100:2377
```

```bash
eval $(docker-machine env dui3)
docker swarm join \
    --token SWMTKN-1-3bba2lvfpfzp1z2cqbf1u57vgyrpgia6qmeaa02kz7i2lp657h-azxhbo7f4n269ee7utl6ccqq0 \
    192.168.99.100:2377
```

### Build image

Every node that needs to run the application requires access to the image.

If you're using docker-machine for example, use the docker-compose build on every node. 

```bash
docker-compose build
```

```bash
docker node ls
```

### Create network

#### Weave Net

Resource: https://www.weave.works/docs/net/latest/install/plugin/plugin-v2/

``` bash
docker plugin install weaveworks/net-plugin:latest_release
docker plugin disable weaveworks/net-plugin:latest_release
docker plugin set weaveworks/net-plugin:latest_release WEAVE_MULTICAST=1
docker plugin enable weaveworks/net-plugin:latest_release
docker network create --driver=weaveworks/net-plugin:latest_release dui
```

#### macvlan (failed)

Resources:

* https://gist.github.com/thaJeztah/83e7469c85bac28ae90b5178a4919301 
* https://stackoverflow.com/questions/46511409/docker-swarm-container-with-macvlan-network-gets-wrong-gateway-no-internet-acc
* https://www.trueneutral.eu/2017/docker-networking.html
* https://hicu.be/docker-networking-macvlan-vlan-configuration

```bash
ip addr | grep mtu

# dui1:
docker network create --config-only --subnet=10.10.0.0/24 --gateway=10.10.0.1 -o parent=vboxnet4.10 --ip-range 10.10.0.0/24 dui-config-1
docker network create --config-only --subnet=10.10.0.0/24 --gateway=10.10.0.1 -o parent=vboxnet4.10 --ip-range 10.10.0.0/24 dui-config-2

# dui2:
docker network create --config-only --subnet=10.10.0.0/24 --gateway=10.10.0.1 -o parent=vboxnet4.10 --ip-range 10.10.0.0/24 dui-config-1

# dui3:
docker network create --config-only --subnet=10.10.0.0/24 --gateway=10.10.0.1 -o parent=vboxnet4.10 --ip-range 10.10.0.0/24 dui-config-1

# Master:
docker network create -d macvlan --scope swarm --internal --config-from dui-config-1 dui

```

```bash
./create-docker-network.sh
```

### Create stack

```bash
./create-docker-stack.sh
```

## Graceful shutdown

* https://docs.docker.com/engine/reference/builder/#run
* https://docs.docker.com/engine/reference/commandline/run/
* https://github.com/krallin/tini
* https://github.com/Yelp/dumb-init
* http://journal.thobe.org/2013/02/jvms-and-kill-signals.html
* https://docs.oracle.com/javase/9/troubleshoot/handle-signals-and-exceptions.htm#JSTGD356
* https://stackoverflow.com/questions/43122080/how-to-use-init-parameter-in-docker-run
* https://stackoverflow.com/questions/32315589/what-happens-when-the-jvm-is-terminated 
* https://www.ctl.io/developers/blog/post/gracefully-stopping-docker-containers/

### Run command

```bash
docker run --rm -ti --init --name dui-test dui
```

### In Dockerfile

```dockerfile
FROM debian:stable-slim
ENV TINI_VERSION v0.16.1
ADD https://github.com/krallin/tini/releases/download/${TINI_VERSION}/tini /tini
RUN chmod +x /tini
ENTRYPOINT ["/tini", "-v", "/usr/bin/dui/bin/dui"]
```

### Docker service/stack

When using the image as a swarm service, the signal send is a SIGTERM.

The JVM will just kill the application and not execute the [ShutdownHook](https://dzone.com/articles/know-jvm-series-2-shutdown).

In order to get that to happen, we should specify our containers should be stopped by SIGINT (interruption) instead.

```yaml
version: "3.5"

services:
  dui:
    image: dui
    build: .
    stop_signal: SIGINT
    networks:
      - dui
    deploy:
      mode: global
```
