plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace  = "com.scansign.app.core.data"
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
    implementation(projects.core.signing)
    implementation(libs.kotlinx.coroutines.core)
}
