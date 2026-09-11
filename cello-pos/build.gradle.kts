plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.cello_pos"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.cello_pos"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
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
    implementation(libs.androidx.activity.ktx)
    implementation(libs.appcompat.v7)
    implementation(libs.constraint.layout)
    implementation(libs.design)
    implementation(libs.livedata)
    implementation(libs.support.annotations)
    implementation(libs.viewmodel)
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.runner)
}