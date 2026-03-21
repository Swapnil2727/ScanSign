plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace  = "com.spatel.scansign.core.data"
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
    implementation(projects.core.model)
    implementation(projects.core.database)
    implementation(projects.core.pdf)
    implementation(libs.kotlinx.coroutines.core)
}
