plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.scansign.app.core.pdf"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        minSdk = 29
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.pdfbox.android)
    implementation(libs.kotlinx.coroutines.core)
}
