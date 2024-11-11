plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {


    namespace = "com.example.fasalmitra"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.fasalmitra"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    // Firebase ML Model Interpreter
    implementation ("com.google.firebase:firebase-ml-model-interpreter:22.0.4")
    // Firebase Realtime Database dependency
    implementation ("com.google.firebase:firebase-database:20.1.0")
    implementation ("com.google.gms:google-services:4.4.2")  // Ensure it's the latest version
    implementation ("com.google.firebase:firebase-database:20.0.3")  // Correct version
    implementation ("com.google.firebase:firebase-auth:21.0.1")     // Authentication if needed
    implementation ("com.google.firebase:firebase-core:21.0.0")
    implementation ("com.google.mlkit:image-labeling:17.0.9")
    implementation("com.google.mlkit:image-labeling-custom:17.0.3")
    implementation ("androidx.cardview:cardview:1.0.0")
    implementation ("androidx.appcompat:appcompat:1.2.0")
    implementation ("androidx.constraintlayout:constraintlayout:2.0.4")
    implementation ("com.github.bumptech.glide:glide:4.15.1")
    implementation ("org.tensorflow:tensorflow-lite:2.10.0")
    implementation ("androidx.room:room-runtime:2.5.0")
    implementation(libs.firebase.database)
    implementation(libs.firebase.storage)
    annotationProcessor("androidx.room:room-compiler:2.5.0")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.android.volley:volley:1.2.1")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}