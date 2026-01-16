plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")}

android {
    namespace = "com.wazzgroup.penagihanwifi"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.wazzgroup.penagihanwifi"
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
    buildFeatures {
        viewBinding = true
    }

}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.okhttp)
    implementation(libs.osmdroid.android)
    implementation(libs.play.services.location)
    implementation(libs.gson)
    implementation(libs.exoplayer)
    implementation(libs.glide)
    implementation(libs.lottie)
    implementation(libs.duitku.android.sdk)
    implementation(libs.zxing.android.embedded)
    implementation(libs.netty.all)
    implementation(libs.google.services)
    implementation(libs.firebase.messaging)
    implementation(libs.com.google.gms.google.services.gradle.plugin)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.github.DantSu:ESCPOS-ThermalPrinter-Android:3.3.0")
    implementation(platform("com.google.firebase:firebase-bom:34.5.0"))
    implementation("com.google.firebase:firebase-analytics")

}
