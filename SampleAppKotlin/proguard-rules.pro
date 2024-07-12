# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
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
#Keep files:
-keepclasseswithmembers class com.daon.fido.** {
    *;
}
-keepclasseswithmembers class com.daon.sdk.** {
    *;
}
-keepclasseswithmembers class com.daon.fidosdklib.** {
    *;
}
-keep interface com.daon.fido.** {
  <methods>;
}

-keep interface com.daon.sdk.** {
  <methods>;
}

-keep interface com.daon.fidosdklib.** {
  <methods>;
}
-keepattributes InnerClasses
-dontoptimize
-keepattributes EnclosingMethod