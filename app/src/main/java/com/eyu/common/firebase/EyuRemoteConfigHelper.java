package com.eyu.common.firebase;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.eyu.common.BuildConfig;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by woo on 2018/5/8.
 */

public class EyuRemoteConfigHelper {

    private static final String TAG = "EyuRemoteConfigHelper";
    private static FirebaseRemoteConfig sFirebaseRemoteConfig;
    private static Map<String, Object> sDefaultsMap = new HashMap<>();


    public static void initRemoteConfig(Context context, Map<String, Object> defaultsMap) {
        if (sFirebaseRemoteConfig != null) {
            return;
        } else {
            try {
                sFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

                // Create a Remote Config Setting to enable developer mode, which you can use to increase
                // the number of fetches available per hour during development. See Best Practices in the
                // README for more information.
                // [START enable_dev_mode]
                FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                        .setDeveloperModeEnabled(BuildConfig.DEBUG)
                        .build();
                sFirebaseRemoteConfig.setConfigSettings(configSettings);
                // [END enable_dev_mode]
            } catch (Exception e) {
                Log.d(TAG, "sFirebaseRemoteConfig init failed");
            }
        }
        // Set default Remote Config parameter values. An app uses the in-app default values, and
        // when you need to adjust those defaults, you set an updated value for only the values you
        // want to change in the Firebase console. See Best Practices in the README for more
        // information.
        // [START set_default_values]
        // sFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);

        Log.d(TAG, defaultsMap.toString());
        if (sFirebaseRemoteConfig != null) {
            sFirebaseRemoteConfig.setDefaults(defaultsMap);
        }
        // [END set_default_values]
        sDefaultsMap.putAll(defaultsMap);
    }

    public static String getString(String key) {
        if (sFirebaseRemoteConfig != null) {
            return sFirebaseRemoteConfig.getString(key);
        } else {
            return getDefaultString(key);
        }
    }

    public static boolean getBoolean(String key) {
        if (sFirebaseRemoteConfig != null) {
            return sFirebaseRemoteConfig.getBoolean(key);
        } else {
            return (boolean) sDefaultsMap.get(key);
        }
    }

    public static int getInt(String key) {
        if (sFirebaseRemoteConfig != null) {
            return (int) sFirebaseRemoteConfig.getLong(key);
        } else {
            return (int) sDefaultsMap.get(key);
        }
    }

    public static long getLong(String key) {
        if (sFirebaseRemoteConfig != null) {
            return sFirebaseRemoteConfig.getLong(key);
        } else {
            return (long) sDefaultsMap.get(key);
        }
    }

    public static String getDefaultString(String key) {
        return (String) sDefaultsMap.get(key);
    }

    public static String readRawString(Context context, int resId)
    {
        String str = "";
        InputStream in = null;
        try{
            in = context.getResources().openRawResource(resId);
            byte[] buf = new byte[in.available()];
            in.read(buf,0, in.available());
            str = new String(buf);
        }catch(Exception ex){
            Log.e(TAG,"readString error", ex);
        }finally {
            if(in!=null){
                try{
                    in.close();
                }catch (Exception ex){
                    Log.e(TAG,"readString close in error", ex);
                }
            }
        }
        return str;
    }

    public static void fetchRemoteConfig() {
        if (sFirebaseRemoteConfig == null) {
            Log.e(TAG, "fetchRemoteConfig sFirebaseRemoteConfig is null");
            return;
        }
        long cacheExpiration = 3600; // 1 hour in seconds.
        // If your app is using developer mode, cacheExpiration is set to 0, so each fetch will
        // retrieve values from the service.
        if (sFirebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }
        Log.d(TAG, "start fetch remote config.");
        // [START fetch_config_with_callback]
        // cacheExpirationSeconds is set to cacheExpiration here, indicating the next fetch request
        // will use fetch data from the Remote Config service, rather than cached parameter values,
        // if cached parameter values are more than cacheExpiration seconds old.
        // See Best Practices in the README for more information.
        sFirebaseRemoteConfig.fetch(cacheExpiration)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            // After config data is successfully fetched, it must be activated before newly fetched
                            // values are returned.
                            try {
                                sFirebaseRemoteConfig.activateFetched();
                            } catch (Exception e) {
                                Log.e(TAG, "Maybe crash on samsung device, reported from Firebase. error : " + e);
                            }
                            Log.d(TAG, "fetch remote config success.");
                        } else {
                            Log.e(TAG, "fetch remote config failed");
                        }
                    }
                });
        // [END fetch_config_with_callback]
    }
}
