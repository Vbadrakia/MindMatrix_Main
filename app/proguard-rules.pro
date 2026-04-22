# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in the SDK tools.

# Keep Firebase classes
-keep class com.google.firebase.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

# Keep data model classes for Firestore serialization
-keep class com.mindmatrix.employeetracker.data.model.** { *; }

# Keep Hilt/Dagger generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.internal.GeneratedComponent { *; }
-dontwarn dagger.hilt.internal.**

# Keep Kotlin metadata used by Firebase serialization
-keep class kotlin.Metadata { *; }
