plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.tatanstudios.eltuncazometapan"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.tatanstudios.eltuncazometapan"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 13
        versionName = "3.0.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // Incluye en el .aab los símbolos del código nativo (.so de las librerías),
            // así Play Console muestra fallos nativos legibles y no avisa de "símbolos de depuración".
            ndk {
                debugSymbolLevel = "SYMBOL_TABLE"
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(projects.shared)
    implementation(libs.onesignal)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.ui)
    debugImplementation(libs.compose.ui.tooling)
}
