# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Readable stack traces in Crashlytics.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-keepattributes Signature,InnerClasses,EnclosingMethod,*Annotation*,Exceptions

# Gson models saved to files and preferences.
-keep class org.openbot.app.robot.tflite.Model { *; }
-keep class org.openbot.app.robot.tflite.Model$* { *; }
-keep class org.openbot.app.robot.projects.ProjectsDataInObject { *; }
-keep class * implements org.openbot.app.robot.utils.PostProcessingEnabler$PostProcessable { *; }
-keep class * extends com.google.gson.reflect.TypeToken
-keep class * extends com.google.common.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class com.google.common.reflect.TypeToken

# Blockly JavaScript bridge.
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Native code calls these classes by name.
-keep class org.webrtc.** { *; }
-keep class org.tensorflow.lite.** { *; }
-keep class com.google.ar.core.** { *; }
-keep class pl.droidsonroids.gif.** { *; }

# Libraries that use reflection.
-keep class com.felhr.** { *; }
-keep class com.abemart.wroup.** { *; }
-keep class com.ficat.** { *; }
-keep class com.pedro.** { *; }
-keep class com.koushikdutta.** { *; }
-keep class com.google.api.client.** { *; }
-keep class com.google.api.services.drive.** { *; }
-keepclassmembers class * {
    @com.google.api.client.util.Key <fields>;
}

# Optional classes the libraries reference but don't need on Android.
-dontwarn com.annimon.stream.**
-dontwarn org.bouncycastle.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**
-dontwarn org.slf4j.impl.StaticLoggerBinder

# Navigation 2.3 reads @Navigator.Name at runtime.
-keep class * extends androidx.navigation.Navigator
