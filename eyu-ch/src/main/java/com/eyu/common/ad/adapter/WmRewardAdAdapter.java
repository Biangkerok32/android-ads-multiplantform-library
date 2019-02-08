package com.eyu.common.ad.adapter;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTRewardVideoAd;
import com.eyu.common.ad.EyuAdManager;
import com.eyu.common.ad.model.AdKey;

public class WmRewardAdAdapter extends RewardAdAdapter {

    private static final String TAG = "WmRewardAdAdapter";
    private volatile boolean isLoaded;
    private TTAdNative mTTAdNative;
    private AdSlot mAdSlot;
    private TTRewardVideoAd mttRewardVideoAd = null;
    private TTAdNative.RewardVideoAdListener mRewardVideoAdListener;

    public WmRewardAdAdapter(Context context, AdKey adKey) {
        super(context, adKey);
        isLoading = false;
        isLoaded = false;

        TTAdManager ttAdManager = EyuAdManager.getInstance().getTtAdManager();
        mTTAdNative = ttAdManager.createAdNative(context);

        mAdSlot = new AdSlot.Builder()
                .setCodeId(adKey.getKey())
                .setSupportDeepLink(true)
                .setImageAcceptedSize(1080, 1920)
                .setOrientation(TTAdConstant.VERTICAL)
                .build();

        mRewardVideoAdListener = new TTAdNative.RewardVideoAdListener() {
            @Override
            public void onError(int code, String message) {
                Log.d(TAG, "onError code: " + code + "  message: " + message);
                cancelTimeoutTask();
                notifyOnAdFailedLoad(code);
            }

            @Override
            public void onRewardVideoCached() {
                Log.d(TAG, "rewardVideoAd video cached");
            }

            @Override
            public void onRewardVideoAdLoad(TTRewardVideoAd ad) {
                Log.d(TAG, "rewardVideoAd loaded");
                cancelTimeoutTask();
                mttRewardVideoAd = ad;
                if(mttRewardVideoAd == null) {
                    notifyOnAdFailedLoad(ERROR_LOAD_WM_REWARD_AD);
                    return;
                }

                isLoading = false;
                isLoaded = true;
                notifyOnAdLoaded();

                mttRewardVideoAd.setRewardAdInteractionListener(new TTRewardVideoAd.RewardAdInteractionListener() {

                    @Override
                    public void onAdShow() {
                        Log.d(TAG, "rewardVideoAd show");
                        notifyOnAdShowed();
                    }

                    @Override
                    public void onAdVideoBarClick() {
                        Log.d(TAG, "rewardVideoAd bar click");
                    }

                    @Override
                    public void onAdClose() {
                        Log.d(TAG, "rewardVideoAd close");
                        notifyOnAdClosed();
                    }

                    @Override
                    public void onVideoComplete() {
                        Log.d(TAG, "rewardVideoAd complete");
                        notifyOnRewarded();
                    }

                    @Override
                    public void onRewardVerify(boolean rewardVerify, int rewardAmount, String rewardName) {
                        Log.d(TAG, "verify:"+rewardVerify+" amount:"+rewardAmount+" name:"+rewardName);
                    }

                    @Override
                    public void onVideoError() {
                        Log.d(TAG, "onVideoError");
                    }
                });
            }
        };

    }

    @Override
    public void loadAd() {
        Log.d(TAG, "loadAd mRevivalAd.isLoaded() = this.isLoading = " + this.isLoading);
        try {
            if (isAdLoaded()) {
                notifyOnAdLoaded();
                return;
            }
            startTimeoutTask();
            if(mTTAdNative != null && !isAdLoading()) {
                isLoading = true;
                mTTAdNative.loadRewardVideoAd(mAdSlot, mRewardVideoAdListener);
            }
        } catch (Exception ex) {
            Log.e(TAG, "loadPlayAd error", ex);
        }
    }

    @Override
    public boolean showAd(Activity activity) {
        Log.d(TAG, "showAd");
        try {
            if (isAdLoaded()) {
                isLoading = false;
                isLoaded = false;
                if(mttRewardVideoAd != null) {
                    mttRewardVideoAd.showRewardVideoAd(activity);
                }
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
