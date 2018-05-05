#!/usr/bin/env bash
TAG=${1}
git tag -a ${TAG} -m "my version v${1}"
git push origin ${TAG}
