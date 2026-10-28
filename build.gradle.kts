// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.kotlin.compose) apply false
  alias(libs.plugins.google.devtools.ksp) apply false
  alias(libs.plugins.secrets) apply false
  alias(libs.plugins.google.services) apply false
}

val syncQuranApk = tasks.register("syncQuranApk") {
  outputs.upToDateWhen { false }
  doLast {
    val root = java.io.File("/app/applet")
    val oldBuildOutputs = java.io.File(root, ".build-outputs")
    if (oldBuildOutputs.exists()) {
      oldBuildOutputs.deleteRecursively()
    }
    java.io.File(root, "quran.apk").delete()
    java.io.File(root, "quran3.apk").delete()
    java.io.File(root, "quran4.apk").delete()
    java.io.File(root, "quran5.apk").delete()
    java.io.File(root, "quran6.apk").delete()
    java.io.File(root, "Quran 6.apk").delete()
    java.io.File(root, "Quran 7.apk").delete()
    java.io.File(root, "Quran 8.apk").delete()
    java.io.File(root, "quran9.apk").delete()
    java.io.File(root, "apk/Quran 6.apk").delete()
    java.io.File(root, "apk/Quran 7.apk").delete()
    java.io.File(root, "apk/Quran 8.apk").delete()
    java.io.File(root, "quran.apk.idsig").delete()
    java.io.File(root, "quran6.apk.idsig").delete()
    java.io.File(root, "app/build/Quran_7_temp.apk").delete()
    java.io.File(root, "app/build/Quran_8_temp.apk").delete()
    java.io.File(root, "app/build/Quran_9_temp.apk").delete()

    java.io.File(root, "Quran 9.apk").delete()
    java.io.File(root, "bayan-ul-huda-1.0.2.apk").delete()
  }
}

subprojects {
  tasks.matching { it.name == "packageDebug" || it.name == "assembleDebug" }.configureEach {
    finalizedBy(syncQuranApk)
  }
}


