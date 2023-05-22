# General
-keepattributes SourceFile,LineNumberTable,*Annotation*,EnclosingMethod,Signature,Exceptions,InnerClasses

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile
#-repackageclasses
#-allowaccessmodification

#Firebase Crashlytics
-keep,includedescriptorclasses public class * extends java.lang.Exception

# Gson
-keep,allowobfuscation,allowoptimization class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keep class sun.misc.Unsafe { *; }
-keepclassmembers enum * { *; }
-dontwarn sun.misc.**

# Ignore JSR 305 annotations for embedding nullability information.
-dontwarn javax.annotation.**

-keep class org.apache.xerces.**

# Encryption
-keep class jp.co.soramitsu.crypto.** { *; }
-keep class org.spongycastle.** { *; }

# Keep sora sdk classes
-keep class jp.co.soramitsu.sora.sdk.** { *; }
-keep class jp.co.soramitsu.fearless_utils.** { *; }
-keep class net.jpountz.** { *; }
-keep class com.sun.jna.** { *; }

##--------------- Begin: Ed25519Sha3 ----------
-keep class jp.co.soramitsu.crypto.ed25519.Ed25519Sha3
-keep class org.spongycastle.jcajce.provider.digest.SHA* { *; }
##--------------- End: Ed25519Sha3 ----------
