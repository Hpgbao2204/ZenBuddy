# Add project specific ProGuard rules here.
-keepattributes *Annotation*

# Supabase / Ktor
-keep class io.github.jan.supabase.** { *; }
-keep class io.ktor.** { *; }

# Retrofit
-keepattributes Signature
-keepattributes Exceptions
-keep class com.zenbuddy.data.remote.dto.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
