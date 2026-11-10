#!/usr/bin/env bash
set -euo pipefail

# ==============================================================================
# BAYAN AL-HUDA — Industrial-Grade Production Keystore Generator (Phase 3 / G1)
# ==============================================================================
# Developer : Zulqarnain Ahmad Sheikh
# Location  : Beerwah, Budgam, Kashmir, India
# Package   : com.bayanulhuda.com
# ==============================================================================

OUTPUT_PATH="${1:-${HOME}/bayan-al-huda-signing/bayan-al-huda-release.jks}"
KEY_ALIAS="upload"
VALIDITY_DAYS="36500"

mkdir -p "$(dirname "${OUTPUT_PATH}")"
chmod 700 "$(dirname "${OUTPUT_PATH}")"

if [[ -f "${OUTPUT_PATH}" ]]; then
  echo "ERROR: Keystore already exists at '${OUTPUT_PATH}'." >&2
  echo "Refusing to overwrite an existing production signing key!" >&2
  exit 1
fi

if [[ -z "${ANDROID_KEYSTORE_PASSWORD:-}" ]]; then
  STORE_PASS="$(openssl rand -base64 36 | tr -dc 'A-Za-z0-9' | head -c 32)"
else
  STORE_PASS="${ANDROID_KEYSTORE_PASSWORD}"
fi

keytool -genkeypair \
  -v \
  -storetype PKCS12 \
  -keystore "${OUTPUT_PATH}" \
  -alias "${KEY_ALIAS}" \
  -keyalg RSA \
  -keysize 4096 \
  -sigalg SHA256withRSA \
  -validity "${VALIDITY_DAYS}" \
  -storepass "${STORE_PASS}" \
  -keypass "${STORE_PASS}" \
  -dname "CN=Zulqarnain Ahmad Sheikh, OU=Bayan Al Huda, O=Bayan Al Huda, L=Beerwah Budgam, ST=Jammu and Kashmir, C=IN" >/dev/null 2>&1

chmod 600 "${OUTPUT_PATH}"

echo "================================================================================"
echo "BAYAN AL-HUDA — PRODUCTION RELEASE KEYSTORE GENERATED SUCCESSFULLY"
echo "================================================================================"
echo "Keystore File : ${OUTPUT_PATH} (permissions 0600)"
echo "Keystore Type : PKCS12"
echo "Key Algorithm : RSA 4096-bit (SHA256withRSA)"
echo "Key Alias     : ${KEY_ALIAS}"
echo "Validity      : ${VALIDITY_DAYS} days (100 years)"
echo "Developer     : Zulqarnain Ahmad Sheikh (Beerwah, Budgam, Kashmir, India)"
echo "Application ID: com.bayanulhuda.com"
echo "--------------------------------------------------------------------------------"
keytool -list -v -keystore "${OUTPUT_PATH}" -storepass "${STORE_PASS}" | grep -E "Owner:|Valid from:|SHA1:|SHA256:|Signature algorithm name:|Subject Public Key Algorithm:"
echo "--------------------------------------------------------------------------------"
echo "GitHub Actions Secret Values (Save immediately in your password manager):"
echo "  ANDROID_KEY_ALIAS         = ${KEY_ALIAS}"
echo "  ANDROID_KEYSTORE_PASSWORD = ${STORE_PASS}"
echo "  ANDROID_KEY_PASSWORD      = ${STORE_PASS}"
echo ""
echo "To generate ANDROID_KEYSTORE_BASE64 on Linux/macOS:"
echo "  base64 -w 0 \"${OUTPUT_PATH}\"   # (or 'base64 -i \"${OUTPUT_PATH}\" | tr -d \"\\n\"' on macOS)"
echo "================================================================================"
