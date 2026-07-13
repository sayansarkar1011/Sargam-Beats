plugins {

    alias(libs.plugins.android.application)

    id("com.google.devtools.ksp")

}

android {
    namespace = "com.syn10.sargambeats"

    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.syn10.sargambeats"

        minSdk = 25
        targetSdk = 36

        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
    }


    buildTypes {

        release {

            isMinifyEnabled = false

            proguardFiles(
                getDefaultProguardFile(
                    "proguard-android-optimize.txt"
                ),
                "proguard-rules.pro"
            )

        }

    }


    compileOptions {

        sourceCompatibility =
            JavaVersion.VERSION_11

        targetCompatibility =
            JavaVersion.VERSION_11
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

    androidTestImplementation(
        libs.androidx.junit
    )

    androidTestImplementation(
        libs.androidx.espresso.core
    )


    // Glide

    implementation(
        "com.github.bumptech.glide:glide:4.16.0"
    )


    // Material

    implementation(
        "com.google.android.material:material:1.13.0"
    )


    // Exoplayer

    implementation(
        "com.google.android.exoplayer:exoplayer:2.19.1"
    )


    // Swipe Refresh

    implementation(
        "androidx.swiperefreshlayout:swiperefreshlayout:1.1.0"
    )


    // Media Session

    implementation(
        "androidx.media:media:1.7.0"
    )


    // Lottie

    implementation(
        "com.airbnb.android:lottie:6.6.7"
    )


    // Room Database

    implementation(
        "androidx.room:room-runtime:2.8.1"
    )

    implementation(
        "androidx.room:room-ktx:2.8.1"
    )

    ksp(
        "androidx.room:room-compiler:2.8.1"
    )
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.10.0")

}