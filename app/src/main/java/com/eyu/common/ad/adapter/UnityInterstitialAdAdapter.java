package com.eyu.common.ad.adapter;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.eyu.common.ad.EyuAdManager;
import com.eyu.common.ad.listener.UnityAdListener;
import com.eyu.common.ad.model.AdKey;
import com.unity3d.ads.UnityAds;


/**
 * Description :
 * <p>
 * Creation    : 2018/2/27
 * Author      : luoweiqiang
 */
public class UnityInterstitialAdAdapter extends InterstitialAdAdapter {
    private static final String TAG = "UnityInterAdAdapter";
    private String mAdId;

    private UnityAdListener mUnityAdListener = new UnityAdListener() {
        @Override
        public void onUnityAdsReady() {
            Log.d(TAG, "onRewardedVideoAdLoaded");
            cancelTimeoutTask();
            notifyOnAdLoaded();
        }

        @Override
        public void onUnityAdsStart() {
            Log.d(TAG, "onUnityAdsStart");
            notifyOnAdShowed();
        }

        @Override
        public void onUnityAdsFinish(UnityAds.FinishState finishState) {
            Log.d(TAG, "onUnityAdsFinish finishState = " + finishState);
            notifyOnAdClosed();
        }

        @Override
        public void onUnityAdsError(UnityAds.UnityAdsError unityAdsError) {
            Log.d(TAG, "onUnityAdsError unityAdsError = " + unityAdsError);
            cancelTimeoutTask();
            notifyOnAdFailedLoad(unityAdsError.ordinal());
        }
    };

    public UnityInterstitialAdAdapter(Context context, AdKey adKey) {
        super(context, adKey);
        this.mAdId = adKey.getKey();
        EyuAdManager.getInstance().addUnityAdListener(mAdId, mUnityAdListener);
    }

    @Override
    public void loadAd() {
        Log.d(TAG, "loadAd isLoaded() = " + UnityAds.isReady(this.mAdId));
        try {
            if (UnityAds.isReady(this.mAdId)) {
                notifyOnAdLoaded();
            } else {
                Log.d(TAG, "loadAd error ERROR_UNITY_AD_NOT_LOADED");
                notifyOnAdFailedLoad(ERROR_UNITY_AD_NOT_LOADED);
            }
        } catch (Exception ex) {
                Log.e(TAG, "loadPlayAd error", ex);
        }
    }

    @Override
    public boolean showAd(Activity activity) {
        try {
            if(isAdLoaded()){
                UnityAds.show(activity, this.mAdId);
                return true;
            }
        } catch (Exception ex) {
            loadAd();
            Log.e(TAG, "showPlayAd error", ex);
        }
        return false;
    }

    @Override
    public boolean isAdLoaded() {
        try {
            boolean isAdLoaded = (UnityAds.isInitialized() && UnityAds.isReady(this.mAdId));
            Log.d(TAG, "isAdLoaded isLoaded = " + isAdLoaded);
            return isAdLoaded;
        } catch (Exception ex) {
            Log.e(TAG, "isAdLoaded error", ex);
        }
        return false;
    }

    @Override
    public void destroy()
    {
        Log.d(TAG, "destroy ");
        super.destroy();
    }
}
