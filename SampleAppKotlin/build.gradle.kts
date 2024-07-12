plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.daon.fido.sdk.sample.kt"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.daon.fido.sdk.sample.kt"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "4.7"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles (
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        viewBinding = true
        dataBinding = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}


dependencies {
    implementation(fileTree(mapOf("include" to listOf("*.jar"), "dir" to "libs")))

    implementation ("com.daon.sdk.fido.client:com.daon.sdk.fido.client-ktx:4+") {
        artifact {
            type = "aar"
        }
    }
    implementation("com.daon.sdk.fido.service:com.daon.sdk.fido.service:4+") {
        artifact {
            type = "aar"
        }
    }
    implementation("com.daon.sdk.authenticator:com.daon.sdk.authcommon:4+") {
        artifact {
            type = "aar"
        }
    }
    implementation("com.daon.sdk.authenticator:com.daon.sdk.authenticator:4+") {
        artifact {
            type = "aar"
        }
    }
    implementation("com.nimbusds:srp6a:2.0.2")
    implementation("com.daon.sdk.device:com.daon.sdk.device:4+") {
        artifact {
            type = "aar"
        }
    }
    implementation("com.daon.sdk.crypto:com.daon.sdk.crypto:4+") {
        artifact {
            type = "aar"
        }
    }

    // Daon Face
    implementation("com.daon.sdk.authenticator:com.daon.sdk.faceauthenticator-daon-0205:4+") {
        artifact {
            type = "aar"
        }
    }
    implementation("com.daon.sdk.face:com.daon.sdk.face:5+") {
        artifact {
            type = "aar"
        }
    }
    implementation("com.daon.sdk.face:com.daon.sdk.face.quality:3.2.103") {
        artifact {
            type = "aar"
        }
    }

    //Daon Passive
    //implementation 'com.daon.sdk.face.liveness:com.daon.sdk.face.liveness:5+@aar'
    implementation("com.daon.sdk.face:com.daon.sdk.face.liveness.dfl:1.0.0") {
        artifact {
            type = "aar"
        }
    }
    implementation("com.daon.sdk.face.matcher:com.daon.sdk.face.matcher:1.3.1") {
        artifact {
            type = "aar"
        }
    }
    implementation("com.daon.sdk.face:com.daon.sdk.face.detector:1.2.2") {
        artifact {
            type = "aar"
        }
    }

    // Used by com.daon.sdk.face.liveness
    implementation("org.slf4j:slf4j-android:1.7.12")
    // Medical/surgical face mask detection
    implementation("com.daon.sdk.face:com.daon.sdk.face.detector.mask:1.0.10") {
        artifact {
            type = "aar"
        }
    }

    implementation("androidx.core:core-ktx:1.13.0")
    
    // CameraX core library
    val camerax_version = "1.3.3"
    implementation("androidx.camera:camera-core:${camerax_version}")
    implementation("androidx.camera:camera-camera2:${camerax_version}")
    implementation("androidx.camera:camera-lifecycle:${camerax_version}")
    implementation("androidx.camera:camera-video:${camerax_version}")
    implementation("androidx.camera:camera-view:${camerax_version}")

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    //Compose Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.9.22")

    implementation("com.google.code.gson:gson:2.10.1")
    implementation("androidx.biometric:biometric:1.1.0")

    implementation("androidx.fragment:fragment-ktx:1.6.2")

    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.preference:preference-ktx:1.2.1")

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")

    // Compose
    val composeBom = platform("androidx.compose:compose-bom:2022.12.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)


    implementation("androidx.compose.material:material")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.constraintlayout:constraintlayout-compose:1.0.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")


    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    implementation("androidx.compose.ui:ui-viewbinding")


    //Hilt
    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-android-compiler:2.51.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    //Accompanist-Permissions
    implementation("com.google.accompanist:accompanist-permissions:0.21.1-beta")

    //Jackson JSON converter
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.7")

    implementation("com.google.android.play:integrity:1.3.0")
}

kapt {
    correctErrorTypes = true
}