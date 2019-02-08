package com.eyu.common.ad.adapter;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.eyu.common.ad.model.AdKey;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.InterstitialAdListener;

/**
 * Description :
 * <p>
 * Creation    : 2018/2/27
 * Author      : luoweiqiang
 */
public class FbInterstitialAdAdapter extends InterstitialAdAdapter {

    private static final String TAG = "FbInterAdAdapter";
    private InterstitialAd mPlayAd;

    public FbInterstitialAdAdapter(Context context, AdKey adKey) {
        super(context,adKey);
        mPlayAd = new com.facebook.ads.InterstitialAd(context.getApplicationContext(), adKey.getKey());
            mPlayAd.setAdListener(new InterstitialAdListener() {
                @Override
                public void onError(Ad ad, AdError adError) {
                    isLoading = false;
                    cancelTimeoutTask();
                    notifyOnAdFailedLoad(adError.getErrorCode());
                }

                @Override
                public void onAdLoaded(Ad ad) {
                    isLoading = false;
                    cancelTimeoutTask();
                    notifyOnAdLoaded();
                }

                @Override
                public void onAdClicked(Ad ad) {
                    notifyOnAdClicked();
                }

                @Override
                public void onLoggingImpression(Ad ad) {
                    notifyOnAdShowed();
                }

                @Override
                public void onInterstitialDisplayed(Ad ad) {

                }

                @Override
                public void onInterstitialDismissed(Ad ad) {
                    notifyOnAdClosed();
                }
            });
    }

    @Override
    public void loadAd() {
        try {
            if (mPlayAd.isAdLoaded()) {
                notifyOnAdLoaded();
            } else if(!isLoading){
                isLoading = true;
                mPlayAd.loadAd();
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
            if (mPlayAd.isAdLoaded()) {
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
        return mPlayAd.isAdLoaded();
    }

    @Override
    public void destroy()
    {
        super.destroy();
        Log.d(TAG, "destroy ");
        mPlayAd.destroy();
        mPlayAd = null;
    }

}
