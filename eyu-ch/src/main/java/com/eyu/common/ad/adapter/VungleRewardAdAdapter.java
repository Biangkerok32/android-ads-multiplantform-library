package com.eyu.common.ad.adapter;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.eyu.common.ad.model.AdKey;
import com.vungle.warren.AdConfig;
import com.vungle.warren.LoadAdCallback;
import com.vungle.warren.PlayAdCallback;
import com.vungle.warren.Vungle;
import com.vungle.warren.error.VungleException;


/**
 * Description :
 * <p>
 * Creation    : 2018/2/27
 * Author      : luoweiqiang
 */
public class VungleRewardAdAdapter extends RewardAdAdapter {

    private static final String TAG = "VungleRewardAdAdapter";
    private String mAdId;
    private Handler mHandler;
    private DelayLoadAdTask mTask;
    private long mDelayTime = 5;

    private LoadAdCallback mLoadAdCallback = new LoadAdCallback() {
        @Override
        public void onAdLoad(String s) {
            if (mAdId.equals(s)) {
                isLoading = false;
                notifyOnAdLoaded();
            }
        }

        @Override
        public void onError(String s, Throwable throwable) {
            if (mAdId.equals(s)) {
                isLoading = false;
                notifyOnAdFailedLoad(13001);
            }
        }
    };
    private PlayAdCallback mPlayAdCallback = new PlayAdCallback() {
        @Override
        public void onAdStart(String s) {
            if (mAdId.equals(s)) {
                notifyOnAdShowed();
            }
        }

        @Override
        public void onAdEnd(String s, boolean completed, boolean isCTAClicked) {
            if (mAdId.equals(s)) {
                if (completed) {
                    notifyOnRewarded();
                }
                if (isCTAClicked) {
                    notifyOnAdClicked();
                }
                notifyOnAdClosed();
            }
        }

        @Override
        public void onError(String s, Throwable throwable) {
            notifyOnAdClosed();
            try {
                VungleException ex = (VungleException) throwable;

//                if (ex.getExceptionCode() == VungleException.VUNGLE_NOT_INTIALIZED) {
//                    AdPlayer.initializeVungleSDK();
//                }
            } catch (ClassCastException cex) {
                Log.d(TAG, cex.getMessage());
            }
        }
    };

    public VungleRewardAdAdapter(Context context, AdKey adKey) {
        super(context, adKey);
        this.mAdId = adKey.getKey();
        mHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void loadAd() {
        Log.d(TAG, "loadAd mRevivalAd.isLoaded() = " + isAdLoaded() + " this.isLoading = " + this.isLoading);
        try {
            if (Vungle.isInitialized()) {
                if (isAdLoaded()) {
                    notifyOnAdLoaded();
                } else if(!this.isLoading) {
                    Log.d(TAG, "loadAd start11111");
                    this.isLoading = true;
                    if (mTask != null) {
                        mHandler.removeCallbacks(mTask);
                        mTask = null;
                    }
                    this.mTask = new DelayLoadAdTask();
                    mHandler.postDelayed(this.mTask, this.mDelayTime);
                    startTimeoutTask();
                }else{
                    startTimeoutTask();
                }
            } else {
                notifyOnAdFailedLoad(13002);
            }
        } catch (Exception ex) {
            Log.e(TAG, "loadPlayAd error", ex);
        }
    }

    @Override
    public boolean showAd(Activity activity) {
        try {
            Log.d(TAG, "showAd ");
            if (isAdLoaded()) {
                this.isLoading = false;
                Vungle.playAd(mAdId, new AdConfig(), this.mPlayAdCallback);
                return true;
            }
        } catch (Exception ex) {
            Log.e(TAG, "showPlayAd error", ex);
        }
        return false;
    }

    @Override
    public boolean isAdLoaded() {
        try {
            if (Vungle.isInitialized()) {
                Log.d(TAG, "isAdLoaded isLoaded = " + Vungle.canPlayAd(mAdId));
                return Vungle.canPlayAd(mAdId);
            }
        } catch (Exception ex) {
            Log.e(TAG, "isAdLoaded error", ex);
        }
        return false;
    }

    @Override
    public void destroy() {
        super.destroy();
        if (mTask != null) {
            mHandler.removeCallbacks(mTask);
        }
    }

    class DelayLoadAdTask implements Runnable {
        @Override
        public void run() {
            if (mTask != null) {
                mHandler.removeCallbacks(mTask);
            }
            Vungle.loadAd(mAdId, mLoadAdCallback);
        }
    }
}
