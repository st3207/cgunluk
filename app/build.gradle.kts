plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.example.cgunluk"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.cgunluk"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildFeatures {
            viewBinding = true
        }
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
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.activity)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    // Room Persistence Library
    val room_version = "2.6.1" // Güncel stabil sürümü kontrol edebilirsiniz
    implementation("androidx.room:room-runtime:$room_version")
    // annotationProcessor("androidx.room:room-compiler:$room_version")
    // Kotlin Annotation Processing Tool (kapt) yerine KSP (Kotlin Symbol Processing) kullanmak daha modern ve performanslıdır.
    // Eğer projenizde KSP yoksa, önce onu eklemeniz gerekir.
    // build.gradle.kts (Project: cgunluk) dosyasına KSP plugin'i ekleyin:
    // plugins { id("com.google.devtools.ksp") version "..." apply false }
    // Sonra build.gradle.kts (Module :app) dosyasının başına plugin'i uygulayın:
    // plugins { id("com.google.devtools.ksp") }
    // ve Room için ksp kullanın:
    ksp("androidx.room:room-compiler:$room_version") // annotationProcessor yerine bunu kullanın

    // Room Kotlin Extensions (Coroutines desteği için)
    implementation("androidx.room:room-ktx:$room_version")

    // Lifecycle Components (ViewModel ve LiveData için - ileride kullanacağız)
    val lifecycle_version = "2.8.3" // Güncel stabil sürüm
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycle_version")

    // Material Components (Dialog gibi UI elemanları için)
    implementation("com.google.android.material:material:1.12.0") // Güncel stabil sürüm

    // AndroidX Core KTX (Kotlin eklentileri)
    implementation("androidx.core:core-ktx:1.13.1") // Güncel stabil sürüm
}