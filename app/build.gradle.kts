import java.util.Properties
import java.io.FileInputStream

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
keystoreProperties.load(FileInputStream(keystorePropertiesFile))

plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
}

android {
  signingConfigs {
    create("sign") {
      keyAlias = keystoreProperties["keyAlias"] as String
      keyPassword = keystoreProperties["keyPassword"] as String
      storeFile = file(keystoreProperties["storeFile"] as String)
      storePassword = keystoreProperties["storePassword"] as String
    }
  }
  namespace = "me.denexapp.usbvideocapture"
  compileSdk = 35

  defaultConfig {
    applicationId = "me.denexapp.usbvideocapture"
    minSdk = 30
    targetSdk = 35
    versionCode = 3
    versionName = "1.0.2"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    externalNativeBuild {
      cmake {
        cppFlags += "-stdlib=libc++ -std=c++20 -fexperimental-library -fvisibility=hidden"
      }
    }
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("sign")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }
  kotlinOptions {
    jvmTarget = "1.8"
  }
  externalNativeBuild {
    cmake {
      path = file("src/main/cpp/CMakeLists.txt")
      version = "3.22.1"
    }
  }
  buildFeatures {
    viewBinding = true
  }

  packaging {
    resources {
      pickFirsts.add("META-INF/LICENSE.md")
      pickFirsts.add("META-INF/LICENSE-notice.md")
    }
  }

  ndkVersion = "26.3.11579264"
}

dependencies {
  implementation("androidx.core:core-ktx:1.13.1")
  implementation("androidx.activity:activity:1.9.0")
  implementation("androidx.activity:activity-ktx:1.9.0")
  implementation("com.google.android.material:material:1.12.0")
  implementation("androidx.constraintlayout:constraintlayout:2.1.4")
  implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.3")
  implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
  implementation("androidx.startup:startup-runtime:1.1.1")
  testImplementation("junit:junit:4.13.2")
  testImplementation(kotlin("test"))
  testImplementation("org.junit.jupiter:junit-jupiter:5.10.3")
  testImplementation("io.mockk:mockk-android:1.13.12")
  testImplementation("org.junit.jupiter:junit-jupiter:5.10.3")
  androidTestImplementation("androidx.test.ext:junit:1.2.1")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
  androidTestImplementation("io.mockk:mockk-android:1.13.12")
}
