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
    implementation(projects.core.model)
    implementation(projects.core.pdf)
    implementation(projects.core.database)
    implementation(projects.core.data)
    implementation(projects.core.datastore)
    implementation(projects.core.signing)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.datastore.preferences)
}
