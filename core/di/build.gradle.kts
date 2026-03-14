plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.spatel.scansign.core.di"
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
    implementation(libs.koin.core)
    implementation(libs.koin.android)
}
