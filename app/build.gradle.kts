plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.secrets)
}

if (file("google-services.json").exists()) {
  apply(plugin = "com.google.gms.google-services")
}

android {
  namespace = "com.example"
  compileSdk = 35

  defaultConfig {
    applicationId = "com.aistudio.moppitv.kmpzqt"
    // Universal Android compatibility: Android 5.0 (Lollipop API 21) through Android 15/16+
    minSdk = 21
    targetSdk = 35
    versionCode = 1
    versionName = "1.0.0"
    multiDexEnabled = true

    vectorDrawables {
      useSupportLibrary = true
    }

    ndk {
      // Support all 32-bit and 64-bit ARM and x86 Android devices
      abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
    }

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    create("release") {
      val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
      storeFile = file(keystorePath)
      storePassword = System.getenv("STORE_PASSWORD") ?: "moppitv123"
      keyAlias = System.getenv("KEY_ALIAS") ?: "upload"
      keyPassword = System.getenv("KEY_PASSWORD") ?: "moppitv123"
      enableV1Signing = true
      enableV2Signing = true
      enableV3Signing = true
    }
    create("debugConfig") {
      storeFile = file("${rootDir}/debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
      enableV1Signing = true
      enableV2Signing = true
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
    }
    debug {
      signingConfig = signingConfigs.getByName("debugConfig")
    }
  }

  splits {
    abi {
      isEnable = false
    }
  }

  compileOptions {
    isCoreLibraryDesugaringEnabled = true
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }

  kotlinOptions {
    jvmTarget = "11"
  }

  buildFeatures {
    compose = true
    buildConfig = true
  }

  lint {
    abortOnError = false
    checkReleaseBuilds = false
  }

  testOptions {
    unitTests {
      isIncludeAndroidResources = true
    }
  }

  dependenciesInfo {
    includeInApk = false
    includeInBundle = true
  }
}

secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
  ignoreList.add("FIREBASE_APPCHECK_DEBUG_TOKEN")
}

dependencies {
  coreLibraryDesugaring(libs.desugar.jdk.libs)
  implementation(libs.androidx.multidex)

  implementation(platform(libs.androidx.compose.bom))
  implementation(platform(libs.firebase.bom))
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.coil.compose)
  implementation(libs.converter.moshi)
  implementation(libs.firebase.firestore)
  implementation(libs.firebase.auth)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  implementation(libs.retrofit)

  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
}
