# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\848525\AppData\Local\Android\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

#-------------------------------------------------
# Google Play App Updates
# This fixes: Caused by: androidx.core.app.CoreComponentFactory$InstantiationException:
# Unable to instantiate appComponentFactory androidx.core.app.CoreComponentFactory: make sure class
# name exists
#-------------------------------------------------
-keep class androidx.core.app.CoreComponentFactory { *; }