import java.util.*

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("androidx.navigation.safeargs.kotlin")
    id("dagger.hilt.android.plugin")
}

android {
    compileSdkVersion(30)
    defaultConfig {
        applicationId = "com.cwlarson.deviceid"
        minSdkVersion(14)
        targetSdkVersion(30)
        versionCode = 15
        versionName = "1.4.2"
        vectorDrawables.useSupportLibrary = true
        testInstrumentationRunner = "com.cwlarson.deviceid.CustomTestRunner"
    }
    buildFeatures.viewBinding = true
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions.jvmTarget = JavaVersion.VERSION_1_8.toString()
    testOptions.animationsDisabled = true
    testBuildType = "debug"
}

val kotlinVersion: String by rootProject.extra
val navVersion: String by rootProject.extra
val hiltVersion: String by rootProject.extra
val fragmentVersion = "1.3.4"
val lifecycleVersion = "2.4.0-alpha01"
val coroutinesVersion = "1.5.0"
dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")
    implementation("androidx.core:core-ktx:1.5.0")
    implementation("androidx.activity:activity-ktx:1.2.3")
    implementation("androidx.fragment:fragment-ktx:$fragmentVersion")
    implementation("androidx.recyclerview:recyclerview:1.2.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("com.google.android.material:material:1.3.0")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.1.0")
    implementation("androidx.constraintlayout:constraintlayout:2.0.4")
    implementation("androidx.preference:preference-ktx:1.1.1")
    implementation("androidx.webkit:webkit:1.4.0")
    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-process:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-common-java8:$lifecycleVersion")
    //Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:$navVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$navVersion")
    implementation("com.google.android.play:core-ktx:1.8.1") { because("Google Play App Updates") }
    // Timber
    implementation("com.jakewharton.timber:timber:4.7.1")
    // Hilt
    implementation("com.google.dagger:hilt-android:$hiltVersion")
    kapt("com.google.dagger:hilt-compiler:$hiltVersion")
    // Instrumentation Testing
    //androidTestImplementation("androidx.test:core:1.3.0")
    androidTestImplementation("androidx.test:runner:1.3.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")
    androidTestImplementation("org.mockito:mockito-android:3.10.0")
    debugImplementation("androidx.fragment:fragment-testing:$fragmentVersion")
    androidTestImplementation("com.google.dagger:hilt-android-testing:$hiltVersion")
    kaptAndroidTest("com.google.dagger:hilt-android-compiler:$hiltVersion")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion") {
        // conflicts with mockito due to direct inclusion of byte buddy
        exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-debug")
    }
    androidTestImplementation("androidx.navigation:navigation-testing:$navVersion")
    // Unit Testing
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
    testImplementation("androidx.navigation:navigation-testing:$navVersion")
    //LeakCanary
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.7")
}
