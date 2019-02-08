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
public class UnityRewardAdAdapter extends RewardAdAdapter {
    private static final String TAG = "UnityRewardAdAdapter";
    private String mAdId;

    private UnityAdListener mUnityAdListener = new UnityAdListener() {
        @Override
        public void onUnityAdsReady() {
            Log.d(TAG, "onRewardedVideoAdLoaded");
            isLoading = false;
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
            if(finishState == UnityAds.FinishState.COMPLETED)
            {
                notifyOnRewarded();
            }
            notifyOnAdClosed();
        }

        @Override
        public void onUnityAdsError(UnityAds.UnityAdsError unityAdsError) {
            Log.d(TAG, "onUnityAdsError unityAdsError = " + unityAdsError);
            isLoading = false;
            cancelTimeoutTask();
            notifyOnAdFailedLoad(unityAdsError.ordinal());
        }
    };

    public UnityRewardAdAdapter(Context context, AdKey adKey) {
        super(context, adKey);
        this.isLoading = false;
        this.mAdId = adKey.getKey();
        EyuAdManager.getInstance().addUnityAdListener(mAdId, mUnityAdListener);
    }

    @Override
    public void loadAd() {
        Log.d(TAG, "loadAd isLoaded() = " + UnityAds.isReady(this.mAdId) + " this.isLoading = " + this.isLoading);
        try {
            if (UnityAds.isReady(this.mAdId)) {
                notifyOnAdLoaded();
            } else {
                Log.d(TAG, "loadAd start11111");
                notifyOnAdFailedLoad(ERROR_UNITY_AD_NOT_LOADED);
            }
        } catch (Exception ex) {
            Log.e(TAG, "loadPlayAd error", ex);
        }
    }

    @Override
    public boolean showAd(Activity activity) {
        try {
            Log.d(TAG, "showAd isLoaded = " + UnityAds.isReady(this.mAdId));
            if(UnityAds.isInitialized()){
                if (UnityAds.isReady(this.mAdId)) {
                    isLoading = false;
                    UnityAds.show(activity, this.mAdId);
                    return true;
                }
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
            Log.d(TAG, "isAdLoaded isLoaded = " + UnityAds.isReady(this.mAdId));
            return UnityAds.isInitialized() && UnityAds.isReady(this.mAdId);
        } catch (Exception ex) {
            Log.e(TAG, "isAdLoaded error", ex);
        }
        return false;
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}
