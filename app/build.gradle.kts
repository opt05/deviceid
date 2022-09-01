import java.util.*

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("kotlin-parcelize")
    id("dagger.hilt.android.plugin")
}

android {
    compileSdk = 32
    defaultConfig {
        applicationId = "com.cwlarson.deviceid"
        minSdk = 21
        targetSdk = 32
        versionCode = 15
        versionName = "1.4.2"
        vectorDrawables.useSupportLibrary = true
        testInstrumentationRunner = "com.cwlarson.deviceid.CustomTestRunner"
    }
    buildFeatures.compose = true
    signingConfigs {
        create("release") {
            val keystorePropertiesFile = rootProject.file("release.properties")
            if (keystorePropertiesFile.exists()) {
                val props = Properties()
                keystorePropertiesFile.inputStream().use { props.load(it) }
                storeFile = rootProject.file(props.getProperty("keyStore"))
                storePassword = props.getProperty("keyStorePassword")
                keyAlias = props.getProperty("keyAlias")
                keyPassword = props.getProperty("keyAliasPassword")
            }
        }
    }
    buildTypes {
        getByName("release") {
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()
    kotlin.sourceSets.all { languageSettings.optIn("kotlin.RequiresOptIn") }
    composeOptions.kotlinCompilerExtensionVersion = "1.3.0"
    testOptions {
        animationsDisabled = true
        unitTests.isIncludeAndroidResources = true
        unitTests.all { it.jvmArgs("-Xmx2g") }
        kotlinOptions.freeCompilerArgs +=
            listOf("-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi")
    }
    testBuildType = "debug"
    kapt.correctErrorTypes = true
}

val coroutinesVersion = "1.6.4"
val hiltVersion: String by rootProject.extra
val lifecycleVersion = "2.5.1"
val composeVersion = "1.2.1"
val composeAccompanistVersion = "0.25.1"
val datastoreVersion = "1.0.0"
val mockkVersion = "1.12.5"
dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")
    implementation("com.google.android.material:material:1.6.1")
    implementation("androidx.webkit:webkit:1.5.0")
    implementation("androidx.datastore:datastore:$datastoreVersion")
    implementation("androidx.datastore:datastore-preferences:$datastoreVersion")
    implementation("androidx.core:core-ktx:1.8.0")
    implementation("androidx.core:core-splashscreen:1.0.0")
    // Compose
    implementation("androidx.activity:activity-compose:1.5.1")
    implementation("androidx.compose.ui:ui:$composeVersion")
    // Tooling support (Previews, etc.)
    debugImplementation("androidx.compose.ui:ui-tooling:$composeVersion")
    implementation("androidx.compose.ui:ui-tooling-preview:$composeVersion")
    // Foundation (Border, Background, Box, Image, Scroll, shapes, animations, etc.)
    implementation("androidx.compose.foundation:foundation:$composeVersion")
    // Material Design
    implementation("androidx.compose.material:material:$composeVersion")
    // Material design icons
    implementation("androidx.compose.material:material-icons-core:$composeVersion")
    implementation("androidx.compose.material:material-icons-extended:$composeVersion")
    // Compose Accompanist
    implementation("com.google.accompanist:accompanist-systemuicontroller:$composeAccompanistVersion")
    implementation("com.google.accompanist:accompanist-swiperefresh:$composeAccompanistVersion")
    implementation("com.google.accompanist:accompanist-permissions:$composeAccompanistVersion")
    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-process:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-common-java8:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycleVersion")
    //Navigation
    implementation("androidx.navigation:navigation-compose:2.5.1")
    // Google Play App Updates
    implementation("com.google.android.play:core:1.10.3")
    implementation("com.google.android.play:core-ktx:1.8.1")
    // Timber
    implementation("com.jakewharton.timber:timber:5.0.1")
    // Hilt
    implementation("com.google.dagger:hilt-android:$hiltVersion")
    implementation("androidx.hilt:hilt-navigation-compose:1.0.0")
    kapt("com.google.dagger:hilt-android-compiler:$hiltVersion")
    androidTestImplementation("com.google.dagger:hilt-android-testing:$hiltVersion")
    kaptAndroidTest("com.google.dagger:hilt-android-compiler:$hiltVersion")
    // Instrumentation Testing
    androidTestImplementation("androidx.test:runner:1.4.0")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:$composeVersion")
    debugImplementation("androidx.compose.ui:ui-test-manifest:$composeVersion")
    androidTestImplementation("io.mockk:mockk-android:$mockkVersion")
    //androidTestImplementation("io.mockk:mockk-agent-jvm:$mockkVersion")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.4.0")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.2.0")
    // Unit Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("io.mockk:mockk-agent-jvm:$mockkVersion")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
    testImplementation("app.cash.turbine:turbine:0.9.0")
    testImplementation("androidx.compose.ui:ui-test-junit4:$composeVersion")
    // Robolectric Testing
    testImplementation("org.robolectric:robolectric:4.8.2")
    testImplementation("androidx.test.ext:junit-ktx:1.1.3")
    testImplementation("androidx.test:rules:1.4.0")
    // LeakCanary
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.9.1")
}