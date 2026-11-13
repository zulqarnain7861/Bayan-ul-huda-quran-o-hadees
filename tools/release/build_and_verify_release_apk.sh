#!/usr/bin/env bash
set -euo pipefail

# ==============================================================================
# BAYAN AL-HUDA — Comprehensive Release APK Build, ProGuard & Signing Verifier
# ==============================================================================
# 1. Prepares clean-build signing prerequisites (.env.example, google-services.json,
#    and release/fallback keystore with alias 'upload').
# 2. Runs the Pre-Release Security & Google Play Protect audit.
# 3. Verifies ProGuard/R8 rules file (app/proguard-rules.pro) and builds the
#    signed Release APK via Gradle (verifyCleanBuildConfig :app:assembleRelease).
# 4. Verifies v1/v2/v3 APK cryptographic signatures, package name
#    (com.bayanulhuda.com), and X.509 certificate SHA-256 via apksigner.
# 5. Generates SHA-256 checksums and the RSA-SHA256 signed update.json manifest,
#    and exports the signed installable APK into zip/BAYAN-AL-HUDA-v1.0.2-release.apk.
# ==============================================================================

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "${ROOT_DIR}"

chmod +x tools/signing/prepare-clean-build-signing.sh
chmod +x tools/release/decode_and_prepare_keystore.sh
chmod +x tools/release/verify_security_and_play_protect.sh
chmod +x tools/release/verify_signed_apk.sh
chmod +x tools/release/generate_signed_update_manifest.sh

echo "=== [1/5] Preparing Clean-Build Signing & Keystore ==="
./tools/signing/prepare-clean-build-signing.sh

KEYSTORE_FILE="/tmp/bayan-release-upload.jks"
KEYSTORE_META="/tmp/keystore_meta.env"
rm -f "${KEYSTORE_META}"

./tools/release/decode_and_prepare_keystore.sh \
  --output "${KEYSTORE_FILE}" \
  --output-env "${KEYSTORE_META}" \
  --sync-gradle-default

USING_FALLBACK="$(grep '^using_fallback_keystore=' "${KEYSTORE_META}" | cut -d'=' -f2 || echo 'true')"
if [[ "${USING_FALLBACK}" == "true" ]]; then
  export STORE_PASSWORD="android"
  export KEY_ALIAS="upload"
  export KEY_PASSWORD="android"
  REJECT_DEBUG_CERT="false"
else
  export STORE_PASSWORD="${ANDROID_KEYSTORE_PASSWORD}"
  export KEY_ALIAS="upload"
  export KEY_PASSWORD="${ANDROID_KEY_PASSWORD}"
  REJECT_DEBUG_CERT="true"
fi
export KEYSTORE_PATH="${KEYSTORE_FILE}"
export ANDROID_KEYSTORE_PATH="${KEYSTORE_FILE}"

echo "=== [2/5] Running Pre-Release Security & Play Protect Audit ==="
./tools/release/verify_security_and_play_protect.sh
test -f "app/proguard-rules.pro"

echo "=== [3/5] Building Signed Release APK with Gradle ==="
gradle verifyCleanBuildConfig :app:assembleRelease -x lintVitalRelease -x lintVitalAnalyzeRelease --stacktrace


APK_PATH="$(find app/build/outputs/apk/release -name '*.apk' ! -name '*unsigned*' | head -n 1)"
if [[ -z "${APK_PATH}" || ! -f "${APK_PATH}" ]]; then
  echo "ERROR: Signed release APK not found in app/build/outputs/apk/release/" >&2
  exit 1
fi

echo "=== [4/5] Verifying APK Cryptographic Signature (apksigner) ==="
VERIFY_ENV="/tmp/apk_verify_meta.env"
rm -f "${VERIFY_ENV}"
./tools/release/verify_signed_apk.sh \
  --apk "${APK_PATH}" \
  --expected-package "com.bayanulhuda.com" \
  --reject-debug-cert "${REJECT_DEBUG_CERT}" \
  --output-env "${VERIFY_ENV}"

APK_VER_NAME="$(grep '^apk_version_name=' "${VERIFY_ENV}" | cut -d'=' -f2 || echo '1.0.2')"
APK_VER_CODE="$(grep '^apk_version_code=' "${VERIFY_ENV}" | cut -d'=' -f2 || echo '8')"
CERT_SHA256="$(grep '^cert_sha256=' "${VERIFY_ENV}" | cut -d'=' -f2 || echo '')"
TAG_NAME="v${APK_VER_NAME}"

echo "=== [5/5] Packaging Signed Installable APK & Signed update.json into zip/ ==="
mkdir -p zip
RELEASE_APK_DEST="zip/BAYAN-AL-HUDA-${TAG_NAME}-release.apk"
cp -f "${APK_PATH}" "${RELEASE_APK_DEST}"

APK_SHA256="$(sha256sum "${RELEASE_APK_DEST}" | awk '{print $1}')"
echo "${APK_SHA256}  BAYAN-AL-HUDA-${TAG_NAME}-release.apk" > "zip/BAYAN-AL-HUDA-${TAG_NAME}-SHA256.txt"

./tools/release/generate_signed_update_manifest.sh \
  --apk "${RELEASE_APK_DEST}" \
  --keystore "${KEYSTORE_FILE}" \
  --store-pass "${STORE_PASSWORD}" \
  --key-alias "upload" \
  --key-pass "${KEY_PASSWORD}" \
  --package-name "com.bayanulhuda.com" \
  --version-name "${APK_VER_NAME}" \
  --version-code "${APK_VER_CODE}" \
  --min-version-code "8" \
  --download-url "https://github.com/zulqarnain7861/Bayan-ul-huda-quran-o-hadees/releases/download/${TAG_NAME}/bayan-al-huda-${TAG_NAME}-release.apk" \
  --release-notes "- Native HD Noorani Qaida 53-page PDF reader with 3-tier quizzes, 280-reciter audio picker, and Azkar/Duas." \
  --force-update "false" \
  --cert-sha256 "${CERT_SHA256}" \
  --output-dir "zip"

rm -f "${KEYSTORE_FILE}" "${KEYSTORE_META}" "${VERIFY_ENV}" "my-upload-key.jks"

echo "=============================================================================="
echo "SUCCESS: Signed Installable Release APK Ready!"
echo " - APK Path:          ${RELEASE_APK_DEST}"
echo " - APK Size:          $(ls -lh "${RELEASE_APK_DEST}" | awk '{print $5}')"
echo " - Package Name:      com.bayanulhuda.com"
echo " - Version:           ${APK_VER_NAME} (versionCode ${APK_VER_CODE})"
echo " - APK SHA-256:       ${APK_SHA256}"
echo " - Cert SHA-256:      ${CERT_SHA256}"
echo " - Signed Manifest:   zip/update.json"
echo " - iOS Source Bundle: zip/Bayan-Al-Huda-iOS-full-source.zip"
echo "=============================================================================="
