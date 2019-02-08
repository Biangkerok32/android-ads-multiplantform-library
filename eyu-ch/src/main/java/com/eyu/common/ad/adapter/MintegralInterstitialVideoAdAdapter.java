package com.eyu.common.ad.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.util.Log;

import com.eyu.common.ad.model.AdKey;
import com.mintegral.msdk.interstitialvideo.out.InterstitialVideoListener;
import com.mintegral.msdk.interstitialvideo.out.MTGInterstitialVideoHandler;
import com.mintegral.msdk.videocommon.download.NetStateOnReceive;

public class MintegralInterstitialVideoAdAdapter extends InterstitialAdAdapter {

    private final static String TAG = "MTGInterstitialVideoAd";
    private volatile boolean isLoaded = false;
    private MTGInterstitialVideoHandler mMtgInterstitalVideoHandler;
    private NetStateOnReceive mNetStateOnReceive;

    public MintegralInterstitialVideoAdAdapter(Context context, AdKey adKey) {
        super(context, adKey);

        try {
            // Declare network status for downloading video
            if (mNetStateOnReceive == null) {
                mNetStateOnReceive = new NetStateOnReceive();
                IntentFilter filter = new IntentFilter();
                filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
                context.registerReceiver(mNetStateOnReceive, filter);
            }

            mMtgInterstitalVideoHandler = new MTGInterstitialVideoHandler((Activity) context, adKey.getKey());
            mMtgInterstitalVideoHandler.setInterstitialVideoListener(new InterstitialVideoListener() {
                @Override
                public void onLoadSuccess(String s) {
                    Log.d(TAG, "onLoadSuccess " + s);
                }

                @Override
                public void onVideoLoadSuccess(String s) {
                    Log.d(TAG, "onVideoLoadSuccess " + s);
                    isLoading = false;
                    isLoaded = true;
                    cancelTimeoutTask();
                    notifyOnAdLoaded();
                }

                @Override
                public void onVideoLoadFail(String s) {
                    Log.w(TAG, "onVideoLoadFail " + s);
                    isLoading = false;
                    cancelTimeoutTask();
                    notifyOnAdFailedLoad(ERROR_LOAD_MINTEGRAL_INTERSTITIAL_AD);
                }

                @Override
                public void onAdShow() {
                    notifyOnAdShowed();
                }

                @Override
                public void onAdClose(boolean b) {
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
            Log.e(TAG, "Init mMtgInterstitalVideoHandler error " + ex);
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
        if (mMtgInterstitalVideoHandler != null && !isAdLoading()) {
            mMtgInterstitalVideoHandler.load();
        }
    }

    @Override
    public boolean showAd(Activity activity) {
        Log.d(TAG, "showAd");
        if (mMtgInterstitalVideoHandler != null && isAdLoaded()) {
            isLoaded = false;
            mMtgInterstitalVideoHandler.show();
            return true;
        }
        return false;
    }

    @Override
    public boolean isAdLoaded() {
        return isLoaded;
    }

    @Override
    public void destroy() {
        super.destroy();
        try {
            if (mNetStateOnReceive != null && mContext != null) {
                mContext.unregisterReceiver(mNetStateOnReceive);
            }
        } catch (Exception ex) {
            Log.e(TAG, "destroy " + ex);
        }
    }
}
