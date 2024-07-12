
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    compileSdk = 34

    defaultConfig {
        applicationId = "com.daon.fido.sdk.sample"
        minSdk = 21
        targetSdk = 34
        versionCode = 8
        versionName = "4.7"
        multiDexEnabled = true
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles (
                getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro"
            )
            ndk {
                abiFilters += listOf("arm64-v8a", "armeabi-v7a")
            }
        }
        debug {
            ndk {
                abiFilters += listOf("arm64-v8a", "armeabi-v7a")
            }
        }
    }

    aaptOptions {
        noCompress("bgm", "bin")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    namespace = "com.daon.fido.sdk.sample"
}

dependencies {

    implementation(fileTree(mapOf("include" to listOf("*.jar"), "dir" to "libs")))

    implementation("androidx.biometric:biometric:1.1.0")

    implementation("androidx.multidex:multidex:2.0.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.legacy:legacy-support-v13:1.0.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    implementation("com.google.code.gson:gson:2.10.1")
    implementation("commons-codec:commons-codec:1.15")

    implementation("com.google.android.play:integrity:1.3.0")
    implementation("com.google.android.gms:play-services-vision:20.1.3")
    implementation("com.google.firebase:firebase-messaging:23.4.1")
    implementation("com.nimbusds:srp6a:2.0.2")
    implementation ("com.daon.sdk.fido.client:com.daon.sdk.fido.client:4+") {
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
    // Remove these dependencies if daon face support is not required
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
    //implementation 'com.daon.sdk.face.liveness:com.daon.sdk.face.liveness:5+@aar'

    implementation("com.daon.sdk.face:com.daon.sdk.face.liveness.dfl:1.0.0") {
        artifact {
            type = "aar"
        }
    }
    implementation("com.daon.sdk.face:com.daon.sdk.face.quality:3.2.103") {
        artifact {
            type = "aar"
        }
    }

    implementation("com.daon.sdk.face:com.daon.sdk.face.detector.mask:1.0.10") {
        artifact {
            type = "aar"
        }
    }

    // Used by com.daon.sdk.face.liveness
    implementation("org.slf4j:slf4j-android:1.7.12")

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

    // Remove these dependencies if voice support is not required
    implementation("com.daon.sdk.authenticator:com.daon.sdk.voiceauthenticator:4+") {
        artifact {
            type = "aar"
        }
    }

    implementation("com.daon.sdk.voice:daon-voice-release") {
        artifact {
            type = "aar"
        }
    }

    implementation("com.daon.oggencoder:ogg_encoder:1.0") {
        artifact {
            type = "aar"
        }
    }

    // Preferences DataStore
    implementation("androidx.preference:preference-ktx:1.2.1") {
        exclude(group = "androidx.lifecycle", module = "lifecycle-viewmodel-ktx")
    }

    // CameraX
    val camerax_version = "1.3.3"
    implementation("androidx.camera:camera-core:${camerax_version}")
    implementation("androidx.camera:camera-camera2:${camerax_version}")
    implementation("androidx.camera:camera-lifecycle:${camerax_version}")
    implementation("androidx.camera:camera-video:${camerax_version}")
    implementation("androidx.camera:camera-view:${camerax_version}")

    implementation("androidx.lifecycle:lifecycle-process:2.7.0")
}
