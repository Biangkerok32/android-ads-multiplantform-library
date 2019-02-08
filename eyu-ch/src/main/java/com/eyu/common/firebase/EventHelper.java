package com.eyu.common.firebase;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.appsflyer.AppsFlyerLib;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by woo on 2017/10/13.
 */

public class EventHelper {
    private static final String TAG = "FirebaseHelper";
    private static FirebaseAnalytics sFirebaseAnalytics;
    private static Context sContext;

    public static void init(Context context) {
        sContext = context.getApplicationContext();
        sFirebaseAnalytics = FirebaseAnalytics.getInstance(sContext);
    }

    public static void logEvent(String event, String jsonStr) {
        if (sFirebaseAnalytics == null) return;
        // 添加 GA 的统计
        try {
//            HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder("event", event);
            Bundle params = new Bundle();
            Map<String, Object> objectValue = new HashMap<>();
            Map<String, String> eventValue = new HashMap<>();
            if(jsonStr!=null) {
//                eventBuilder.setLabel(jsonStr);
                JSONObject jsonObject = new JSONObject(jsonStr);
                Iterator<String> it = jsonObject.keys();
                while (it.hasNext()) {
                    String key = it.next();
                    params.putString(key, jsonObject.getString(key));
                    //eventBuilder.set(key, jsonObject.getString(key));
                    eventValue.put(key, jsonObject.getString(key));
                    objectValue.put(key, jsonObject.getString(key));
                }
            }
            sFirebaseAnalytics.logEvent(event, params);
//            mTracker.send(eventBuilder.build());
            AppsFlyerLib.getInstance().trackEvent(sContext, event, objectValue);
            MobclickAgent.onEvent(sContext, event, eventValue);
        } catch (Exception e) {
            Log.e(TAG, "_logEvent name Exception: ",e);
        }
    }
}