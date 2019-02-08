package com.eyu.common.ad.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.util.Log;

import com.eyu.common.ad.model.AdKey;
import com.mintegral.msdk.out.MTGRewardVideoHandler;
import com.mintegral.msdk.out.RewardVideoListener;
import com.mintegral.msdk.videocommon.download.NetStateOnReceive;

public class MintegralRewardAdAdapter extends RewardAdAdapter {

    private final static String TAG = "MintegralRewardAd";
    private MTGRewardVideoHandler mMTGRewardVideoHandler;
    private NetStateOnReceive mNetStateOnReceive;

    public MintegralRewardAdAdapter(Context context, AdKey adKey) {
        super(context, adKey);
        try {
            // Declare network status for downloading video
            if (mNetStateOnReceive == null) {
                mNetStateOnReceive = new NetStateOnReceive();
                IntentFilter filter = new IntentFilter();
                filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
                context.registerReceiver(mNetStateOnReceive, filter);
            }

            mMTGRewardVideoHandler = new MTGRewardVideoHandler((Activity) mContext, adKey.getKey());
            mMTGRewardVideoHandler.setRewardVideoListener(new RewardVideoListener() {
                @Override
                public void onVideoLoadSuccess(String s) {
                    Log.d(TAG, "onVideoLoadSuccess " + s);
                    cancelTimeoutTask();
                    isLoading = false;
                    notifyOnAdLoaded();
                }

                @Override
                public void onLoadSuccess(String s) {
                    Log.d(TAG, "onLoadSuccess " + s);
                }

                @Override
                public void onVideoLoadFail(String s) {
                    cancelTimeoutTask();
                    Log.w(TAG, "onVideoLoadFail " + s);
                    isLoading = false;
                    notifyOnAdFailedLoad(ERROR_LOAD_MINTEGRAL_REWARD_AD);
                }

                @Override
                public void onAdShow() {
                    notifyOnAdShowed();
                }

                @Override
                public void onAdClose(boolean b, String s, float v) {
                    if (b) {
                        notifyOnRewarded();
                    }
                    notifyOnAdClosed();
                }

                @Override
                public void onShowFail(String s) {
                    Log.w(TAG, "onShowFail " + s);
                }

                @Override
                public void onVideoAdClicked(String s) {
                    notifyOnAdClicked();
                }
            });
        } catch (Exception ex) {
            Log.e(TAG, "Init MintegralRewardAdAdapter error " + ex);
        }
    }
    @Override
    public void loadAd() {
        Log.d(TAG, "loadAd");
        if (isAdLoaded()) {
            notifyOnAdLoaded();
            return;
        }
        startTimeoutTask();
        if (mMTGRewardVideoHandler != null && !isAdLoading()) {
            isLoading = true;
            mMTGRewardVideoHandler.load();
        }
    }

    @Override
    public boolean showAd(Activity activity) {
        Log.d(TAG, "showAd");
        if (isAdLoaded()) {
            mMTGRewardVideoHandler.show("");
        }
        return false;
    }

    @Override
    public boolean isAdLoaded() {
        if (mMTGRewardVideoHandler != null) {
            Log.d(TAG, "isAdLoaded " + mMTGRewardVideoHandler.isReady());
            return mMTGRewardVideoHandler.isReady();
        }
        return false;
    }

    @Override
    public void destroy() {
        super.destroy();
        try {
            if (mNetStateOnReceive != null) {
                mContext.unregisterReceiver(mNetStateOnReceive);
            }
        } catch (Exception ex) {
            Log.e(TAG, "destroy error " + ex);
        }
    }
}
