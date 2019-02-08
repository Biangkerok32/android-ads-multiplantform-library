package com.eyu.common.ad.adapter;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.eyu.common.R;
import com.eyu.common.ad.EyuAdManager;
import com.eyu.common.ad.model.AdConfig;
import com.eyu.common.ad.model.AdKey;
import com.eyu.common.firebase.EyuRemoteConfigHelper;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.RewardedVideoAd;
import com.facebook.ads.RewardedVideoAdListener;


/**
 * Description :
 * <p>
 * Creation    : 2018/2/27
 * Author      : luoweiqiang
 */
public class FbRewardAdAdapter extends RewardAdAdapter {

    private static final String TAG = "FbRewardAdAdapter";
    private RewardedVideoAd mRewardAd;
    private String mAdId;
    private RewardedVideoAdListener mRewardedVideoAdListener = new RewardedVideoAdListener() {
        @Override
        public void onError(Ad ad, AdError adError) {
            Log.e(TAG, "initRewardAd error msg = " + adError.getErrorMessage());
            isLoading = false;
            cancelTimeoutTask();
            notifyOnAdFailedLoad(adError.getErrorCode());
        }

        @Override
        public void onAdLoaded(Ad ad) {
            Log.d(TAG, "initRewardAd onAdLoaded");
            isLoading = false;
            cancelTimeoutTask();
            notifyOnAdLoaded();
        }

        @Override
        public void onAdClicked(Ad ad) {
            notifyOnAdClicked();
        }

        @Override
        public void onRewardedVideoCompleted() {
            notifyOnRewarded();
        }

        @Override
        public void onLoggingImpression(Ad ad) {
            notifyOnAdShowed();
        }

        @Override
        public void onRewardedVideoClosed() {
            notifyOnAdClosed();
        }
    };

    public FbRewardAdAdapter(Context context, AdKey adKey) {
        super(context, adKey);
        this.isLoading = false;
        this.mAdId = adKey.getKey();
        this.mRewardAd = new RewardedVideoAd(context, this.mAdId);
        this.mRewardAd.setAdListener(this.mRewardedVideoAdListener);
    }

    @Override
    public boolean isAdLoaded() {
        try {
            return mRewardAd.isAdLoaded();
        } catch (Exception ex) {
            Log.e(TAG, "loadPlayAd error", ex);
        }
        return false;
    }

    @Override
    public void loadAd() {
        try {
            Log.d(TAG, "loadAd isLoaded = " + mRewardAd.isAdLoaded());
            if (mRewardAd.isAdLoaded()) {
                notifyOnAdLoaded();
            } else {
                if (!this.isLoading) {
                    Log.d(TAG, "loadAd isLoaded11111111");
                    this.isLoading = true;
                    mRewardAd.loadAd();
                }
                startTimeoutTask();
            }
        } catch (Exception ex) {
            Log.e(TAG, "loadPlayAd error", ex);
        }
    }

    @Override
    public boolean showAd(Activity activity) {
        try {
            Log.d(TAG, "showAd isLoaded = " + mRewardAd.isAdLoaded());
            if (mRewardAd.isAdLoaded()) {
                isLoading = false;
                mRewardAd.show();
                return true;
            }
        } catch (Exception ex) {
            Log.e(TAG, "showPlayAd error", ex);
        }
        return false;
    }

    @Override
    public void destroy() {
        super.destroy();
        if (mRewardAd != null) {
            mRewardAd.destroy();
            mRewardAd = null;
        }
        isLoading = false;
    }
}
