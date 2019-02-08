package com.eyu.common.ad.adapter;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.eyu.common.ad.EyuAdManager;
import com.eyu.common.ad.model.AdKey;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;


/**
 * Description :
 * <p>
 * Creation    : 2018/2/27
 * Author      : luoweiqiang
 */
public class AdmobRewardAdAdapter extends RewardAdAdapter {

    private static final String TAG = "AdmobRewardAdAdapter";
    private RewardedVideoAd mRevivalAd;
    private String mAdId;
    private volatile boolean isLoaded;

    private RewardedVideoAdListener mRewardedVideoAdListener = new RewardedVideoAdListener() {
        @Override
        public void onRewardedVideoAdLoaded() {
            Log.d(TAG, "onRewardedVideoAdLoaded ");
            isLoading = false;
            isLoaded = true;
            cancelTimeoutTask();
            EyuAdManager.getInstance().setAdmobRewardAdLoaded(true);
            EyuAdManager.getInstance().setAdmobRewardAdLoading(false);
            notifyOnAdLoaded();
        }

        @Override
        public void onRewardedVideoAdOpened() {
            Log.d(TAG, "onRewardedVideoAdOpened ");
            notifyOnAdShowed();
        }

        @Override
        public void onRewardedVideoStarted() {
            Log.d(TAG, "onRewardedVideoStarted ");
        }

        @Override
        public void onRewardedVideoAdClosed() {
            Log.d(TAG, "loadAd ");
            notifyOnAdClosed();
        }

        @Override
        public void onRewarded(RewardItem rewardItem) {
            Log.d(TAG, "onRewarded ");
            notifyOnRewarded();
        }

        @Override
        public void onRewardedVideoAdLeftApplication() {
            Log.d(TAG, "loadAd ");
        }

        @Override
        public void onRewardedVideoAdFailedToLoad(int i) {
            Log.d(TAG, "onRewardedVideoAdFailedToLoad i = " + i);
            isLoading = false;
            cancelTimeoutTask();
            EyuAdManager.getInstance().setAdmobRewardAdLoading(false);
            notifyOnAdFailedLoad(i);
        }

        @Override
        public void onRewardedVideoCompleted() {
            Log.d(TAG, "onRewarded ");
        }
    };

    public AdmobRewardAdAdapter(Context context, AdKey adKey) {
        super(context, adKey);
        this.isLoading = false;
        this.mAdId = adKey.getKey();
        mRevivalAd = MobileAds.getRewardedVideoAdInstance(context.getApplicationContext());
        isLoaded = false;
    }

    @Override
    public void loadAd() {
        Log.d(TAG, "loadAd mRevivalAd.isLoaded() = " + mRevivalAd.isLoaded() + " this.isLoading = " + this.isLoading);
        try {
            if (isAdLoaded()) {
                notifyOnAdLoaded();
            }else if(EyuAdManager.getInstance().isAdmobRewardAdLoaded()) {
                Log.d(TAG, "loadAd one AdmobRewardAdAdapter was already loaded.");
                notifyOnAdFailedLoad(ERROR_OTHER_ADMOB_REWARD_AD_LOADED);
            }else if(EyuAdManager.getInstance().isAdmobRewardAdLoading()){
                Log.d(TAG, "loadAd one AdmobRewardAdAdapter is loading.");
                if(this.isLoading){
                    startTimeoutTask();
                }else{
                    notifyOnAdFailedLoad(ERROR_OTHER_ADMOB_REWARD_AD_LOADING);
                }
            } else if (!this.isLoading) {
                Log.d(TAG, "loadAd start11111");
                this.isLoading = true;
                mRevivalAd.setRewardedVideoAdListener(mRewardedVideoAdListener);
                mRevivalAd.loadAd(this.mAdId, new AdRequest.Builder().build());
                EyuAdManager.getInstance().setAdmobRewardAdLoading(true);
                startTimeoutTask();
            }else{
                startTimeoutTask();
            }
        } catch (Exception ex) {
            Log.e(TAG, "loadPlayAd error", ex);
        }
    }

    @Override
    public boolean showAd(Activity activity) {
        try {
            Log.d(TAG, "showAd isLoaded = " + isLoaded);
            if (isAdLoaded()) {
                isLoading = false;
                isLoaded = false;
                EyuAdManager.getInstance().setAdmobRewardAdLoaded(false);
                mRevivalAd.show();
                return true;
            }
        } catch (Exception ex) {
            Log.e(TAG, "showPlayAd error", ex);
        }
        return false;
    }

    @Override
    public boolean isAdLoaded() {
        Log.d(TAG, "isAdLoaded isLoaded = " + isLoaded);
        return isLoaded;
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}
