#!/usr/bin/env bash
NUMBER_DEFAULT=1
NUMBER=${1-$NUMBER_DEFAULT}

docker run --rm -ti --init --name dui-test-${NUMBER} --network dui-test dui-jdk10
