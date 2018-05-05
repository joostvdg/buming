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

## Create Kubernetes stack

### Tectonic

* https://coreos.com/tectonic/docs/latest/tutorials/sandbox/install.html

## Graceful shutdown (Docker Swarm)

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

## Graceful shutdown (Kubernetes)

So far we've utilized the utilities from Docker itself in conjunction with it's native Docker Swarm orchestrator.

Unfortunately, when it comes to popularity [Kubernetes beats Swarm hands down](https://platform9.com/blog/kubernetes-docker-swarm-compared/).

So this isn't complete if it doesn't also do graceful shutdown in Kubernetes. 

## In Dockerfile

Our original file had to be changed, as Debian's Slim image doesn't actually contain the kill package.
And we need a kill package, as we cannot instruct Kubernetes to issue a specific SIGNAL.
Instead, we can issue a [PreStop exec command](https://kubernetes.io/docs/concepts/containers/container-lifecycle-hooks/), which we can utilise to execute a [killall](https://packages.debian.org/wheezy/psmisc) java [-INT](https://www.tecmint.com/how-to-kill-a-process-in-linux/).

The command will be specified in the Kubernetes deployment definition below.

```dockerfile
FROM openjdk:9-jdk AS build

RUN mkdir -p /usr/src/mods/jars
RUN mkdir -p /usr/src/mods/compiled

COPY . /usr/src
WORKDIR /usr/src

RUN javac -Xlint:unchecked -d /usr/src/mods/compiled --module-source-path /usr/src/src $(find src -name "*.java")
RUN jar --create --file /usr/src/mods/jars/joostvdg.dui.logging.jar --module-version 1.0 -C /usr/src/mods/compiled/joostvdg.dui.logging .
RUN jar --create --file /usr/src/mods/jars/joostvdg.dui.api.jar --module-version 1.0 -C /usr/src/mods/compiled/joostvdg.dui.api .
RUN jar --create --file /usr/src/mods/jars/joostvdg.dui.client.jar --module-version 1.0 -C /usr/src/mods/compiled/joostvdg.dui.client .
RUN jar --create --file /usr/src/mods/jars/joostvdg.dui.server.jar --module-version 1.0  -e com.github.joostvdg.dui.server.cli.DockerApp\
    -C /usr/src/mods/compiled/joostvdg.dui.server .

RUN rm -rf /usr/bin/dui-image
RUN jlink --module-path /usr/src/mods/jars/:/${JAVA_HOME}/jmods \
    --add-modules joostvdg.dui.api \
    --add-modules joostvdg.dui.logging \
    --add-modules joostvdg.dui.server \
    --add-modules joostvdg.dui.client \
    --launcher dui=joostvdg.dui.server \
    --output /usr/bin/dui-image

RUN ls -lath /usr/bin/dui-image
RUN ls -lath /usr/bin/dui-image
RUN /usr/bin/dui-image/bin/java --list-modules

FROM debian:stable-slim
LABEL authors="Joost van der Griendt <joostvdg@gmail.com>"
LABEL version="0.1.0"
LABEL description="Docker image for playing with java applications in a concurrent, parallel and distributed manor."
# Add Tini - it is already included: https://docs.docker.com/engine/reference/commandline/run/
ENV TINI_VERSION v0.16.1
ADD https://github.com/krallin/tini/releases/download/${TINI_VERSION}/tini /tini
RUN chmod +x /tini
ENTRYPOINT ["/tini", "-vv","-g", "--", "/usr/bin/dui/bin/dui"]
ENV DATE_CHANGED="20180120-1525"
RUN apt-get update && apt-get install --no-install-recommends -y psmisc=22.* && rm -rf /var/lib/apt/lists/*
COPY --from=build /usr/bin/dui-image/ /usr/bin/dui
RUN /usr/bin/dui/bin/java --list-modules
```

## Kubernetes Deployment

So here we have the image's K8s [Deployment]() descriptor.

Including the Pod's [lifecycle]() ```preStop``` with a exec style command. You should know by now [why we prefer that](http://www.johnzaccone.io/entrypoint-vs-cmd-back-to-basics/).

```yaml
apiVersion: extensions/v1beta1
kind: Deployment
metadata:
  name: dui-deployment
  namespace: default
  labels:
    k8s-app: dui
spec:
  replicas: 3
  template:
    metadata:
      labels:
        k8s-app: dui
    spec:
      containers:
        - name: master
          image: caladreas/buming
          ports:
            - name: http
              containerPort: 7777
          lifecycle:
            preStop:
              exec:
                command: ["killall", "java" , "-INT"]
      terminationGracePeriodSeconds: 60
```

## Plan/TODO

* Leave membership message propagation
    * digest check should be on SEND message, as other might not have received it 
* Check for current active members is broken
    * leavers are undetected!
* ~~CLEAN UP RECENT MESSAGES <-- check if this is done~~ it gets done
* Send membership list to others <-- current step
* send message digest to others
    * refresh signing hash every x rotations of multicast?
* lamport timestamp ordering
* keep a set of recent digests received
    * sign it?
* propagate leave messages
    * ONLY propagate messages not yet send (check digest list)
* Decide on a leader
* Have a API for data entries
* Store data entries
* Share data entries with other nodes
* Have a Go (lang) client
