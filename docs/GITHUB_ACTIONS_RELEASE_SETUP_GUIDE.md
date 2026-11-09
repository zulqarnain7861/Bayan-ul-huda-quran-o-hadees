# First-Time Setup & Beginner-Friendly Release Guide for BAYAN AL-HUDA

Repository: `https://github.com/zulqarnain7861/Bayan-ul-huda-quran-o-hadees`

This guide explains how to complete the **one-time GitHub Secrets setup** so that every future update of **Bayan al-Huda** is built, signed, verified, and published to GitHub Releases **100% automatically** whenever you push a version tag.

---

## Part 1: One-Time Signing Keystore & GitHub Secrets Setup

Android requires every update of **Bayan al-Huda** (`com.bayanulhuda.com`) to be signed with the **exact same signing key**. If a new APK is signed with a different key, Android will refuse to install the update over the existing app.

### Step 1: Create Your Release Keystore (Only Once!)
If you do not already have a `my-upload-key.jks` file on your computer, run this command once in your terminal:

```bash
keytool -genkeypair -v \
  -keystore my-upload-key.jks \
  -alias upload \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

> **IMPORTANT:**
> - Keep the alias as `upload` (this matches `keyAlias = "upload"` in `app/build.gradle.kts`).
> - Save `my-upload-key.jks` and your passwords in a safe backup location (never commit `my-upload-key.jks` to GitHub).

### Step 2: Convert `my-upload-key.jks` to Base64 Text
Run the following command on your computer to convert the keystore file into a single-line Base64 string:

- **Linux / Ubuntu / Git Bash:**
  ```bash
  base64 -w 0 my-upload-key.jks > keystore_base64.txt
  ```
- **macOS:**
  ```bash
  base64 -i my-upload-key.jks | tr -d '\n' > keystore_base64.txt
  ```
- **Windows PowerShell:**
  ```powershell
  [Convert]::ToBase64String([IO.File]::ReadAllBytes("my-upload-key.jks")) | Set-Content -NoNewline keystore_base64.txt
  ```

Open `keystore_base64.txt`, copy the entire text, and then delete `keystore_base64.txt` when done.

### Step 3: Add the 4 Signing Secrets in GitHub
1. Open your repository on GitHub:
   `https://github.com/zulqarnain7861/Bayan-ul-huda-quran-o-hadees`
2. Click **Settings** (top navigation bar of the repository).
3. In the left sidebar, click **Secrets and variables** → **Actions**.
4. Click **New repository secret** and add these **4 secrets**:

| Secret Name | Value to Enter |
| :--- | :--- |
| `ANDROID_KEYSTORE_BASE64` | Paste the Base64 text of `my-upload-key.jks` from Step 2 |
| `ANDROID_KEYSTORE_PASSWORD` | Your keystore password |
| `ANDROID_KEY_ALIAS` | `upload` |
| `ANDROID_KEY_PASSWORD` | Your key password |

### Step 4: Enable GitHub Actions Workflow Write Permissions
1. In your GitHub repository, go to **Settings** → **Actions** → **General**.
2. Scroll down to **Workflow permissions**.
3. Select **Read and write permissions** and click **Save**.
   *(This allows `.github/workflows/android-release.yml` to automatically create GitHub Releases and upload the signed APK and `update.json` manifest.)*

---

## Part 2: How to Publish a New Update (Your Regular 3-Step Routine)

Once Part 1 is configured, **you never need to manually build APKs, upload files, calculate SHA-256 checksums, or write `update.json`**.

Whenever you are ready to release a new version:

### 1. Increment `versionCode` and `versionName` in `app/build.gradle.kts`
Open `app/build.gradle.kts` and increment `versionCode` (e.g., from `8` to `9`) and update `versionName` (e.g., `"1.1.0"`):

```kotlin
defaultConfig {
  applicationId = "com.bayanulhuda.com"
  minSdk = 24
  targetSdk = 36
  versionCode = 9
  versionName = "1.1.0"
```

### 2. Commit and Push Your Code
```bash
git add .
git commit -m "Release v1.1.0: bug fixes and improvements"
git push origin main
```

### 3. Create and Push the Version Tag
```bash
git tag v1.1.0
git push origin v1.1.0
```

### What Happens Automatically Next:
1. GitHub Actions starts `.github/workflows/android-release.yml`.
2. Runs unit tests and clean-build checks.
3. Builds and signs the release APK using your `ANDROID_KEYSTORE_BASE64` secret.
4. Verifies the package name (`com.bayanulhuda.com`), increasing `versionCode`, and X.509 signing certificate.
5. Computes the SHA-256 checksum and cryptographically signs `update.json`.
6. Publishes the GitHub Release with `Bayan-al-Huda-v1.1.0.apk`, `Bayan-al-Huda.apk`, `.sha256`, and `update.json`.
7. Installed copies of **Bayan al-Huda** automatically detect the new release, notify the user, verify the signed `update.json` + APK SHA-256 + X.509 certificate, and prompt the user to install via Android's official Package Installer!
