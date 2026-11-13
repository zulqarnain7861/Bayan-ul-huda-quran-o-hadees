#!/usr/bin/env bash
set -euo pipefail

# Syncs byte-for-byte shared assets from the primary Android repository into Bayan-Al-Huda-iOS.
# Usage: ./tools/sync_shared_assets_from_android.sh [path_to_android_repo_root]

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
IOS_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
ANDROID_ROOT="${1:-$(cd "${IOS_ROOT}/.." && pwd)}"

ANDROID_ASSETS="${ANDROID_ROOT}/app/src/main/assets"
IOS_RESOURCES="${IOS_ROOT}/BayanAlHuda/Resources"

if [ ! -d "${ANDROID_ASSETS}" ]; then
  echo "Error: Android assets directory not found at ${ANDROID_ASSETS}"
  exit 1
fi

mkdir -p "${IOS_RESOURCES}/Fonts"
mkdir -p "${IOS_RESOURCES}/Quran"

cp -f "${ANDROID_ASSETS}/noorani_qaida.pdf" "${IOS_RESOURCES}/noorani_qaida.pdf"
cp -f "${ANDROID_ASSETS}/noorani_qaida_text.json" "${IOS_RESOURCES}/noorani_qaida_text.json"
cp -f "${ANDROID_ASSETS}/fonts/Amiri-Regular.ttf" "${IOS_RESOURCES}/Fonts/Amiri-Regular.ttf"
cp -f "${ANDROID_ASSETS}/fonts/ScheherazadeNew-Regular.ttf" "${IOS_RESOURCES}/Fonts/ScheherazadeNew-Regular.ttf"

if [ -f "${ANDROID_ASSETS}/quran_arabic_6236.json" ]; then
  cp -f "${ANDROID_ASSETS}/quran_arabic_6236.json" "${IOS_RESOURCES}/Quran/quran_arabic_6236.json"
fi

echo "Successfully synchronized shared assets from ${ANDROID_ASSETS} to ${IOS_RESOURCES}:"
sha256sum "${IOS_RESOURCES}/noorani_qaida.pdf" \
          "${IOS_RESOURCES}/noorani_qaida_text.json" \
          "${IOS_RESOURCES}/Fonts/Amiri-Regular.ttf" \
          "${IOS_RESOURCES}/Fonts/ScheherazadeNew-Regular.ttf"
