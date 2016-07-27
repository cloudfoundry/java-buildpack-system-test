#!/usr/bin/env bash

set -e

java -version

pushd java-buildpack-system-test
  ./mvnw -q test
popd
