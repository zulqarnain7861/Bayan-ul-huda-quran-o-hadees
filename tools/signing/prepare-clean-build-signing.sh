#!/usr/bin/env bash
set -euo pipefail

# Bayan al-Huda — Clean-Build & Signing Verification Helper
# Ensures clean clones and CI runners have all required signing and secret defaults
# without committing private release keys to source control.

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"

if [[ ! -f "${ROOT_DIR}/.env.example" ]]; then
  echo "ERROR: Missing .env.example at ${ROOT_DIR}/.env.example" >&2
  exit 1
fi

if [[ ! -f "${ROOT_DIR}/debug.keystore" && -f "${ROOT_DIR}/debug.keystore.base64" ]]; then
  base64 -d "${ROOT_DIR}/debug.keystore.base64" > "${ROOT_DIR}/debug.keystore"
  echo "Materialized debug.keystore from debug.keystore.base64 for clean build."
fi

echo "Clean-build configuration and signing prerequisites verified."
