import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

// Load properties from local.properties
val properties = Properties()
properties.load(FileInputStream(rootProject.file("local.properties")))

val projectNumberSecret = properties.getProperty("projectNumberSecret")
val projectIdSecret = properties.getProperty("projectIdSecret")
val storageBucketSecret = properties.getProperty("storageBucketSecret")
val mobilesdkAppIdSecret = properties.getProperty("mobilesdkAppIdSecret")
val packageNameSecret = properties.getProperty("packageNameSecret")
val currentKeySecret = properties.getProperty("currentKeySecret")

android {
    namespace = "com.example.manjvisitors"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.manjvisitors"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "PROJECT_NUMBER_SECRET", "\"${projectNumberSecret}\"")
        buildConfigField("String", "PROJECT_ID_SECRET", "\"${projectIdSecret}\"")
        buildConfigField("String", "STORAGE_BUCKET_SECRET", "\"${storageBucketSecret}\"")
        buildConfigField("String", "MOBILE_SDK_APP_ID_SECRET", "\"${mobilesdkAppIdSecret}\"")
        buildConfigField("String", "PACKAGE_NAME_SECRET", "\"${packageNameSecret}\"")
        buildConfigField("String", "CURRENT_KEY_SECRET", "\"${currentKeySecret}\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(platform("com.google.firebase:firebase-bom:32.7.1"))
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-functions-ktx:20.4.0")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.08.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
