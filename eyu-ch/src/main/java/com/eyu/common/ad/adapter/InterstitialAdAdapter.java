package com.eyu.common.ad.adapter;

import android.app.Activity;
import android.content.Context;

import com.eyu.common.ad.listener.InterstitialAdListener;
import com.eyu.common.ad.model.AdKey;

/**
 * Description :
 * <p>
 * Creation    : 2018/2/27
 * Author      : luoweiqiang
 */
public abstract class InterstitialAdAdapter extends BaseAdAdapter{

    private InterstitialAdListener mListener;
    public InterstitialAdAdapter(Context context, AdKey adKey)
    {
        super(context, adKey);
    }
    public abstract void loadAd();
    public abstract boolean showAd(Activity activity);
    public abstract boolean isAdLoaded();
    public AdKey getAdKey() {
        return mAdKey;
    }

    public void setListener(InterstitialAdListener listener)
    {
        this.mListener = listener;
    }

    protected void notifyOnAdFailedLoad(int errorCode) {
        if (mListener != null) {
            mListener.onAdFailedLoad(this, errorCode);
        }
    }

    protected void notifyOnAdLoaded() {
        if (mListener != null) {
            mListener.onAdLoaded(this);
        }
    }

    protected void notifyOnAdShowed() {
        if (mListener != null) {
            mListener.onAdShowed(this);
        }
    }

    protected void notifyOnAdClicked() {
        if (mListener != null) {
            mListener.onAdClicked(this);
        }
    }

    protected void notifyOnAdClosed() {
        if (mListener != null) {
            mListener.onAdClosed(this);
        }
    }

    @Override
    public void onLoadAdTimeout() {
        cancelTimeoutTask();
        notifyOnAdFailedLoad(ERROR_TIMEOUT);
    }
}
