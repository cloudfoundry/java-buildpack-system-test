#!/usr/bin/env bash

set -euo pipefail

# shellcheck source=common.sh
source "$(dirname "$0")"/common.sh

POOL_NAME=$(curl \
  --fail \
  --location \
  --show-error \
  --silent \
  --header 'Accept: application/json' \
  "https://environments.toolsmiths.cf-app.com/pool_names" \
  | jq -r '.pool_names.gcp | map(select(contains("us"))) | map(select(test("3_[0-9]$"))) | .[]' | sort -Vr | head -1)

printf "Claiming environment from %s\n" "${POOL_NAME}"

CLAIM=$(curl \
  --fail \
  --location \
  --show-error \
  --silent \
  --header 'Accept: application/json' \
  --request "POST" \
  "https://environments.toolsmiths.cf-app.com/pooled_gcp_engineering_environments/claim?api_token=${API_TOKEN}&pool_name=${POOL_NAME}&notes=Claimed%20by%20Java%20Buildpack%20CI")

printf "Claimed %s\n" "$(jq -n -r --argjson claim "${CLAIM}" '$claim.name')"

CREDENTIALS=$(om \
  --target "$(jq -n -r --argjson claim "${CLAIM}" '$claim.ops_manager.url')" \
  --username "$(jq -n -r --argjson claim "${CLAIM}" '$claim.ops_manager.username')" \
  --password "$(jq -n -r --argjson claim "${CLAIM}" '$claim.ops_manager.password')" \
  credentials \
  --product-name cf \
  --credential-reference .uaa.admin_credentials \
  --format json)

  jq \
    -n -r \
    --argjson claim "${CLAIM}" \
    --argjson credentials "${CREDENTIALS}" \
    '{ name: $claim.name, username: $credentials.identity, password: $credentials.password }' \
    > "${ROOT}"/environment/credentials.json
