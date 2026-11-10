#!/usr/bin/env bash
set -euo pipefail

# ==============================================================================
# BAYAN AL-HUDA — Secure Keystore Decoding & Preparation Script
# ==============================================================================
# Decodes ANDROID_KEYSTORE_BASE64 into an isolated temporary file outside the
# Git repository (e.g., ${RUNNER_TEMP}/bayan-release-upload.jks) with 0600
# permissions, validates keystore integrity with keytool, and ensures the
# signing alias expected by app/build.gradle.kts ("upload") is present.
# Never prints passwords, key material, or Base64 secrets to stdout/stderr.
# ==============================================================================

OUTPUT_KEYSTORE=""

while [[ $# -gt 0 ]]; do
  case "$1" in
    --output)
      OUTPUT_KEYSTORE="$2"
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

MISSING_VARS=()
[[ -z "${ANDROID_KEYSTORE_BASE64:-}" ]] && MISSING_VARS+=("ANDROID_KEYSTORE_BASE64")
[[ -z "${ANDROID_KEYSTORE_PASSWORD:-}" ]] && MISSING_VARS+=("ANDROID_KEYSTORE_PASSWORD")
[[ -z "${ANDROID_KEY_ALIAS:-}" ]] && MISSING_VARS+=("ANDROID_KEY_ALIAS")
[[ -z "${ANDROID_KEY_PASSWORD:-}" ]] && MISSING_VARS+=("ANDROID_KEY_PASSWORD")

if [[ ${#MISSING_VARS[@]} -gt 0 ]]; then
  echo "ERROR: Missing required Android release signing environment variables: ${MISSING_VARS[*]}" >&2
  echo "Refusing to build an unsigned or debug-signed production APK." >&2
  exit 1
fi

mkdir -p "$(dirname "${OUTPUT_KEYSTORE}")"
rm -f "${OUTPUT_KEYSTORE}"

# Decode Base64 keystore without echoing secret contents
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

echo "Verified release keystore at ${OUTPUT_KEYSTORE} (permissions 0600)."
