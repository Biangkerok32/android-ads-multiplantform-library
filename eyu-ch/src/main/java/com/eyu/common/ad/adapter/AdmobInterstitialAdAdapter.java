package com.eyu.common.ad.adapter;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.eyu.common.ad.model.AdKey;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;


/**
 * Description :
 * <p>
 * Creation    : 2018/2/27
 * Author      : luoweiqiang
 */
public class AdmobInterstitialAdAdapter extends InterstitialAdAdapter {

    private static final String TAG = "AdmobIntAdAdapter";
    private InterstitialAd mPlayAd;
    private volatile boolean isLoaded;


    public AdmobInterstitialAdAdapter(Context context, AdKey adKey) {
        super(context, adKey);
        isLoaded = false;
        mPlayAd = new InterstitialAd(context.getApplicationContext());
        mPlayAd.setAdUnitId(adKey.getKey());
        mPlayAd.setAdListener(new com.google.android.gms.ads.AdListener() {
            @Override
            public void onAdLoaded() {
                isLoaded = true;
                isLoading = false;
                cancelTimeoutTask();
                notifyOnAdLoaded();
            }

            @Override
            public void onAdFailedToLoad(int i) {
                isLoading = false;
                cancelTimeoutTask();
                notifyOnAdFailedLoad(i);
            }

            @Override
            public void onAdClicked() {
                notifyOnAdClicked();
            }

            @Override
            public void onAdOpened() {
                notifyOnAdShowed();
            }

            @Override
            public void onAdClosed() {
                notifyOnAdClosed();
            }
        });
    }

    @Override
    public void loadAd() {
        try {
            if (isAdLoaded()) {
                notifyOnAdLoaded();
            } else if (!mPlayAd.isLoading()) {
                Log.d(TAG, "loadAd ");
                isLoading = true;
                mPlayAd.loadAd(new AdRequest.Builder().build());
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
            if (isAdLoaded()) {
                Log.d(TAG, "showAd ");
                isLoaded = false;
                mPlayAd.show();
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
