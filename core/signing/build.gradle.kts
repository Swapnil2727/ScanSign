plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.spatel.scansign.core.signing"
    compileSdk {
        version = release(36)
    }
    defaultConfig { minSdk = 29 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
