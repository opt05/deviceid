import java.util.*

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("kotlin-parcelize")
    id("dagger.hilt.android.plugin")
}

android {
    namespace = "com.cwlarson.deviceid"
    compileSdk = 33
    defaultConfig {
        minSdk = 21
        targetSdk = 33
        versionCode = 16
        versionName = "1.4.3"
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
    composeOptions.kotlinCompilerExtensionVersion = "1.4.4"
    testOptions {
        animationsDisabled = true
        unitTests.isIncludeAndroidResources = true
        unitTests.all { it.jvmArgs("-Xmx2g") }
        kotlinOptions.freeCompilerArgs +=
            listOf("-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi")
    }
    packagingOptions.resources.merges.addAll(
        listOf("META-INF/LICENSE.md", "META-INF/LICENSE-notice.md")
    )
    testBuildType = "debug"
    kapt.correctErrorTypes = true
}

val coroutinesVersion = "1.6.4"
val hiltVersion: String by rootProject.extra
val lifecycleVersion = "2.6.1"
val composeBom = dependencies.platform("androidx.compose:compose-bom:2023.03.00")
val composeMaterial3Version = "1.1.0-beta01"
val composeAccompanistVersion = "0.30.0"
val datastoreVersion = "1.0.0"
val mockkVersion = "1.13.3"
dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")
    implementation("com.google.android.material:material:1.8.0")
    implementation("androidx.webkit:webkit:1.6.1")
    implementation("androidx.datastore:datastore:$datastoreVersion")
    implementation("androidx.datastore:datastore-preferences:$datastoreVersion")
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.core:core-splashscreen:1.0.0")
    // Compose
    implementation(composeBom)
    implementation("androidx.activity:activity-compose:1.7.0")
    implementation("androidx.compose.ui:ui")
    // Tooling support (Previews, etc.)
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.compose.ui:ui-tooling-preview")
    // Foundation (Border, Background, Box, Image, Scroll, shapes, animations, etc.)
    implementation("androidx.compose.foundation:foundation")
    // Material Design
    implementation("androidx.compose.material:material") {
        because("PullRefresh is not in Material 3 yet")
    }
    implementation("androidx.compose.material3:material3:$composeMaterial3Version")
    implementation("androidx.compose.material3:material3-window-size-class:$composeMaterial3Version")
    implementation("androidx.compose.ui:ui-text-google-fonts")
    // Material design icons
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.material:material-icons-extended")
    // Compose Accompanist
    implementation("com.google.accompanist:accompanist-systemuicontroller:$composeAccompanistVersion")
    implementation("com.google.accompanist:accompanist-permissions:$composeAccompanistVersion")
    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-process:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-common-java8:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:$lifecycleVersion")
    //Navigation
    implementation("androidx.navigation:navigation-compose:2.5.3")
    // Google Play App Updates
    implementation("com.google.android.play:app-update-ktx:2.0.1")
    // Timber
    implementation("com.jakewharton.timber:timber:5.0.1")
    // Hilt
    implementation("com.google.dagger:hilt-android:$hiltVersion")
    implementation("androidx.hilt:hilt-navigation-compose:1.0.0")
    kapt("com.google.dagger:hilt-android-compiler:$hiltVersion")
    androidTestImplementation("com.google.dagger:hilt-android-testing:$hiltVersion")
    kaptAndroidTest("com.google.dagger:hilt-android-compiler:$hiltVersion")
    // Instrumentation Testing
    androidTestImplementation("androidx.test:core-ktx:1.5.0")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
    androidTestImplementation(composeBom)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    androidTestImplementation("io.mockk:mockk-android:$mockkVersion")
    //androidTestImplementation("io.mockk:mockk-agent-jvm:$mockkVersion")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.5.1")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.2.0")
    // Unit Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("io.mockk:mockk-agent-jvm:$mockkVersion")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
    testImplementation("app.cash.turbine:turbine:0.12.1")
    testImplementation(composeBom)
    testImplementation("androidx.compose.ui:ui-test-junit4")
    // Robolectric Testing
    testImplementation("org.robolectric:robolectric:4.9.2")
    testImplementation("androidx.test.ext:junit-ktx:1.1.5")
    testImplementation("androidx.test:rules:1.5.0")
    // LeakCanary
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.10")
}