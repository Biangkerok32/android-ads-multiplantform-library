package com.eyu.common;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.appsflyer.AppsFlyerConversionListener;
import com.appsflyer.AppsFlyerLib;
import com.crashlytics.android.Crashlytics;
import com.eyu.common.firebase.EventHelper;
import com.eyu.common.firebase.EyuRemoteConfigHelper;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.Map;

//import com.google.android.gms.analytics.GoogleAnalytics;
//import com.google.android.gms.analytics.HitBuilders;
//import com.google.android.gms.analytics.Tracker;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;

public class SdkHelper {
    private static boolean isUmSdkInited = false;
    private static final String KEY_READ_PHONE_STATE_PERMISSION_DIALOG_HAS_SHOW = "KEY_READ_PHONE_STATE_PERMISSION_DIALOG_HAS_SHOW";
    private static final String TAG = "SdkHelper";
    public static void initAppFlyerSdk(String key, AppsFlyerConversionListener conversionDataListener, Application application, String uninstallKey)
    {
        AppsFlyerLib.getInstance().init(key, conversionDataListener, application);
        AppsFlyerLib.getInstance().enableUninstallTracking(uninstallKey);
        AppsFlyerLib.getInstance().setCollectAndroidID(true);
        AppsFlyerLib.getInstance().setCollectIMEI(true);
        //通过该API设置两次Session上报的时间间隔。
        //Activity onResume()会有首次Session的上报，用户授权后获取IMEI，会有第二次Session的上报，
        //如果两次Session上报的时间间隔小于设定的值，则第二次Session会被block掉。
        AppsFlyerLib.getInstance().setMinTimeBetweenSessions(2);
        AppsFlyerLib.getInstance().startTracking(application);
    }

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

    public static void initUmSdk(Context context, String appkey, String channel)
    {
        UMConfigure.init(context, appkey, channel, UMConfigure.DEVICE_TYPE_PHONE, null);
        UMConfigure.setEncryptEnabled(true);
        isUmSdkInited = true;
    }

    public static void onResume(Activity activity){
        AppEventsLogger.activateApp(activity);
        if(isUmSdkInited){
            MobclickAgent.onResume(activity);
        }
    }

    public static void onPause(Activity activity){
        if(isUmSdkInited){
            MobclickAgent.onPause(activity);
        }
    }

    public static void appsflyerPermission(Activity activity) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
        if(preferences.getBoolean(KEY_READ_PHONE_STATE_PERMISSION_DIALOG_HAS_SHOW, false)){
            return;
        }

        preferences.edit().putBoolean(KEY_READ_PHONE_STATE_PERMISSION_DIALOG_HAS_SHOW, true).apply();

        Dexter.withActivity(activity)
                .withPermission(Manifest.permission.READ_PHONE_STATE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                            String imei = ((TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
                            Log.d(TAG, "imei = " + imei);
                            AppsFlyerLib.getInstance().setImeiData(imei);
                            String androidId = Settings.Secure.getString(activity.getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
                            AppsFlyerLib.getInstance().setAndroidIdData(androidId);
                            Log.d(TAG, "androidId = " + androidId);
                        }
                        AppsFlyerLib.getInstance().setCollectIMEI(true);
                        AppsFlyerLib.getInstance().setCollectAndroidID(true);
                        //NOTE: Here the report session API is reportTrackSession() not the startTracking()
                        AppsFlyerLib.getInstance().reportTrackSession(activity.getApplication());
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();

    }
}
