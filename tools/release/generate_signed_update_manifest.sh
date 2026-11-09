#!/usr/bin/env bash
set -euo pipefail

# ==============================================================================
# Bayan al-Huda — Automated Signed Update Manifest Generator & Verifier
# ==============================================================================
# Generates:
#   1. update.json       (Cryptographically signed update metadata for In-App Updater)
#   2. update.json.sig   (Detached Base64 signature over canonical manifest payload)
# Uses Java's native KeyStore (JKS/PKCS12) + SHA256withRSA Signature API to
# guarantee 100% compatibility with Android's UpdateManifestVerifier.
# ==============================================================================

APK_FILE=""
KEYSTORE_FILE=""
STORE_PASS=""
KEY_ALIAS="upload"
KEY_PASS=""
PACKAGE_NAME="com.bayanulhuda.com"
VERSION_NAME=""
VERSION_CODE=""
MIN_VERSION_CODE="8"
DOWNLOAD_URL=""
RELEASE_NOTES=""
FORCE_UPDATE="false"
CERT_SHA256=""
OUTPUT_DIR="."

while [[ $# -gt 0 ]]; do
  case "$1" in
    --apk) APK_FILE="$2"; shift 2 ;;
    --keystore) KEYSTORE_FILE="$2"; shift 2 ;;
    --store-pass) STORE_PASS="$2"; shift 2 ;;
    --key-alias) KEY_ALIAS="$2"; shift 2 ;;
    --key-pass) KEY_PASS="$2"; shift 2 ;;
    --package-name) PACKAGE_NAME="$2"; shift 2 ;;
    --version-name) VERSION_NAME="$2"; shift 2 ;;
    --version-code) VERSION_CODE="$2"; shift 2 ;;
    --min-version-code) MIN_VERSION_CODE="$2"; shift 2 ;;
    --download-url) DOWNLOAD_URL="$2"; shift 2 ;;
    --release-notes) RELEASE_NOTES="$2"; shift 2 ;;
    --force-update) FORCE_UPDATE="$2"; shift 2 ;;
    --cert-sha256) CERT_SHA256="$2"; shift 2 ;;
    --output-dir) OUTPUT_DIR="$2"; shift 2 ;;
    *)
      echo "ERROR: Unknown argument: $1" >&2
      exit 1
      ;;
  esac
done

if [[ -z "${APK_FILE}" || ! -f "${APK_FILE}" ]]; then
  echo "ERROR: Valid --apk file is required." >&2
  exit 1
fi

if [[ -z "${KEYSTORE_FILE}" || ! -f "${KEYSTORE_FILE}" ]]; then
  echo "ERROR: Valid --keystore file is required." >&2
  exit 1
fi

if [[ -z "${STORE_PASS}" || -z "${KEY_PASS}" ]]; then
  echo "ERROR: Keystore and key passwords must be provided." >&2
  exit 1
fi

if [[ -z "${VERSION_NAME}" || -z "${VERSION_CODE}" || -z "${DOWNLOAD_URL}" ]]; then
  echo "ERROR: --version-name, --version-code, and --download-url are required." >&2
  exit 1
fi

mkdir -p "${OUTPUT_DIR}"

APK_SHA256="$(sha256sum "${APK_FILE}" | awk '{print $1}' | tr '[:upper:]' '[:lower:]')"
PUBLISHED_AT="$(date -u +"%Y-%m-%dT%H:%M:%SZ")"

if [[ "${FORCE_UPDATE}" == "true" || "${FORCE_UPDATE}" == "1" ]]; then
  FORCE_BOOL="true"
else
  FORCE_BOOL="false"
fi

TMP_WORK="$(mktemp -d)"
trap 'rm -rf "${TMP_WORK}"' EXIT

cat > "${TMP_WORK}/ManifestSigner.java" << 'JAVAEOF'
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Key;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.util.Base64;
import java.util.Locale;

public class ManifestSigner {
    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format(Locale.ROOT, "%02x", b & 0xff));
        }
        return sb.toString();
    }

    public static void main(String[] args) throws Exception {
        String keystorePath = args[0];
        String storePass = args[1];
        String keyAlias = args[2];
        String keyPass = args[3];
        String packageName = args[4];
        String versionName = args[5];
        String versionCode = args[6];
        String minVersionCode = args[7];
        String downloadUrl = args[8];
        String apkSha256 = args[9].toLowerCase(Locale.ROOT);
        String forceBool = args[10].toLowerCase(Locale.ROOT);
        String explicitCertSha256 = args[11].toLowerCase(Locale.ROOT);
        String outDir = args[12];

        KeyStore ks = KeyStore.getInstance(new File(keystorePath), storePass.toCharArray());
        Key key = ks.getKey(keyAlias, keyPass.toCharArray());
        if (!(key instanceof PrivateKey)) {
            throw new IllegalStateException("Keystore entry '" + keyAlias + "' is not a PrivateKey");
        }
        PrivateKey privateKey = (PrivateKey) key;
        Certificate cert = ks.getCertificate(keyAlias);
        if (cert == null) {
            throw new IllegalStateException("No certificate found for alias '" + keyAlias + "'");
        }
        PublicKey publicKey = cert.getPublicKey();

        byte[] certDer = cert.getEncoded();
        String computedCertSha256 = toHex(MessageDigest.getInstance("SHA-256").digest(certDer));
        String effectiveCertSha256 = explicitCertSha256.isEmpty() ? computedCertSha256 : explicitCertSha256;
        String pubKeyDerB64 = Base64.getEncoder().encodeToString(publicKey.getEncoded());

        String canonicalPayload = "bayan-al-huda-update-v1"
            + "|package=" + packageName.trim()
            + "|version=" + versionName.trim()
            + "|version_code=" + versionCode.trim()
            + "|minimum_version_code=" + minVersionCode.trim()
            + "|download_url=" + downloadUrl.trim()
            + "|sha256=" + apkSha256.trim()
            + "|force_update=" + forceBool.trim()
            + "|signing_cert_sha256=" + effectiveCertSha256.trim();

        byte[] payloadBytes = canonicalPayload.getBytes(StandardCharsets.UTF_8);

        Signature signer = Signature.getInstance("SHA256withRSA");
        signer.initSign(privateKey);
        signer.update(payloadBytes);
        byte[] signatureBytes = signer.sign();

        // Immediately verify signature before writing output
        Signature verifier = Signature.getInstance("SHA256withRSA");
        verifier.initVerify(publicKey);
        verifier.update(payloadBytes);
        if (!verifier.verify(signatureBytes)) {
            throw new SecurityException("Self-verification of update.json RSA-SHA256 signature failed!");
        }

        String sigB64 = Base64.getEncoder().encodeToString(signatureBytes);
        Files.writeString(Paths.get(outDir, "cert_sha256.txt"), effectiveCertSha256, StandardCharsets.UTF_8);
        Files.writeString(Paths.get(outDir, "pubkey_b64.txt"), pubKeyDerB64, StandardCharsets.UTF_8);
        Files.writeString(Paths.get(outDir, "sig_b64.txt"), sigB64, StandardCharsets.UTF_8);
    }
}
JAVAEOF

java "${TMP_WORK}/ManifestSigner.java" \
  "${KEYSTORE_FILE}" \
  "${STORE_PASS}" \
  "${KEY_ALIAS}" \
  "${KEY_PASS}" \
  "${PACKAGE_NAME}" \
  "${VERSION_NAME}" \
  "${VERSION_CODE}" \
  "${MIN_VERSION_CODE}" \
  "${DOWNLOAD_URL}" \
  "${APK_SHA256}" \
  "${FORCE_BOOL}" \
  "${CERT_SHA256}" \
  "${TMP_WORK}"

EFFECTIVE_CERT_SHA256="$(cat "${TMP_WORK}/cert_sha256.txt")"
PUBKEY_DER_B64="$(cat "${TMP_WORK}/pubkey_b64.txt")"
SIGNATURE_B64="$(cat "${TMP_WORK}/sig_b64.txt")"

printf "%s\n" "${SIGNATURE_B64}" > "${OUTPUT_DIR}/update.json.sig"

python3 - "${OUTPUT_DIR}/update.json" \
  "${VERSION_NAME}" \
  "${VERSION_CODE}" \
  "${MIN_VERSION_CODE}" \
  "${DOWNLOAD_URL}" \
  "${APK_SHA256}" \
  "${RELEASE_NOTES}" \
  "${FORCE_BOOL}" \
  "${PACKAGE_NAME}" \
  "${EFFECTIVE_CERT_SHA256}" \
  "${PUBLISHED_AT}" \
  "${SIGNATURE_B64}" \
  "${PUBKEY_DER_B64}" << 'PYEOF'
import json
import sys

out_path = sys.argv[1]
manifest = {
    "version": sys.argv[2],
    "version_code": int(sys.argv[3]),
    "minimum_version_code": int(sys.argv[4]),
    "download_url": sys.argv[5],
    "sha256": sys.argv[6],
    "release_notes": sys.argv[7],
    "force_update": sys.argv[8].lower() == "true",
    "package_name": sys.argv[9],
    "signing_cert_sha256": sys.argv[10],
    "published_at": sys.argv[11],
    "signature_base64": sys.argv[12],
    "public_key_der_base64": sys.argv[13],
    "signature_algorithm": "SHA256withRSA"
}

with open(out_path, "w", encoding="utf-8") as f:
    json.dump(manifest, f, indent=2, ensure_ascii=False)
PYEOF

echo "Successfully generated and verified signed update.json at ${OUTPUT_DIR}/update.json"
