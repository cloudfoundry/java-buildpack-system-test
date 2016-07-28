#!/usr/bin/env sh

set -e

cd java-buildpack-system-test
./mvnw -q test
