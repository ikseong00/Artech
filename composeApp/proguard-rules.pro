# kotlinx-serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class org.ikseong.artech.**$$serializer { *; }
-keepclassmembers class org.ikseong.artech.** {
    *** Companion;
}
-keepclasseswithmembers class org.ikseong.artech.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Ktor - engine discovery (ServiceLoader)
-keep class io.ktor.client.engine.**EngineContainer { *; }
-keepclassmembers class io.ktor.** { volatile <fields>; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
