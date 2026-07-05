import java.util.Properties

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { localProperties.load(it) }
}
val mapboxToken = localProperties.getProperty("MAPBOX_TOKEN") ?: ""

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.enterprisemobile"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.enterprisemobile"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "MAPBOX_TOKEN", "\"$mapboxToken\"")
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.2")

    // Room essenziale
    implementation("androidx.room:room-runtime:2.8.4")
    ksp("androidx.room:room-compiler:2.8.4")

    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

    // Compose (Usa solo ciò che ti serve per il layout)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)

    // Retrofit (Essenziale per i tuoi itinerari)
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.gson)

    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Libreria per le icone extra come Place, Email, ecc. (HomeViaggiatoreActivity)
    implementation("androidx.compose.material:material-icons-extended:1.6.0")
    implementation("androidx.compose.runtime:runtime-livedata:1.6.0")
    //per Mapbox
    implementation("com.mapbox.maps:android:11.2.2")
    implementation("com.mapbox.extension:maps-compose:11.2.2")

    implementation("com.stripe:stripe-android:21.0.0")

    // Per le immagini dei viaggi
    implementation("io.coil-kt:coil-compose:2.6.0")

    //dipendenze per le notifiche sul dispositivo
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-messaging-ktx")
}