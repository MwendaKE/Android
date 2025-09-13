# =====================================================
# Firebase & Support Library ProGuard Rules (AIDE-Compatible)
# =====================================================

# Core Firebase rules (16.x series)
-keep class com.google.firebase.** { *; }
-keep class com.google.firebase.provider.** { *; }
-keep class com.google.firebase.iid.** { *; }

# Google Play Services 16.x
-keep class com.google.android.gms.** { *; }
-keep class * implements com.google.android.gms.common.internal.IGmsServiceBroker
-keep class * implements com.google.android.gms.common.api.Api$ApiOptions$HasOptions

# Support Library 27.x
-keep class android.support.v4.** { *; }
-keep interface android.support.v4.** { *; }
-keep class android.support.annotation.** { *; }

# Required for Firebase Storage
-keep class org.apache.** { *; }
-keepattributes Signature, InnerClasses, Exceptions

# Fix for Java 8 compatibility
-dontwarn java.lang.invoke.**
-dontwarn sun.misc.**
-dontnote com.google.android.gms.**

# Firebase Analytics workaround
-keep class com.google.android.gms.measurement.** { *; }
-keep class com.google.firebase.analytics.** { *; }

# OkHttp/Okio rules (simplified for AIDE)
-keepnames class okhttp3.**
-keepnames class okio.**
-dontwarn okhttp3.**
-dontwarn okio.**

# Gson serialization
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# =====================================================
# End of rules
# =====================================================
