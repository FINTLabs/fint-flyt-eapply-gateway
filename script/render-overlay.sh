#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TEMPLATE_PATH="$ROOT/kustomize/templates/overlay.yaml.tpl"

ORG_SLUGS=(
  "afk-no"
  "agderfk-no"
  "bfk-no"
  "bym-oslo-kommune-no"
  "ffk-no"
  "fintlabs-no"
  "innlandetfylke-no"
  "mrfylke-no"
  "nfk-no"
  "ofk-no"
  "rogfk-no"
  "telemarkfylke-no"
  "tromsfylke-no"
  "trondelagfylke-no"
  "vestfoldfylke-no"
  "vlfk-no"
)

supports_environment() {
  local org_slug="$1"
  local environment="$2"

  case "$org_slug" in
    fintlabs-no)
      [[ "$environment" == "beta" ]]
      ;;
    bym-oslo-kommune-no|mrfylke-no|nfk-no)
      [[ "$environment" == "api" ]]
      ;;
    *)
      [[ "$environment" == "api" || "$environment" == "beta" ]]
      ;;
  esac
}

for org_slug in "${ORG_SLUGS[@]}"; do
  org_id_dot="${org_slug//-/.}"
  org_id_underscore="${org_slug//-/_}"

  for environment in api beta; do
    if ! supports_environment "$org_slug" "$environment"; then
      continue
    fi

    case "$environment" in
      api)
        base_path="/${org_slug}"
        vault_name="aks-api-vault"
        ;;
      beta)
        base_path="/beta/${org_slug}"
        vault_name="aks-beta-vault"
        ;;
      *)
        echo "Unsupported environment: ${environment}" >&2
        exit 1
        ;;
    esac

    export NAMESPACE="$org_slug"
    export ORG_ID_DOT="$org_id_dot"
    export APP_INSTANCE_LABEL="fint-flyt-eapply-gateway_${org_id_underscore}"
    export KAFKA_TOPIC="${org_slug}.flyt.*"
    export BASE_PATH="$base_path"
    export INGRESS_BASE_PATH="${base_path}/api/eapply"
    export STARTUP_PATH="${base_path}/actuator/health"
    export READINESS_PATH="${base_path}/actuator/health/readiness"
    export LIVENESS_PATH="${base_path}/actuator/health/liveness"
    export FINT_KAFKA_TOPIC_ORGID="$org_slug"
    export ITEM_PATH="vaults/${vault_name}/items/fint-flyt-eapply-oauth2-client-${org_slug}"

    target_dir="$ROOT/kustomize/overlays/$org_slug/$environment"
    mkdir -p "$target_dir"

    tmp="$(mktemp)"
    envsubst < "$TEMPLATE_PATH" > "$tmp"
    mv "$tmp" "$target_dir/kustomization.yaml"
  done
done
