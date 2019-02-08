package com.eyu.common.ad.adapter;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTFullScreenVideoAd;
import com.eyu.common.ad.EyuAdManager;
import com.eyu.common.ad.model.AdKey;


public class WmInterstitialAdAdapter extends InterstitialAdAdapter {

    private static final String TAG = "WmInterstitialAdAdapter";
    private volatile boolean isLoaded;
    private TTAdNative mTTAdNative;
    private AdSlot mAdSlot;
    private TTAdNative.FullScreenVideoAdListener mFullScreenVideoAdListener;
    private TTFullScreenVideoAd mTTFullScreenVideoAd = null;


    public WmInterstitialAdAdapter(Context context, AdKey adKey) {
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

        mFullScreenVideoAdListener = new TTAdNative.FullScreenVideoAdListener() {
            @Override
            public void onError(int code, String message) {
                Log.d(TAG, "onError code: " + code + "  message: " + message);
                cancelTimeoutTask();
                isLoading = false;
                notifyOnAdFailedLoad(code);
            }

            @Override
            public void onFullScreenVideoAdLoad(TTFullScreenVideoAd ad) {
                Log.d(TAG, "onFullScreenVideoAdLoad");
                cancelTimeoutTask();

                mTTFullScreenVideoAd = ad;
                if(mTTFullScreenVideoAd == null) {
                    notifyOnAdFailedLoad(ERROR_LOAD_WM_INTERSTITIAL_AD);
                    return;
                }

                isLoaded = true;
                isLoading = false;
                notifyOnAdLoaded();

                mTTFullScreenVideoAd.setFullScreenVideoAdInteractionListener(new TTFullScreenVideoAd.FullScreenVideoAdInteractionListener() {

                    @Override
                    public void onAdShow() {
                        Log.d(TAG, "onAdShow");
                        notifyOnAdShowed();
                    }

                    @Override
                    public void onAdVideoBarClick() {
                        Log.d(TAG, "onAdVideoBarClick");
                        notifyOnAdClicked();
                    }

                    @Override
                    public void onAdClose() {
                        Log.d(TAG, "onAdClose");
                        notifyOnAdClosed();
                    }

                    @Override
                    public void onVideoComplete() {
                        Log.d(TAG, "onVideoComplete");
                    }

                    @Override
                    public void onSkippedVideo() {
                        Log.d(TAG, "onSkippedVideo");
                    }

                });
            }
            @Override
            public void onFullScreenVideoCached() {
                Log.d(TAG, "onFullScreenVideoCached");
            }
        };

    }

    @Override
    public void loadAd() {
        Log.d(TAG, "loadAd ");
        try {
            if (isAdLoaded()) {
                notifyOnAdLoaded();
                return;
            }
            startTimeoutTask();
            if(mTTAdNative != null && !isAdLoading()) {
                isLoading = true;
                mTTAdNative.loadFullScreenVideoAd(mAdSlot, mFullScreenVideoAdListener);
            }
        } catch (Exception ex) {
            Log.e(TAG, "loadPlayAd error", ex);
        }
    }

    @Override
    public boolean showAd(Activity activity) {
        Log.d(TAG, "showAd ");
        try {
            if (isAdLoaded()) {
                isLoading = false;
                isLoaded = false;
                if (mTTFullScreenVideoAd != null) {
                    mTTFullScreenVideoAd.showFullScreenVideoAd(activity);
                    mTTFullScreenVideoAd = null;
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
    public void destroy()
    {
        Log.d(TAG, "destroy ");
        super.destroy();
    }
}
