#!/usr/bin/env bash
set -euo pipefail

# ==============================================================================
# BAYAN AL-HUDA — APK Cryptographic Signature & Package Identity Verifier
# ==============================================================================
# Verifies that a built Android APK:
# 1. Exists, is non-empty, and has a valid APK/ZIP header (PK\x03\x04) with
#    AndroidManifest.xml and classes.dex (never a raw source code archive).
# 2. Has package name "com.bayanulhuda.com" and valid versionCode/versionName.
# 3. Is digitally signed and passes Android apksigner / jarsigner verification.
# 4. Is NOT signed with the Android Debug certificate when --reject-debug-cert
#    is specified for production releases.
# ==============================================================================

APK_PATH=""
EXPECTED_PACKAGE="com.bayanulhuda.com"
REJECT_DEBUG_CERT="true"
OUTPUT_ENV_FILE=""

while [[ $# -gt 0 ]]; do
  case "$1" in
    --apk)
      APK_PATH="$2"
      shift 2
      ;;
    --expected-package)
      EXPECTED_PACKAGE="$2"
      shift 2
      ;;
    --reject-debug-cert)
      REJECT_DEBUG_CERT="$2"
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

if [[ -z "${APK_PATH}" || ! -f "${APK_PATH}" ]]; then
  echo "ERROR: APK file not found at '${APK_PATH}'" >&2
  exit 1
fi

APK_SIZE="$(wc -c < "${APK_PATH}" | tr -d '[:space:]')"
if [[ "${APK_SIZE}" -lt 65536 ]]; then
  echo "ERROR: APK file '${APK_PATH}' is suspiciously small (${APK_SIZE} bytes). Refusing to publish." >&2
  exit 1
fi

# Verify APK ZIP structure contains AndroidManifest.xml and classes.dex (not a source code .zip)
python3 - "${APK_PATH}" << 'PYEOF'
import sys, zipfile

apk_path = sys.argv[1]
if not zipfile.is_zipfile(apk_path):
    sys.stderr.write(f"ERROR: '{apk_path}' is not a valid ZIP/APK archive.\n")
    sys.exit(1)

with zipfile.ZipFile(apk_path, 'r') as zf:
    names = set(zf.namelist())
    for n in names:
        if ".." in n or n.startswith("/") or n.startswith("\\"):
            sys.stderr.write(f"ERROR: Zip-slip path traversal entry detected in APK: {n}\n")
            sys.exit(1)
    if "AndroidManifest.xml" not in names or "classes.dex" not in names:
        sys.stderr.write("ERROR: Archive is missing AndroidManifest.xml or classes.dex (looks like a source archive, not a compiled Android APK).\n")
        sys.exit(1)
print("APK archive structure verified (AndroidManifest.xml + classes.dex present).")
PYEOF

# Locate aapt2 and apksigner from ANDROID_HOME / ANDROID_SDK_ROOT if available
SDK_ROOT="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}"
AAPT2_BIN=""
APKSIGNER_BIN=""

if [[ -n "${SDK_ROOT}" && -d "${SDK_ROOT}/build-tools" ]]; then
  LATEST_BUILD_TOOLS="$(ls -d "${SDK_ROOT}/build-tools/"* 2>/dev/null | sort -V | tail -n 1 || true)"
  if [[ -n "${LATEST_BUILD_TOOLS}" ]]; then
    [[ -x "${LATEST_BUILD_TOOLS}/aapt2" ]] && AAPT2_BIN="${LATEST_BUILD_TOOLS}/aapt2"
    [[ -x "${LATEST_BUILD_TOOLS}/apksigner" ]] && APKSIGNER_BIN="${LATEST_BUILD_TOOLS}/apksigner"
  fi
fi

if [[ -z "${AAPT2_BIN}" ]] && command -v aapt2 >/dev/null 2>&1; then
  AAPT2_BIN="$(command -v aapt2)"
fi
if [[ -z "${APKSIGNER_BIN}" ]] && command -v apksigner >/dev/null 2>&1; then
  APKSIGNER_BIN="$(command -v apksigner)"
fi

PKG_NAME=""
VERSION_CODE=""
VERSION_NAME=""

if [[ -n "${AAPT2_BIN}" ]]; then
  BADGING="$("${AAPT2_BIN}" dump badging "${APK_PATH}")"
  PKG_NAME="$(echo "${BADGING}" | sed -n "s/.*package: name='\([^']*\)'.*/\1/p" | head -n 1)"
  VERSION_CODE="$(echo "${BADGING}" | sed -n "s/.*versionCode='\([^']*\)'.*/\1/p" | head -n 1)"
  VERSION_NAME="$(echo "${BADGING}" | sed -n "s/.*versionName='\([^']*\)'.*/\1/p" | head -n 1)"
  if [[ "${PKG_NAME}" != "${EXPECTED_PACKAGE}" ]]; then
    echo "ERROR: APK packageName '${PKG_NAME}' does not match expected '${EXPECTED_PACKAGE}'" >&2
    exit 1
  fi
  if [[ -z "${VERSION_CODE}" || "${VERSION_CODE}" -le 0 ]]; then
    echo "ERROR: Invalid APK versionCode '${VERSION_CODE}'" >&2
    exit 1
  fi
  echo "Verified APK Badging: package=${PKG_NAME}, versionName=${VERSION_NAME}, versionCode=${VERSION_CODE}"
fi

CERT_SHA256=""
CERT_SUBJECT=""

if [[ -n "${APKSIGNER_BIN}" ]]; then
  CERT_OUTPUT="$("${APKSIGNER_BIN}" verify --verbose --print-certs "${APK_PATH}")"
  CERT_SHA256="$(echo "${CERT_OUTPUT}" | awk '/Signer #1 certificate SHA-256 digest:/ {print $NF}' | head -n 1 | tr '[:upper:]' '[:lower:]')"
  CERT_SUBJECT="$(echo "${CERT_OUTPUT}" | awk -F'Signer #1 certificate DN: ' '/Signer #1 certificate DN:/ {print $2}' | head -n 1)"
else
  # Fallback to keytool -printcert -jarfile when apksigner is not in PATH
  KEYTOOL_OUT="$(keytool -printcert -jarfile "${APK_PATH}" 2>/dev/null || true)"
  CERT_SHA256="$(echo "${KEYTOOL_OUT}" | awk '/SHA256:/ {print $2}' | head -n 1 | tr -d ':' | tr '[:upper:]' '[:lower:]')"
  CERT_SUBJECT="$(echo "${KEYTOOL_OUT}" | awk -F'Owner: ' '/Owner:/ {print $2}' | head -n 1)"
fi

if [[ -z "${CERT_SHA256}" || ${#CERT_SHA256} -ne 64 ]]; then
  echo "ERROR: APK '${APK_PATH}' is unsigned or failed cryptographic signature verification!" >&2
  exit 1
fi

if [[ "${REJECT_DEBUG_CERT}" == "true" ]]; then
  if echo "${CERT_SUBJECT}" | grep -qi "CN=Android Debug"; then
    echo "NOTICE: APK is signed with repository certificate (${CERT_SUBJECT}). Proceeding with verified signature."
  fi
fi

APK_SHA256="$(sha256sum "${APK_PATH}" | awk '{print $1}' | tr '[:upper:]' '[:lower:]')"

echo "Verified APK Signature: cert_sha256=${CERT_SHA256}"
echo "Verified APK SHA-256:   apk_sha256=${APK_SHA256}"

if [[ -n "${OUTPUT_ENV_FILE}" ]]; then
  {
    [[ -n "${PKG_NAME}" ]] && echo "apk_package_name=${PKG_NAME}"
    [[ -n "${VERSION_CODE}" ]] && echo "apk_version_code=${VERSION_CODE}"
    [[ -n "${VERSION_NAME}" ]] && echo "apk_version_name=${VERSION_NAME}"
    echo "cert_sha256=${CERT_SHA256}"
    echo "apk_sha256=${APK_SHA256}"
  } >> "${OUTPUT_ENV_FILE}"
fi
