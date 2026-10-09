# Room entities are referenced reflectively — keep them whole.
-keep class com.intellipaat.learndash.data.local.entity.** { *; }

# kotlinx.serialization looks up generated serializers by name at runtime.
# Without this, release builds crash decoding courses.json.
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class com.intellipaat.learndash.data.remote.** {
    *** serializer(...);
}
