#!/usr/bin/env bash

set -e

pushd java-buildpack-system-test
  ./mvnw -q test
popd
