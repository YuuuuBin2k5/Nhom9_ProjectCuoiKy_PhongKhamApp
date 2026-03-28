import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.Collections
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

/** IPv4 private: 192.168.x, 10.x, 172.16–31.x (bỏ loopback / link-local). */
fun collectLanIPv4(): List<String> {
    return try {
        Collections.list(NetworkInterface.getNetworkInterfaces())
            .asSequence()
            .filter { it.isUp && !it.isLoopback }
            .flatMap { Collections.list(it.inetAddresses).asSequence() }
            .filterIsInstance<Inet4Address>()
            .filter { !it.isLoopbackAddress && !it.isLinkLocalAddress }
            .map { it.hostAddress }
            .filter { ha ->
                when {
                    ha.startsWith("192.168.") -> true
                    ha.startsWith("10.") -> true
                    ha.startsWith("172.") -> {
                        val octet = ha.substringAfter("172.").substringBefore(".").toIntOrNull() ?: return@filter false
                        octet in 16..31
                    }
                    else -> false
                }
            }
            .distinct()
            .toList()
    } catch (_: Exception) {
        emptyList()
    }
}

fun pickBestLanIp(candidates: List<String>): String? {
    if (candidates.isEmpty()) return null
    return candidates.firstOrNull { it.startsWith("192.168.") }
        ?: candidates.firstOrNull { it.startsWith("10.") }
        ?: candidates.firstOrNull { it.startsWith("172.") }
}

/**
 * Mặc định: tự đoán IP LAN máy build → điện thoại thật + emulator (qua IP máy) đều dùng được.
 * Không tìm thấy LAN → fallback http://10.0.2.2 (emulator classic).
 *
 * Ghi đè (local.properties), khi cần:
 * - backend.base.url=http://x.x.x.x:8081/
 * - backend.host=EMULATOR  → luôn 10.0.2.2
 * - backend.host=1.2.3.4   → IP cố định
 * - backend.port=8081
 */
fun resolveBackendBaseUrl(rootDir: java.io.File): String {
    val props = Properties()
    val lp = rootDir.resolve("local.properties")
    if (lp.isFile) lp.inputStream().use { props.load(it) }

    val explicit = props.getProperty("backend.base.url")?.trim()
    if (!explicit.isNullOrEmpty()) {
        return if (explicit.endsWith("/")) explicit else "$explicit/"
    }
    val port = props.getProperty("backend.port")?.trim()?.takeIf { it.isNotEmpty() } ?: "8081"
    val hostRaw = props.getProperty("backend.host")?.trim()

    if (hostRaw.equals("EMULATOR", ignoreCase = true)) {
        return "http://10.0.2.2:$port/"
    }
    if (!hostRaw.isNullOrBlank() && !hostRaw.equals("AUTO", ignoreCase = true)) {
        val host = hostRaw.removePrefix("http://").removePrefix("https://").substringBefore("/").substringBefore(":")
        return "http://$host:$port/"
    }
    val lan = pickBestLanIp(collectLanIPv4())
    return if (lan != null) "http://$lan:$port/" else "http://10.0.2.2:$port/"
}

android {
    namespace = "com.hcmute.mobile_android"
    compileSdk = 36

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.hcmute.mobile_android"
        minSdk = 24
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
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")
    
    // Security
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation(libs.glide)
    
    // Firebase Realtime Database
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-database")
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
