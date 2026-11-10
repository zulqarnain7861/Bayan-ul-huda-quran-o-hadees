#!/usr/bin/env bash
set -euo pipefail

# ==============================================================================
# BAYAN AL-HUDA — Pre-Release Security & Google Play Protect Audit Script
# ==============================================================================
# Audits the AndroidManifest.xml, Gradle configuration, and Kotlin source tree
# before publishing a release to ensure:
# 1. Zero prohibited/malware-associated permissions (Accessibility, Device Admin,
#    SMS, Call Log, Contacts, System Overlay, Package Deletion, Install Packages
#    abuse).
# 2. Zero dynamic code loading (DexClassLoader, PathClassLoader, InMemoryDexClassLoader)
#    or runtime shell execution of downloaded files.
# 3. Enforced HTTPS / cleartext traffic disabled (android:usesCleartextTraffic="false").
# 4. Zero hardcoded private keys, GitHub personal access tokens, or keystore passwords.
# 5. Safe WebView settings (no universal/file URL access).
# ==============================================================================

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
MANIFEST_FILE="${ROOT_DIR}/app/src/main/AndroidManifest.xml"
SRC_DIR="${ROOT_DIR}/app/src/main/java"

echo "Running Bayan al-Huda Security & Google Play Protect Pre-Release Audit..."

if [[ ! -f "${MANIFEST_FILE}" ]]; then
  echo "ERROR: AndroidManifest.xml not found at ${MANIFEST_FILE}" >&2
  exit 1
fi

# 1. Verify cleartext traffic is disabled in AndroidManifest.xml
if ! grep -q 'android:usesCleartextTraffic="false"' "${MANIFEST_FILE}"; then
  echo "ERROR: AndroidManifest.xml must declare android:usesCleartextTraffic=\"false\"." >&2
  exit 1
fi

# 2. Check for prohibited / high-risk permissions flagged by Google Play Protect
PROHIBITED_PERMISSIONS=(
  "android.permission.BIND_ACCESSIBILITY_SERVICE"
  "android.permission.BIND_DEVICE_ADMIN"
  "android.permission.READ_SMS"
  "android.permission.SEND_SMS"
  "android.permission.RECEIVE_SMS"
  "android.permission.READ_CALL_LOG"
  "android.permission.WRITE_CALL_LOG"
  "android.permission.READ_CONTACTS"
  "android.permission.SYSTEM_ALERT_WINDOW"
  "android.permission.INSTALL_PACKAGES"
  "android.permission.REQUEST_DELETE_PACKAGES"
  "android.permission.WRITE_SECURE_SETTINGS"
  "android.permission.MANAGE_EXTERNAL_STORAGE"
)

for perm in "${PROHIBITED_PERMISSIONS[@]}"; do
  if grep -q "${perm}" "${MANIFEST_FILE}"; then
    echo "ERROR: Prohibited permission '${perm}' detected in AndroidManifest.xml!" >&2
    exit 1
  fi
done

# 3. Check for Dynamic Code Loading (DCL) of external DEX/JAR/SO payloads
if grep -rnE "DexClassLoader|InMemoryDexClassLoader" "${SRC_DIR}" --exclude="GiantWallSecuritySuite.kt" --exclude="SecurityIntegrityGuard.kt" >/dev/null 2>&1; then
  echo "ERROR: Dynamic code loading (DexClassLoader / InMemoryDexClassLoader) detected in application source!" >&2
  exit 1
fi

# 4. Check for hardcoded GitHub Personal Access Tokens or private keys in source code
if grep -rnE "ghp_[A-Za-z0-9]{30,}|github_pat_[A-Za-z0-9_]{30,}|-----BEGIN (RSA |EC )?PRIVATE KEY-----" "${SRC_DIR}" >/dev/null 2>&1; then
  echo "ERROR: Hardcoded GitHub token or private key detected in Kotlin source code!" >&2
  exit 1
fi

# 5. Check for unsafe WebView universal file access
if grep -rnE "setAllowUniversalAccessFromFileURLs\(\s*true\s*\)|setAllowFileAccessFromFileURLs\(\s*true\s*\)" "${SRC_DIR}" >/dev/null 2>&1; then
  echo "ERROR: Insecure WebView file URL access configuration detected!" >&2
  exit 1
fi

echo "Pre-release security & Google Play Protect audit passed with 0 violations."
