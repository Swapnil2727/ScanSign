# ──── Line numbers for Crashlytics stack traces ──────────────────────────────
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

# ──── PDFBox Android ──────────────────────────────────────────────────────────
-keep class com.tom_roush.pdfbox.** { *; }
-dontwarn com.tom_roush.pdfbox.**

# ──── Koin Dependency Injection ───────────────────────────────────────────────
-keep class org.koin.** { *; }
-dontwarn org.koin.**

# ──── Kotlinx Serialization (Navigation 3 Routes) ────────────────────────────
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep,includedescriptorclasses class com.scansign.app.**$$serializer { *; }
-keepclassmembers class com.scansign.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.scansign.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ──── Coil ────────────────────────────────────────────────────────────────────
-dontwarn coil.**

# ──── Play Services / ML Kit ─────────────────────────────────────────────────
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**
