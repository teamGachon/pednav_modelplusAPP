plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.modeltest"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.modeltest"
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

    buildFeatures {
        dataBinding = true
    }
}


dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation(libs.tensorflow.lite)

    // for vsm sdk
    implementation("com.google.flatbuffers:flatbuffers-java:1.11.0")

    // TMapAPI SDK
    implementation(files("libs/com.skt.Tmap_1.76.jar"))

    // TFLite
    implementation("org.tensorflow:tensorflow-lite:2.12.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.0")


    implementation("com.google.code.gson:gson:2.10")

}