package com.eyu.common;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.appsflyer.AppsFlyerConversionListener;
import com.appsflyer.AppsFlyerLib;
import com.crashlytics.android.Crashlytics;
import com.eyu.common.firebase.EventHelper;
import com.eyu.common.firebase.EyuRemoteConfigHelper;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.exoplayer2.C;
//import com.google.android.gms.analytics.GoogleAnalytics;
//import com.google.android.gms.analytics.HitBuilders;
//import com.google.android.gms.analytics.Tracker;
import com.facebook.FacebookSdk;

import java.util.Map;

public class SdkHelper {
    public static void initAppFlyerSdk(String key, AppsFlyerConversionListener conversionDataListener, Application application, String uninstallKey)
    {
        AppsFlyerLib.getInstance().init(key, conversionDataListener, application);
        AppsFlyerLib.getInstance().enableUninstallTracking(uninstallKey);
        AppsFlyerLib.getInstance().startTracking(application);
        AppsFlyerLib.getInstance().setCollectAndroidID(false);
        AppsFlyerLib.getInstance().setCollectIMEI(false);
        AppsFlyerLib.getInstance().reportTrackSession(application);
    }

//    public static void initGaSdk(Context context, int gaResId)
//    {
//        GoogleAnalytics analytics = GoogleAnalytics.getInstance(context);
//        Tracker tracker = analytics.newTracker(gaResId);
//        HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder("event", "section_start");
//        tracker.send(eventBuilder.build());
//    }

    public static void initCrashlytics(String deviceModel, String udid)
    {
        Crashlytics.setString("device", deviceModel);
        Crashlytics.setString("UDID", udid);
    }

    public static void initRemoteConfig(Context context, Map<String, Object> defaultsMap){
        EyuRemoteConfigHelper.initRemoteConfig(context, defaultsMap);
    }

    public static void init(Context context){
        FacebookSdk.sdkInitialize(context);
        EventHelper.init(context);
    }

    public static void onResume(Activity activity){
        AppEventsLogger.activateApp(activity);

    }

    public static void onPause(Activity activity){

    }
}
