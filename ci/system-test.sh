#!/usr/bin/env bash

set -euo pipefail

# shellcheck source=common.sh
source "$(dirname "$0")"/common.sh

CREDENTIALS=$(cat "${ROOT}"/environment/credentials.json)

TEST_HOST=$(jq -n -r --argjson credentials "${CREDENTIALS}" '"https://api.sys.\($credentials.name).cf-app.com"')
export TEST_HOST

TEST_PASSWORD=$(jq -n -r --argjson credentials "${CREDENTIALS}" '$credentials.password')
export TEST_PASSWORD

TEST_USERNAME=$(jq -n -r --argjson credentials "${CREDENTIALS}" '$credentials.username')
export TEST_USERNAME

cd "${ROOT}"/java-buildpack-system-test
./mvnw -q test
