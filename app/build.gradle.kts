plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.keepcoding.dragonball"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.keepcoding.dragonball"
        minSdk = 24
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
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Esta librería es para el ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    // Esta librería es para Glide
    implementation(libs.glide)

    // Esta librería es para material design
    implementation(libs.material)

    // Esta librería es para el activityFragment
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)

    // Esta librería es para el http
    implementation(libs.okhttp)

    // Esta librería es para los JSON
    implementation(libs.gson)

    // Esta librería es para test
    testImplementation(libs.kotlinx.coroutines.test)
}