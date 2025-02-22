plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.rentease"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.rentease"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
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
}

dependencies {
    // Core Android dependencies
    implementation(libs.androidx.core.ktx.v1120)  // Ensure this is defined in libs.versions.toml
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.activity.ktx)   // For ActivityResult API

    // Firebase dependencies
    implementation(libs.firebase.auth.v2231)     // Firebase Authentication
    implementation(libs.firebase.database)       // Firebase Realtime Database
    implementation(libs.firebase.storage)        // Firebase Storage

    // All:
    implementation (libs.cloudinary.android.v302)

// Download + Preprocess:
    implementation (libs.cloudinary.android.download)
    implementation (libs.cloudinary.cloudinary.android.preprocess)

    // Cloudinary and image handling
    implementation(libs.cloudinary.android)      // Reference to Cloudinary in version catalog
    implementation(libs.glide)                   // For image loading

    // Networking (optional, for additional API calls)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)

    // SDP/SSP for scalable sizes
    implementation("com.intuit.ssp:ssp-android:1.0.6")
    implementation("com.intuit.sdp:sdp-android:1.0.6")

    // Play Services (only if needed for casting)
    implementation(libs.play.services.cast.tv)
    implementation(libs.androidx.runner)

    // Testing dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}