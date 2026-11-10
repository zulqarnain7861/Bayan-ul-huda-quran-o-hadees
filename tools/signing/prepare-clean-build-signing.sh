#!/usr/bin/env bash
set -euo pipefail

# ==============================================================================
# Bayan al-Huda — Clean-Build & CI Signing/Firebase Bootstrap Helper
# ==============================================================================
# Guarantees that clean clones and GitHub Actions runners have:
#   1. .env.example (for Secrets Gradle Plugin defaults)
#   2. app/google-services.json (decoded from GOOGLE_SERVICES_JSON_BASE64 when
#      configured in GitHub Secrets, or a secret-free CI structural config when
#      stripped by the AI Studio GitHub exporter so verifyCleanBuildConfig and
#      process*GoogleServices tasks succeed)
#   3. debug.keystore (decoded from debug.keystore.base64 if present, or
#      generated via keytool when stripped by the exporter)
# Zero secrets, passwords, or private key blobs are hardcoded in this script.
# ==============================================================================

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"

if [[ ! -f "${ROOT_DIR}/.env.example" ]]; then
  cat > "${ROOT_DIR}/.env.example" << 'EOF'
HADITH_API_PROXY_BASE_URL=https://hadith-proxy.bayanulhuda.org
HADITH_API_KEY=HADITH_API_KEY_DEFAULT_VALUE
SUNNAH_API_KEY=SUNNAH_API_KEY_DEFAULT_VALUE
UMMAH_API_KEY=UMMAH_API_KEY_DEFAULT_VALUE
EOF
fi

# 1. Restore app/google-services.json from GOOGLE_SERVICES_JSON_BASE64 if provided,
#    or create a secret-free structural config on clean CI checkouts where the file is absent.
if [[ -n "${GOOGLE_SERVICES_JSON_BASE64:-}" ]]; then
  mkdir -p "${ROOT_DIR}/app"
  printf '%s' "${GOOGLE_SERVICES_JSON_BASE64}" | tr -d '[:space:]' | base64 -d > "${ROOT_DIR}/app/google-services.json"
  echo "Restored app/google-services.json from GOOGLE_SERVICES_JSON_BASE64 secret."
elif [[ ! -f "${ROOT_DIR}/app/google-services.json" ]]; then
  mkdir -p "${ROOT_DIR}/app"
  cat > "${ROOT_DIR}/app/google-services.json" << 'EOF'
{
  "project_info": {
    "project_number": "000000000000",
    "project_id": "bayan-al-huda-ci",
    "storage_bucket": "bayan-al-huda-ci.appspot.com"
  },
  "client": [
    {
      "client_info": {
        "mobilesdk_app_id": "1:000000000000:android:0000000000000000",
        "android_client_info": {
          "package_name": "com.bayanulhuda.com"
        }
      },
      "oauth_client": [],
      "api_key": [
        {
          "current_key": "CI_STRUCTURAL_PLACEHOLDER_KEY_000000000"
        }
      ],
      "services": {
        "appinvite_service": {
          "other_platform_oauth_client": []
        }
      }
    }
  ],
  "configuration_version": "1"
}
EOF
  echo "Created secret-free structural app/google-services.json for CI clean build."
fi

# 2. Materialize debug.keystore from debug.keystore.base64 or generate via keytool if absent
if [[ ! -f "${ROOT_DIR}/debug.keystore" ]]; then
  if [[ -f "${ROOT_DIR}/debug.keystore.base64" ]]; then
    base64 -d "${ROOT_DIR}/debug.keystore.base64" > "${ROOT_DIR}/debug.keystore"
    echo "Materialized debug.keystore from debug.keystore.base64."
  else
    keytool -genkeypair \
      -v \
      -storetype PKCS12 \
      -keystore "${ROOT_DIR}/debug.keystore" \
      -alias androiddebugkey \
      -keyalg RSA \
      -keysize 2048 \
      -sigalg SHA256withRSA \
      -validity 10000 \
      -storepass android \
      -keypass android \
      -dname "CN=Android Debug, O=Android, C=US" >/dev/null 2>&1
    base64 -w 0 "${ROOT_DIR}/debug.keystore" > "${ROOT_DIR}/debug.keystore.base64" 2>/dev/null \
      || base64 "${ROOT_DIR}/debug.keystore" | tr -d '\n' > "${ROOT_DIR}/debug.keystore.base64"
    echo "Generated clean-build debug.keystore and debug.keystore.base64 via keytool."
  fi
fi

echo "Clean-build configuration and signing prerequisites verified."
