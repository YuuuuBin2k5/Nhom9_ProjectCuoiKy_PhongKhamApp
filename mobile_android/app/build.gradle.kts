import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.Collections
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

fun resolveBackendBaseUrl(rootDir: java.io.File): String {
    val localPropsFile = rootDir.resolve("mobile_android/local.properties")
    if (localPropsFile.exists()) {
        val props = Properties()
        localPropsFile.inputStream().use { props.load(it) }
        val host = props.getProperty("backend.host", "192.168.1.6")
        val port = props.getProperty("backend.port", "8081")
        return "http://$host:$port/"
    }
    return "http://192.168.1.6:8081/"
}

android {
    namespace = "com.hcmute.mobile_android"
    compileSdk = 36

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.hcmute.mobile_android"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val apiUrl = resolveBackendBaseUrl(rootProject.projectDir)
        logger.lifecycle("[Toothly] API_BASE_URL → $apiUrl")
        buildConfigField(
            "String",
            "API_BASE_URL",
            "\"${apiUrl.replace("\\", "\\\\").replace("\"", "\\\"")}\""
        )
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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    
    // Glide
    implementation(libs.glide)
    
    // PhotoView for pinch-to-zoom images
    implementation("com.github.chrisbanes:PhotoView:2.3.0")
    
    // SwipeRefreshLayout
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    
    // ZXing for QR Code generation and scanning
    implementation("com.google.zxing:core:3.5.2")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    
    // Camera permissions
    implementation("androidx.camera:camera-core:1.3.1")
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")
    
    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    
    // Security
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-database")

    // Animations (Phase 3)
    implementation("com.airbnb.android:lottie:6.3.0")
    
    // Charts (Phase 4)
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    
    // Excel & PDF Export (Phase 4)
    implementation("org.apache.poi:poi:5.2.3")
    implementation("org.apache.poi:poi-ooxml:5.2.3")
    implementation("com.itextpdf:itext7-core:7.2.5")
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
