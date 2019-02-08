package com.eyu.common.ad.adapter;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.eyu.common.ad.model.AdKey;
import com.qq.e.ads.rewardvideo.RewardVideoAD;
import com.qq.e.ads.rewardvideo.RewardVideoADListener;
import com.qq.e.comm.constants.Constants;
import com.qq.e.comm.util.AdError;

public class GdtRewardAdAdapter extends RewardAdAdapter {
    private static final String TAG = "GdtRewardAdAdapter";
    private String mAdId;
    private RewardVideoAD mRewardVideoAD = null;


    public GdtRewardAdAdapter(Context context, AdKey adKey) {
        super(context, adKey);
        this.isLoading = false;
        this.mAdId = adKey.getKey();

        mRewardVideoAD = new RewardVideoAD(context, "1105941321", this.mAdId, new RewardVideoADListener() {
            @Override
            public void onADLoad() {
                Log.d(TAG, "onAdLoaded");
                isLoading = false;
                cancelTimeoutTask();
                notifyOnAdLoaded();
            }

            @Override
            public void onVideoCached() {
                Log.d(TAG, "onVideoCached");
                notifyOnAdShowed();
            }

            @Override
            public void onADShow() {
                Log.d(TAG, "onADShow");
            }

            @Override
            public void onADExpose() {
                Log.d(TAG, "onADExpose");
            }

            @Override
            public void onReward() {
                Log.d(TAG, "onReward");
                notifyOnRewarded();
            }

            @Override
            public void onADClick() {
                Log.d(TAG, "onADClick");
                notifyOnAdClicked();
            }

            @Override
            public void onVideoComplete() {
                Log.d(TAG, "onVideoComplete");
//                notifyOnRewarded();
            }

            @Override
            public void onADClose() {
                Log.d(TAG, "onADClose");
                notifyOnAdClosed();
            }

            @Override
            public void onError(AdError error) {
                Log.e(TAG, "error msg = " + error.getErrorMsg());
                isLoading = false;
                cancelTimeoutTask();
                notifyOnAdFailedLoad(error.getErrorCode());

            }
        });
    }

    @Override
    public boolean isAdLoaded() {
        try {
        } catch (Exception ex) {
            Log.e(TAG, "loadPlayAd error", ex);
        }
        return false;
    }

    @Override
    public void loadAd() {
        try {
            if (isAdLoaded()) {
                notifyOnAdLoaded();
            } else {
                if (mRewardVideoAD != null) {
                    mRewardVideoAD.loadAD();
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
            Log.d(TAG, "showAd showAd");
            if (isAdLoaded()) {
                isLoading = false;
                if(mRewardVideoAD != null) {
                    mRewardVideoAD.showAD();
                }
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
        isLoading = false;
    }
}
