plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.huerteando.app"
    compileSdk = 35


    defaultConfig {
        applicationId = "com.huerteando.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.coordinatorlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    // Retrofit: HTTP calls to the REST API
    implementation(libs.retrofit)
    implementation(libs.converter.gson) // JSON -> Java objects
    // Glide: load images without blocking UI
    implementation(libs.glide)
    // OkHttp logging interceptor for debugging HTTP
    debugImplementation(libs.logging.interceptor)
    // OSMDroid for maps (if needed)
    implementation(libs.osmdroid.android)
    // EncryptedSharedPreferences for secure token storage
    implementation(libs.security.crypto)
    // Google Play Services for location
    implementation(libs.play.services.location)
}
