# Readable stack traces in Play Console
-keepattributes SourceFile,LineNumberTable

# Gson needs generic type info
-keepattributes Signature, *Annotation*, InnerClasses, EnclosingMethod

# Gson models (replace with your real package, or use @Keep instead)
-keep class com.ottrojja.YOUR.MODEL.PACKAGE.** { *; }

# YouTube player + WebView JS bridge
-keep class com.pierfrancescosoffritti.androidyoutubeplayer.** { *; }
-keepclassmembers class * { @android.webkit.JavascriptInterface <methods>; }

# Harmless library warnings
-dontwarn org.slf4j.**
-dontwarn java.lang.management.**