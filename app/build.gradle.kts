import java.util.Properties

plugins {
    id("com.android.application")
    kotlin("android")
    id("com.google.devtools.ksp")
    id("kotlin-parcelize")
    id("dagger.hilt.android.plugin")
}

android {
    namespace = "com.cwlarson.deviceid"
    compileSdk = 34
    defaultConfig {
        minSdk = 21
        targetSdk = 34
        versionCode = 18
        versionName = "1.5.1"
        vectorDrawables.useSupportLibrary = true
        testInstrumentationRunner = "com.cwlarson.deviceid.CustomTestRunner"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
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
            //signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions.jvmTarget = JavaVersion.VERSION_17.toString()
    composeOptions.kotlinCompilerExtensionVersion = "1.5.5"
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
}

val coroutinesBom: Dependency = dependencies.platform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.7.3")
val hiltVersion: String by rootProject.extra
val lifecycleVersion = "2.6.2"
val composeBom: Dependency = dependencies.platform("androidx.compose:compose-bom:2023.10.01")
val composeAccompanistVersion = "0.32.0"
val datastoreVersion = "1.0.0"
val mockkVersion = "1.13.8"
dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(coroutinesBom)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.webkit:webkit:1.9.0")
    implementation("androidx.datastore:datastore:$datastoreVersion")
    implementation("androidx.datastore:datastore-preferences:$datastoreVersion")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.core:core-splashscreen:1.0.1")
    // Compose
    implementation(composeBom)
    implementation("androidx.activity:activity-compose:1.8.1")
    implementation("androidx.compose.ui:ui")
    // Tooling support (Previews, etc.)
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.compose.ui:ui-tooling-preview")
    // Foundation (Border, Background, Box, Image, Scroll, shapes, animations, etc.)
    implementation("androidx.compose.foundation:foundation")
    // Material Design
    implementation("androidx.compose.material:material")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material3:material3-window-size-class")
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
    implementation("androidx.navigation:navigation-compose:2.7.5")
    // Google Play App Updates
    implementation("com.google.android.play:app-update-ktx:2.1.0")
    // Timber
    implementation("com.jakewharton.timber:timber:5.0.1")
    // Hilt
    implementation("com.google.dagger:hilt-android:$hiltVersion")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    ksp("com.google.dagger:hilt-android-compiler:$hiltVersion")
    androidTestImplementation("com.google.dagger:hilt-android-testing:$hiltVersion")
    kspAndroidTest("com.google.dagger:hilt-android-compiler:$hiltVersion")
    // Instrumentation Testing
    androidTestImplementation("androidx.test:core-ktx:1.5.0")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
    androidTestImplementation(composeBom)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    androidTestImplementation("io.mockk:mockk-android:$mockkVersion")
    androidTestImplementation("io.mockk:mockk-agent:$mockkVersion")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.5.1")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.2.0")
    // Unit Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk-android:$mockkVersion")
    testImplementation("io.mockk:mockk-agent:$mockkVersion")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
    testImplementation("app.cash.turbine:turbine:1.0.0")
    testImplementation(composeBom)
    testImplementation("androidx.compose.ui:ui-test-junit4")
    // Robolectric Testing
    testImplementation("org.robolectric:robolectric:4.11")
    testImplementation("androidx.test.ext:junit-ktx:1.1.5")
    testImplementation("androidx.test:rules:1.5.0")
    // LeakCanary
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.12")
}