# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
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

#-keep class com.bytedance.sdk.openadsdk.** { *; }
#-keep class com.androidquery.callback.** {*;}
#-keep class com.bytedance.sdk.openadsdk.service.TTDownloadProvider

-keep class MTT.ThirdAppInfoNew {
    *;
}
-keep class com.tencent.** {
    *;
}
-keep class com.qq.e.** {
    public protected *;
}
-keep class android.support.v4.**{
   public *;
}
-keep class android.support.v7.**{
    public *;
}

#-keep class MTT.ThirdAppInfoNew {
#    *;
#}
#-keep class com.tencent.** {
#    *;
#}
#-keep class com.qq.e.** {
#    public protected *;
#}
#-keep class android.support.v4.**{
#   public *;
#}
#-keep class android.support.v7.**{
#    public *;
#}

#-keep class MTT.ThirdAppInfoNew {
#    *;
#}
#-keep class com.tencent.** {
#    *;
#}
#-keep class com.qq.e.** {
#    public protected *;
#}
#-keep class android.support.v4.**{
#   public *;
#}
#-keep class android.support.v7.**{
#    public *;
#}

# Mintegral
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.mintegral.** {*; }
-keep interface com.mintegral.** {*; }
-keep class android.support.v4.** { *; }
-dontwarn com.mintegral.**
-keep class **.R$* { public static final int mintegral*; }
-keep class com.alphab.** {*; }
-keep interface com.alphab.** {*; }

##umeng
-keep class com.umeng.** {*;}
-keepclassmembers class * {
   public <init> (org.json.JSONObject);
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}