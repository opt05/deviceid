import java.util.*

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("kotlin-parcelize")
    id("dagger.hilt.android.plugin")
}

hilt.enableAggregatingTask = true

val coroutinesVersion = "1.5.2"
val hiltVersion: String by rootProject.extra
val lifecycleVersion = "2.4.0-alpha03" //beta01 is 31+
val composeVersion = "1.0.2"
val composeAccompanistVersion = "0.18.0"
val datastoreVersion = "1.0.0"
val mockitoVersion = "3.12.4"
val mockitoKotlinVersion = "3.2.0"
android {
    compileSdk = 30
    defaultConfig {
        applicationId = "com.cwlarson.deviceid"
        minSdk = 21
        targetSdk = 30
        versionCode = 15
        versionName = "1.4.2"
        vectorDrawables.useSupportLibrary = true
        testInstrumentationRunner = "com.cwlarson.deviceid.CustomTestRunner"
    }
    buildFeatures {
        viewBinding = true
        compose = true
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
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()
    composeOptions.kotlinCompilerExtensionVersion = composeVersion
    testOptions {
        animationsDisabled = true
        unitTests.isIncludeAndroidResources = true
        unitTests.all { it.jvmArgs("-Xmx2g") }
    }
    kapt.correctErrorTypes = true
    packagingOptions {
        // for JNA and JNA-platform
        resources.excludes += "META-INF/AL2.0"
        resources.excludes += "META-INF/LGPL2.1"
        // for byte-buddy
        resources.excludes += "META-INF/licenses/**"
        resources.pickFirsts += "**/attach_hotspot_windows.dll"
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")
    implementation("com.google.android.material:material:1.4.0")
    implementation("androidx.webkit:webkit:1.4.0")
    implementation("androidx.datastore:datastore:$datastoreVersion")
    implementation("androidx.datastore:datastore-preferences:$datastoreVersion")
    implementation("androidx.core:core-ktx:1.6.0")
    // Compose
    implementation("androidx.activity:activity-compose:1.3.1")
    implementation("androidx.compose.compiler:compiler:$composeVersion")
    implementation("androidx.compose.ui:ui:$composeVersion")
    // Tooling support (Previews, etc.)
    debugImplementation("androidx.compose.ui:ui-tooling:$composeVersion")
    // Foundation (Border, Background, Box, Image, Scroll, shapes, animations, etc.)
    implementation("androidx.compose.foundation:foundation:$composeVersion")
    // Material Design
    implementation("androidx.compose.material:material:$composeVersion")
    // Material design icons
    implementation("androidx.compose.material:material-icons-core:$composeVersion")
    implementation("androidx.compose.material:material-icons-extended:$composeVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:1.0.0-alpha07")
    // Compose Accompanist
    implementation("com.google.accompanist:accompanist-insets:$composeAccompanistVersion")
    implementation("com.google.accompanist:accompanist-insets-ui:$composeAccompanistVersion")
    implementation("com.google.accompanist:accompanist-systemuicontroller:$composeAccompanistVersion")
    implementation("com.google.accompanist:accompanist-swiperefresh:$composeAccompanistVersion")
    implementation("com.google.accompanist:accompanist-permissions:$composeAccompanistVersion")
    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-process:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-common-java8:$lifecycleVersion")
    kapt("androidx.lifecycle:lifecycle-compiler:$lifecycleVersion")
    //Navigation
    implementation("androidx.navigation:navigation-compose:2.4.0-alpha06") //alpha07 is 31+
    // Google Play App Updates
    implementation("com.google.android.play:core:1.10.2")
    implementation("com.google.android.play:core-ktx:1.8.1")
    // Timber
    implementation("com.jakewharton.timber:timber:5.0.1")
    // Hilt
    implementation("com.google.dagger:hilt-android:$hiltVersion")
    kapt("com.google.dagger:hilt-android-compiler:$hiltVersion")
    implementation("androidx.hilt:hilt-navigation-compose:1.0.0-alpha03")
    // Instrumentation Testing
    androidTestImplementation("androidx.test:runner:1.4.0")
    androidTestImplementation("com.google.dagger:hilt-android-testing:$hiltVersion")
    kaptAndroidTest("com.google.dagger:hilt-android-compiler:$hiltVersion")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:$composeVersion")
    debugImplementation("androidx.compose.ui:ui-test-manifest:$composeVersion")
    androidTestImplementation("org.mockito:mockito-android:$mockitoVersion")
    androidTestImplementation("org.mockito.kotlin:mockito-kotlin:$mockitoKotlinVersion")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.4.0")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.2.0")
    // Unit Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito.kotlin:mockito-kotlin:$mockitoKotlinVersion")
    testImplementation("org.mockito:mockito-inline:$mockitoVersion")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
    testImplementation("app.cash.turbine:turbine:0.6.1")
    testImplementation("androidx.compose.ui:ui-test-junit4:$composeVersion")
    // Robolectric Testing
    testImplementation("org.robolectric:robolectric:4.6.1")
    testImplementation("androidx.test.ext:junit:1.1.3")
    testImplementation("androidx.test:rules:1.4.0")
    // LeakCanary
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.7")
}