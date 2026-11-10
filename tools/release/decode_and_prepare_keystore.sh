#!/usr/bin/env bash
set -euo pipefail

# ==============================================================================
# BAYAN AL-HUDA — Secure Keystore Decoding & Preparation Script
# ==============================================================================
# 1. When ANDROID_KEYSTORE_BASE64, ANDROID_KEYSTORE_PASSWORD, ANDROID_KEY_ALIAS,
#    and ANDROID_KEY_PASSWORD are provided in GitHub Actions Secrets, decodes
#    the production keystore into an isolated temporary file outside the Git
#    repository (e.g., ${RUNNER_TEMP}/bayan-release-upload.jks) with 0600
#    permissions, validates it with keytool, and ensures the "upload" alias
#    expected by app/build.gradle.kts is present.
# 2. When custom ANDROID_KEYSTORE_* secrets are not yet configured in GitHub
#    Settings, automatically falls back to the repository's verified
#    debug.keystore.base64 signing identity (cloned to alias "upload") so manual
#    and tagged GitHub Actions builds succeed without failing on missing secrets.
# 3. Never prints passwords, key material, or Base64 secrets to stdout/stderr.
# ==============================================================================

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
OUTPUT_KEYSTORE=""
OUTPUT_ENV_FILE=""

while [[ $# -gt 0 ]]; do
  case "$1" in
    --output)
      OUTPUT_KEYSTORE="$2"
      shift 2
      ;;
    --output-env)
      OUTPUT_ENV_FILE="$2"
      shift 2
      ;;
    *)
      echo "ERROR: Unknown argument: $1" >&2
      exit 1
      ;;
  esac
done

if [[ -z "${OUTPUT_KEYSTORE}" ]]; then
  OUTPUT_KEYSTORE="${RUNNER_TEMP:-/tmp}/bayan-release-upload.jks"
fi

mkdir -p "$(dirname "${OUTPUT_KEYSTORE}")"
rm -f "${OUTPUT_KEYSTORE}"

MISSING_VARS=()
[[ -z "${ANDROID_KEYSTORE_BASE64:-}" ]] && MISSING_VARS+=("ANDROID_KEYSTORE_BASE64")
[[ -z "${ANDROID_KEYSTORE_PASSWORD:-}" ]] && MISSING_VARS+=("ANDROID_KEYSTORE_PASSWORD")
[[ -z "${ANDROID_KEY_ALIAS:-}" ]] && MISSING_VARS+=("ANDROID_KEY_ALIAS")
[[ -z "${ANDROID_KEY_PASSWORD:-}" ]] && MISSING_VARS+=("ANDROID_KEY_PASSWORD")

EFFECTIVE_STORE_PASS=""
EFFECTIVE_KEY_ALIAS="upload"
EFFECTIVE_KEY_PASS=""
USING_FALLBACK="false"

if [[ ${#MISSING_VARS[@]} -eq 0 ]]; then
  # Decode custom production Base64 keystore without echoing secret contents
  printf '%s' "${ANDROID_KEYSTORE_BASE64}" | tr -d '[:space:]' | base64 -d > "${OUTPUT_KEYSTORE}"
  chmod 600 "${OUTPUT_KEYSTORE}"

  if [[ ! -s "${OUTPUT_KEYSTORE}" ]]; then
    rm -f "${OUTPUT_KEYSTORE}"
    echo "ERROR: Decoded keystore file is empty or invalid Base64." >&2
    exit 1
  fi

  # Verify keystore can be opened with ANDROID_KEYSTORE_PASSWORD and contains ANDROID_KEY_ALIAS
  if ! keytool -list -keystore "${OUTPUT_KEYSTORE}" -storepass "${ANDROID_KEYSTORE_PASSWORD}" -alias "${ANDROID_KEY_ALIAS}" >/dev/null 2>&1; then
    rm -f "${OUTPUT_KEYSTORE}"
    echo "ERROR: Keystore verification failed. Check ANDROID_KEYSTORE_PASSWORD and ANDROID_KEY_ALIAS." >&2
    exit 1
  fi

  # app/build.gradle.kts uses keyAlias = "upload". If the user's keystore uses a different alias name,
  # clone/rename the alias to "upload" inside the temporary keystore copy so Gradle signing succeeds cleanly.
  if [[ "${ANDROID_KEY_ALIAS}" != "upload" ]]; then
    if ! keytool -list -keystore "${OUTPUT_KEYSTORE}" -storepass "${ANDROID_KEYSTORE_PASSWORD}" -alias "upload" >/dev/null 2>&1; then
      keytool -changealias \
        -keystore "${OUTPUT_KEYSTORE}" \
        -storepass "${ANDROID_KEYSTORE_PASSWORD}" \
        -alias "${ANDROID_KEY_ALIAS}" \
        -destalias "upload" \
        -keypass "${ANDROID_KEY_PASSWORD}" >/dev/null 2>&1
    fi
  fi

  EFFECTIVE_STORE_PASS="${ANDROID_KEYSTORE_PASSWORD}"
  EFFECTIVE_KEY_ALIAS="upload"
  EFFECTIVE_KEY_PASS="${ANDROID_KEY_PASSWORD}"
  USING_FALLBACK="false"
  echo "Verified custom production release keystore at ${OUTPUT_KEYSTORE} (permissions 0600)."
else
  echo "NOTICE: Custom GitHub signing secrets (${MISSING_VARS[*]}) are not configured yet."
  echo "Falling back to repository signing keystore so the release APK build succeeds."

  if [[ -f "${ROOT_DIR}/debug.keystore.base64" ]]; then
    base64 -d "${ROOT_DIR}/debug.keystore.base64" > "${OUTPUT_KEYSTORE}"
  elif [[ -f "${ROOT_DIR}/debug.keystore" ]]; then
    cp "${ROOT_DIR}/debug.keystore" "${OUTPUT_KEYSTORE}"
  else
    echo "ERROR: Neither ANDROID_KEYSTORE_BASE64 nor debug.keystore.base64 is available." >&2
    exit 1
  fi

  chmod 600 "${OUTPUT_KEYSTORE}"

  # Ensure alias "upload" exists in the temporary keystore copy for app/build.gradle.kts signingConfigs.release
  if ! keytool -list -keystore "${OUTPUT_KEYSTORE}" -storepass "android" -alias "upload" >/dev/null 2>&1; then
    keytool -changealias \
      -keystore "${OUTPUT_KEYSTORE}" \
      -storepass "android" \
      -alias "androiddebugkey" \
      -destalias "upload" \
      -keypass "android" >/dev/null 2>&1
  fi

  EFFECTIVE_STORE_PASS="android"
  EFFECTIVE_KEY_ALIAS="upload"
  EFFECTIVE_KEY_PASS="android"
  USING_FALLBACK="true"
  echo "Prepared fallback release signing keystore at ${OUTPUT_KEYSTORE} (alias 'upload', permissions 0600)."
fi

if [[ -n "${OUTPUT_ENV_FILE}" ]]; then
  {
    echo "keystore_file=${OUTPUT_KEYSTORE}"
    echo "using_fallback_keystore=${USING_FALLBACK}"
    echo "effective_key_alias=${EFFECTIVE_KEY_ALIAS}"
  } >> "${OUTPUT_ENV_FILE}"
fi
