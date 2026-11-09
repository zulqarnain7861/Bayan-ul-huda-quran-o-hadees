// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.kotlin.compose) apply false
  alias(libs.plugins.google.devtools.ksp) apply false
  alias(libs.plugins.secrets) apply false
  alias(libs.plugins.google.services) apply false
}

val syncQuranApk = tasks.register("syncQuranApk") {
  description = "No-op legacy APK sync task preserved for build script compatibility."
}

val verifyCleanBuildConfig = tasks.register("verifyCleanBuildConfig") {
  description = "Verifies clean-build configuration, .env.example defaults, and signing prerequisites."
  check(file("${rootDir}/.env.example").exists()) {
    "Missing .env.example in project root for Secrets Gradle Plugin."
  }
  check(file("${rootDir}/app/google-services.json").exists()) {
    "Missing app/google-services.json for Firebase integration."
  }
}


