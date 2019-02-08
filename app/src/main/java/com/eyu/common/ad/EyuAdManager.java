package com.eyu.common.ad;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eyu.common.R;
import com.eyu.common.ad.adapter.NativeAdAdapter;
import com.eyu.common.ad.group.InterstitialAdCacheGroup;
import com.eyu.common.ad.group.NativeAdCacheGroup;
import com.eyu.common.ad.group.RewardAdCacheGroup;
import com.eyu.common.ad.listener.UnityAdListener;
import com.eyu.common.ad.model.AdCache;
import com.eyu.common.ad.model.AdConfig;
import com.eyu.common.ad.model.AdKey;
import com.eyu.common.ad.model.AdPlace;
import com.eyu.common.firebase.EventHelper;
import com.eyu.common.firebase.EyuRemoteConfigHelper;
import com.google.android.gms.ads.MobileAds;
import com.unity3d.ads.IUnityAdsListener;
import com.unity3d.ads.UnityAds;
import com.vungle.warren.InitCallback;
import com.vungle.warren.Vungle;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by woo on 2017/12/6.
 */

public class EyuAdManager implements EyuAdsListener{

    public static String NETWORK_FACEBOOK = "facebook";
    public static String NETWORK_ADMOB = "admob";
    public static String NETWORK_UNITY = "unity";
    public static String NETWORK_VUNGLE = "vungle";
    public static String NETWORK_APPLOVIN = "applovin";

    public static String TYPE_REWARD_AD = "rewardAd";
    public static String TYPE_INTERSTITIAL_AD = "interstitialAd";
    public static String TYPE_NATIVE_AD = "nativeAd";

    public static String EVENT_LOADING = "_LOADING";
    public static String EVENT_SHOW = "_SHOW";
    public static String EVENT_LOAD_FAILED = "_LOAD_FAILED";
    public static String EVENT_LOAD_SUCCESS = "_LOAD_SUCCESS";
    public static String EVENT_REWARDED = "_REWARDED";
    public static String EVENT_CLICKED = "_CLICKED";
    public static String EVENT_DEFAULT_AD_CLICKED = "EVENT_DEFAULT_AD_CLICKED";


    private static String TAG = "AdPlayer";

    private static Map<String, AdPlace> sAdPlaceMap = new HashMap<>();
    private static Map<String, AdKey> sAdKeyMap = new HashMap<>();
    private static Map<String, AdCache> sAdCacheMap = new HashMap<>();
    private static Map<String, String> sNativeAdLayoutMap = new HashMap<>();
    private static Map<String, RewardAdCacheGroup> sRewardAdCacheGroupMap = new HashMap<>();
    private static Map<String, InterstitialAdCacheGroup> sInterstitialAdCacheGroupMap = new HashMap<>();
    private static Map<String, NativeAdCacheGroup> sNativeAdCacheGroupMap = new HashMap<>();
    private static Map<Activity, Map<String, NativeAdViewContainer>> sNativeAdViewContainerMap = new HashMap<>();
    private static Map<String, UnityAdListener> sUnityAdListenerMap = new HashMap<>();

    private static Activity sNativeAdActivity;

    private static EyuAdManager instance;
    private EyuAdsListener mAdsListener;
    private Handler mHandler;
    private AdConfig mAdConfig;
    private boolean isAdmobRewardAdLoaded = false;
    private boolean isAdmobRewardAdLoading = false;

    static public EyuAdManager getInstance() {
        if (null == instance) {
            instance = new EyuAdManager();
        }
        return instance;
    }

    public void config(Activity activity, AdConfig adConfig, EyuAdsListener adsListener) {
        mAdConfig = adConfig;
        mHandler = new Handler(Looper.getMainLooper());
        loadAdConfig(adConfig);
        this.mAdsListener = adsListener;

        String unityClientId = adConfig.getUnityClientId();
        if(unityClientId !=null && !unityClientId.isEmpty()) {
            try {
                UnityAds.initialize(activity, unityClientId, sUnityAdsListener);
            }catch (Exception ex){
                Log.e(TAG, "UnityAds.initialize error", ex);
            }
        }

        String vungleClientId = adConfig.getVungleClientId();
        if(vungleClientId !=null && !vungleClientId.isEmpty() ) {
            Log.d(TAG, "VunglePub initialize vungleClientId = " + unityClientId );
            try {
                Vungle.init(vungleClientId, activity.getApplicationContext(), new InitCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "vungle initialize onSuccess ");
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e(TAG, "Vungle initialize onError ", throwable);
                    }

                    @Override
                    public void onAutoCacheAdAvailable(String s) {
                        Log.d(TAG, "Vungle initialize onAutoCacheAdAvailable s = " + s);
                    }

                });
            }catch (Exception ex){
                Log.e(TAG, "vungle initialize error", ex);
            }
        }

//        try {
//            AppLovinSdk.initializeSdk(activity.getApplicationContext());
//        }catch (Exception ex){
//            Log.e(TAG, "applovin initialize error", ex);
//        }

        initAdCacheGroup(activity, adConfig);
    }

    public void loadRewardedVideoAd(String adPlaceId) {
        Log.d(TAG, "loadRewardedVideoAd adPlaceId = " + adPlaceId);
        AdPlace adPlace = sAdPlaceMap.get(adPlaceId);
        if (adPlace != null) {
            String adCacheId = adPlace.getCacheGroupId();
            RewardAdCacheGroup cacheGroup = sRewardAdCacheGroupMap.get(adCacheId);
            if (cacheGroup != null) {
                cacheGroup.loadRewardedVideoAd(adPlaceId);
            }
        }
    }

    public void showRewardedVideoAd(Activity activity, String adPlaceId) {
        Log.d(TAG, "showRewardedVideoAd  adPlaceId = " + adPlaceId);
        AdPlace adPlace = sAdPlaceMap.get(adPlaceId);
        if (adPlace != null) {
            String adCacheId = adPlace.getCacheGroupId();
            RewardAdCacheGroup cacheGroup = sRewardAdCacheGroupMap.get(adCacheId);
            if (cacheGroup != null ) {
                cacheGroup.showRewardedVideoAd(activity, adPlaceId);
            }else{
                Log.e(TAG, "showRewardedVideoAd  adPlaceId = " + adPlaceId + " cacheGroup== null");
            }
        }else{
            Log.e(TAG, "showRewardedVideoAd  adPlaceId = " + adPlaceId + " is not found");
        }
    }

    public void loadInterstitialAd(String adPlaceId) {
        Log.d(TAG, "loadInterstitialAd adPlaceId = " + adPlaceId);
        AdPlace adPlace = sAdPlaceMap.get(adPlaceId);
        if (adPlace != null) {
            String adCacheId = adPlace.getCacheGroupId();
            InterstitialAdCacheGroup cacheGroup = sInterstitialAdCacheGroupMap.get(adCacheId);
            if (cacheGroup != null) {
                cacheGroup.loadAd(adPlaceId);
            }
        }
    }

    public void showInterstitialAd(Activity activity, String adPlaceId) {
        Log.d(TAG, "showInterstitialAd + adPlaceId = " + adPlaceId);
        AdPlace adPlace = sAdPlaceMap.get(adPlaceId);
        if (adPlace != null) {
            String adCacheId = adPlace.getCacheGroupId();
            InterstitialAdCacheGroup cacheGroup = sInterstitialAdCacheGroupMap.get(adCacheId);
            if (cacheGroup != null && cacheGroup.isAdLoaded()) {
                cacheGroup.showAd(activity, adPlaceId);
            }
        }
    }

    public void loadNativeAd(String adPlaceId) {
        Log.d(TAG, "loadNativeAd adPlaceId = " + adPlaceId);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    AdPlace adPlace = sAdPlaceMap.get(adPlaceId);
                    if (adPlace != null) {
                        String adCacheId = adPlace.getCacheGroupId();
                        NativeAdCacheGroup cacheGroup = sNativeAdCacheGroupMap.get(adCacheId);
                        if (cacheGroup != null) {
                            cacheGroup.loadAd(adPlaceId);
                        } else {
                            Log.e(TAG, "NativeAdCacheGroup is null for adPlaceId = " + adPlaceId);
                        }
                    } else {
                        Log.e(TAG, "AdPlace is null for adPlaceId = " + adPlaceId);
                    }
                } catch (Exception ex) {
                    Log.e(TAG, "loadBannerNativeAd error", ex);
                }
            }
        });
    }

    public NativeAdAdapter getNativeAdAdapter(String adPlaceId){
        NativeAdAdapter adAdapter = null;
        AdPlace adPlace = sAdPlaceMap.get(adPlaceId);
        if (adPlace != null) {
            String adCacheId = adPlace.getCacheGroupId();
            NativeAdCacheGroup cacheGroup = sNativeAdCacheGroupMap.get(adCacheId);
            if (cacheGroup != null && cacheGroup.isAdLoaded()) {
                adAdapter = cacheGroup.getAvailableAdapter(adPlaceId);
            }
        }
        return adAdapter;
    }

    private NativeAdViewContainer getNativeAdViewContainerFromCache(Activity activity, String adPlaceId)
    {
        if(activity==null || adPlaceId == null)
        {
            return null;
        }
        Map<String, NativeAdViewContainer> viewContainerMap = sNativeAdViewContainerMap.get(activity);
        if(viewContainerMap == null)
        {
            viewContainerMap = new HashMap<>();
            sNativeAdViewContainerMap.put(activity, viewContainerMap);
        }
        return viewContainerMap.get(adPlaceId);
    }

    private void putNativeAdViewContainerToCache(Activity activity, NativeAdViewContainer container, String adPlaceId)
    {
        Map<String, NativeAdViewContainer> viewContainerMap = sNativeAdViewContainerMap.get(activity);
        if(viewContainerMap == null)
        {
            viewContainerMap = new HashMap<>();
            sNativeAdViewContainerMap.put(activity, viewContainerMap);
        }
        viewContainerMap.put(adPlaceId, container);
    }

    public void removeNativeAdViewContainerCache(Activity activity)
    {
        sNativeAdViewContainerMap.remove(activity);
        if(sNativeAdActivity == activity)
        {
            sNativeAdActivity = null;
        }
    }

    private NativeAdViewContainer getNativeAdViewContainer(Activity activity, ViewGroup nativeAdRoot, String adPlaceId) {
        NativeAdViewContainer container = getNativeAdViewContainerFromCache(activity, adPlaceId);
        if(container == null){
            LayoutInflater inflater = activity.getLayoutInflater();
            Resources resources = activity.getResources();
            String defPackage = activity.getPackageName();
            String nativeAdLayout = sNativeAdLayoutMap.get(adPlaceId);
            if(nativeAdLayout!=null&& !nativeAdLayout.isEmpty())
            {
                int resId = resources.getIdentifier(nativeAdLayout, "layout", defPackage);
                if (resId != 0) {
                    View nativeAdView = inflater.inflate(resId, nativeAdRoot, false);
                    Log.d(TAG, "initNativeAdLayout nativeAdView = " + nativeAdView);
                    container = new NativeAdViewContainer(nativeAdView);
                    Log.d(TAG, "initNativeAdLayout nativeAdViewContainer = " + container);
                    nativeAdRoot.addView(nativeAdView);
                    putNativeAdViewContainerToCache(activity, container, adPlaceId);
                }
            }
        }

        return container;
    }

    public void showNativeAd(final Activity activity,final ViewGroup nativeAdRoot,final String adPlaceId) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, "showNativeAd adPlaceId = " + adPlaceId);
                    NativeAdViewContainer nativeAdViewContainer = getNativeAdViewContainer(activity, nativeAdRoot, adPlaceId);
                    if (nativeAdViewContainer == null) {
                        Log.e(TAG, "showNativeAd error, nativeAdViewContainer is null for place:" + adPlaceId);
                        return;
                    }
                    sNativeAdActivity = activity;
                    NativeAdAdapter currentAdAdapter = nativeAdViewContainer.getNativeAdAdapter();
//                    if (currentAdAdapter != null) {
                    nativeAdViewContainer.setVisibility(View.VISIBLE);
//                    }
                    nativeAdViewContainer.setCanShow(true);
                    nativeAdViewContainer.setNeedUpdate(true);
                    NativeAdAdapter adAdapter = getNativeAdAdapter(adPlaceId);
                    if (adAdapter != null) {
                        nativeAdViewContainer.updateNativeAd(adAdapter);
                    } else {
                        loadNativeAd(adPlaceId);
                    }
                } catch (Exception ex) {
                    Log.e(TAG, "showNativeAd error", ex);
                }
            }
        });
    }

    public void hideNativeAd(final Activity activity, final String adPlaceId) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, "hideNativeAd adPlaceId = " + adPlaceId);

                    NativeAdViewContainer nativeAdViewContainer = getNativeAdViewContainerFromCache(activity , adPlaceId);
                    if (nativeAdViewContainer == null) {
                        Log.e(TAG, "hideNativeAd error, nativeAdViewContainer is null for place:" + adPlaceId);
                        return;
                    }
                    nativeAdViewContainer.setCanShow(false);
                    nativeAdViewContainer.setVisibility(View.GONE);

                    loadNativeAd(adPlaceId);
                } catch (Exception ex) {
                    Log.e(TAG, "hideNativeAd error", ex);
                }
            }
        });
    }

    private void initAdCacheGroup(Context context, AdConfig adConfig) {
        Log.d(TAG, "sAdCacheMap = " + sAdCacheMap);
        for (AdCache adCache : sAdCacheMap.values()) {
            if (TYPE_REWARD_AD.equalsIgnoreCase(adCache.getType())) {
                RewardAdCacheGroup rewardAdCacheGroup = new RewardAdCacheGroup();
                rewardAdCacheGroup.init(context, adCache, adConfig, this);
                sRewardAdCacheGroupMap.put(adCache.getId(), rewardAdCacheGroup);
                if (adCache.isAutoLoad()) {
                    rewardAdCacheGroup.loadRewardedVideoAd("auto_load");
                }
            } else if (TYPE_INTERSTITIAL_AD.equalsIgnoreCase(adCache.getType())) {
                InterstitialAdCacheGroup cacheGroup = new InterstitialAdCacheGroup();
                cacheGroup.init(context, adCache, adConfig, this);
                sInterstitialAdCacheGroupMap.put(adCache.getId(), cacheGroup);
                if (adCache.isAutoLoad()) {
                    cacheGroup.loadAd("auto_load");
                }
            } else if (TYPE_NATIVE_AD.equalsIgnoreCase(adCache.getType())) {
                NativeAdCacheGroup cacheGroup = new NativeAdCacheGroup();
                cacheGroup.init(context, adCache, adConfig, this);
                Log.d(TAG, "load ad cache, id = " + adCache.getId());
                sNativeAdCacheGroupMap.put(adCache.getId(), cacheGroup);
                if (adCache.isAutoLoad()) {
                    cacheGroup.loadAd("auto_load");
                }
            }
        }
    }


    private void loadAdConfig(AdConfig adConfig) {
        String adKeySettingJsonStr = adConfig.getAdKeyConfigStr();
        Log.d(TAG, "loadAdConfig ,adKeySettingJsonStr = " + adKeySettingJsonStr);
        try {
            JSONArray jsonArray = new JSONArray(adKeySettingJsonStr);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String id = jsonObject.getString("id");
                String network = jsonObject.getString("network");
                String key = jsonObject.getString("key");
                AdKey adKey = new AdKey(id, network, key);
                sAdKeyMap.put(id, adKey);
            }
        } catch (Exception ex) {
            Log.e(TAG, "loadAdConfig error", ex);
        }

        String adCacheSettingJsonStr = adConfig.getAdGroupConfigStr();
        Log.d(TAG, "loadAdConfig , adCacheSettingJsonStr = " + adCacheSettingJsonStr);
        try {
            JSONArray jsonArray = new JSONArray(adCacheSettingJsonStr);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String id = jsonObject.getString("id");
                String keysStr = jsonObject.getString("keys");
                String isAutoLoadStr = jsonObject.getString("isAutoLoad");
                boolean isAutoLoad = "TRUE".equalsIgnoreCase(isAutoLoadStr);
                String type = jsonObject.getString("type");
                AdCache adCache = new AdCache(id, keysStr, isAutoLoad, type);
                sAdCacheMap.put(id, adCache);
            }
        } catch (Exception ex) {
            Log.e(TAG, "loadAdConfig 222 error", ex);
        }

        String adPlaceSettingJsonStr = adConfig.getAdPlaceConfigStr();
        Log.d(TAG, "loadAdConfig ,adPlaceSettingJsonStr = " + adPlaceSettingJsonStr);
        try {
            JSONArray jsonArray = new JSONArray(adPlaceSettingJsonStr);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String id = jsonObject.getString("id");
                String isEnabledStr = jsonObject.getString("isEnabled");
                String cacheGroup = jsonObject.getString("cacheGroup");
                String nativeAdLayout = jsonObject.getString("nativeAdLayout");
                boolean isEnabled = "TRUE".equalsIgnoreCase(isEnabledStr);
                AdPlace adPlace = new AdPlace(id, isEnabled, cacheGroup, nativeAdLayout);
                sAdPlaceMap.put(id, adPlace);
                sNativeAdLayoutMap.put(id, nativeAdLayout);
            }
        } catch (Exception ex) {
            Log.e(TAG, "loadAdConfig 333 error", ex);
        }
    }

    public void resume(Context context) {
        MobileAds.getRewardedVideoAdInstance(context.getApplicationContext()).resume(context);
    }

    public void pause(Context context) {
        MobileAds.getRewardedVideoAdInstance(context.getApplicationContext()).pause(context);
    }

    public  void destroy(Context context) {
        try {
            MobileAds.getRewardedVideoAdInstance(context).destroy(context);
            for (RewardAdCacheGroup group:sRewardAdCacheGroupMap.values())
            {
                group.destroy(context);
            }
            sRewardAdCacheGroupMap.clear();
            for (InterstitialAdCacheGroup group:sInterstitialAdCacheGroupMap.values())
            {
                group.destroy(context);
            }
            sInterstitialAdCacheGroupMap.clear();

            for (NativeAdCacheGroup group:sNativeAdCacheGroupMap.values())
            {
                group.destroy(context);
            }
            sNativeAdCacheGroupMap.clear();

            sAdCacheMap.clear();
            sAdKeyMap.clear();
            sAdPlaceMap.clear();
            sNativeAdViewContainerMap.clear();
            sNativeAdLayoutMap.clear();
            sUnityAdListenerMap.clear();

            isAdmobRewardAdLoaded = false;
            isAdmobRewardAdLoading = false;
        } catch (Exception ex) {
            Log.e(TAG, "onDestroy", ex);
        }
    }

    public AdKey getAdKey(String key) {
        return sAdKeyMap.get(key);
    }

    private IUnityAdsListener sUnityAdsListener = new IUnityAdsListener() {
        @Override
        public void onUnityAdsReady(String s) {
            Log.d(TAG, "sUnityAdsListener onRewardedVideoAdLoaded s = " + s);
            UnityAdListener listener = sUnityAdListenerMap.get(s);
            if(listener!=null)
            {
                listener.onUnityAdsReady();
            }
        }

        @Override
        public void onUnityAdsStart(String s) {
            Log.d(TAG, "sUnityAdsListener onUnityAdsStart s = " + s);
            UnityAdListener listener = sUnityAdListenerMap.get(s);
            if(listener!=null)
            {
                listener.onUnityAdsStart();
            }
        }

        @Override
        public void onUnityAdsFinish(String s, UnityAds.FinishState finishState) {
            Log.d(TAG, "sUnityAdsListener onUnityAdsFinish s = " + s + " finishState = " + finishState);
            UnityAdListener listener = sUnityAdListenerMap.get(s);
            if(listener!=null)
            {
                listener.onUnityAdsFinish(finishState);
            }
        }

        @Override
        public void onUnityAdsError(UnityAds.UnityAdsError unityAdsError, String s) {
            Log.d(TAG, "sUnityAdsListener onUnityAdsError s = " + s + " unityAdsError = " + unityAdsError);
            UnityAdListener listener = sUnityAdListenerMap.get(s);
            if(listener!=null)
            {
                listener.onUnityAdsError(unityAdsError);
            }
        }
    };

    public void addUnityAdListener(String adKey, UnityAdListener listener)
    {
        sUnityAdListenerMap.put(adKey, listener);
    }

    public boolean isNativeAdLoaded(String placeId)
    {
        boolean isLoaded = false;
        AdPlace adPlace = sAdPlaceMap.get(placeId);
        if(adPlace!=null)
        {
            NativeAdCacheGroup cacheGroup = sNativeAdCacheGroupMap.get(adPlace.getCacheGroupId());
            if(cacheGroup!=null)
            {
                isLoaded = cacheGroup.isAdLoaded();
            }
        }
        return isLoaded;
    }

    public boolean isInterstitialAdLoaded(String placeId)
    {
        boolean isLoaded = false;
        AdPlace adPlace = sAdPlaceMap.get(placeId);
        if(adPlace!=null)
        {
            InterstitialAdCacheGroup cacheGroup = sInterstitialAdCacheGroupMap.get(adPlace.getCacheGroupId());
            if(cacheGroup!=null)
            {
                isLoaded = cacheGroup.isAdLoaded();
            }
        }
        return isLoaded;
    }

    public boolean isRewardAdLoaded(String placeId)
    {
        boolean isLoaded = false;
        AdPlace adPlace = sAdPlaceMap.get(placeId);
        if(adPlace!=null)
        {
            RewardAdCacheGroup cacheGroup = sRewardAdCacheGroupMap.get(adPlace.getCacheGroupId());
            if(cacheGroup!=null)
            {
                isLoaded = cacheGroup.isAdLoaded();
            }
        }
        return isLoaded;
    }


    @Override
    public void onAdReward(String type, String placeId) {
        if(this.mAdsListener!=null)
        {
            this.mAdsListener.onAdReward(type, placeId);
        }
    }

    @Override
    public void onAdLoaded(String type, String placeId) {
        if(this.mAdsListener!=null)
        {
            this.mAdsListener.onAdLoaded(type, placeId);
        }
        if(TYPE_NATIVE_AD.equals(type))
        {
            this.onNativeAdLoaded(placeId);
        }
    }

    @Override
    public void onAdShowed(String type, String placeId) {
        if(this.mAdsListener!=null)
        {
            this.mAdsListener.onAdShowed(type, placeId);
        }
    }

    @Override
    public void onAdClosed(String type, String placeId) {
        if(this.mAdsListener!=null)
        {
            this.mAdsListener.onAdClosed(type, placeId);
        }
    }

    @Override
    public void onAdClicked(String type, String placeId) {
        if(this.mAdsListener!=null)
        {
            this.mAdsListener.onAdClicked(type, placeId);
        }
    }

    private void onNativeAdLoaded(final String placeId) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                NativeAdViewContainer container = getNativeAdViewContainerFromCache(sNativeAdActivity, placeId);
                if (container != null && container.isCanShow() && container.isNeedUpdate()) {
                    NativeAdAdapter adAdapter = getNativeAdAdapter(placeId);
                    if(adAdapter!=null) {
                        container.updateNativeAd(adAdapter);
                    }
                } else {
                    Log.e(TAG, "NativeAdViewContainer is null, placeId = " + placeId);
                }
            }
        });
    }

    public int getLoadAdTimeout()
    {
        return this.mAdConfig!=null&&this.mAdConfig.getLoadAdTimeout() > 6000 ? this.mAdConfig.getLoadAdTimeout() : 15000;
    }

    public boolean isAdmobRewardAdLoaded() {
        return isAdmobRewardAdLoaded;
    }

    public void setAdmobRewardAdLoaded(boolean admobRewardAdLoaded) {
        isAdmobRewardAdLoaded = admobRewardAdLoaded;
    }

    public boolean isAdmobRewardAdLoading() {
        return isAdmobRewardAdLoading;
    }

    public void setAdmobRewardAdLoading(boolean admobRewardAdLoading) {
        isAdmobRewardAdLoading = admobRewardAdLoading;
    }

    public void onDefaultNativeAdClicked()
    {
        if(this.mAdsListener != null){
            EventHelper.logEvent(EVENT_DEFAULT_AD_CLICKED,null);
            this.mAdsListener.onDefaultNativeAdClicked();
        }
    }
}

