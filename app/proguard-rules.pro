# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in the SDK tools.

# Keep Firebase classes
-keep class com.google.firebase.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

# Keep data model classes for Firestore serialization
-keep class com.mindmatrix.employeetracker.data.model.** { *; }
