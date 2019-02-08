package com.eyu.common.ad.adapter;

import android.app.Activity;
import android.content.Context;

import com.eyu.common.ad.listener.RewardAdListener;
import com.eyu.common.ad.model.AdKey;

/**
 * Description :
 * <p>
 * Creation    : 2018/2/27
 * Author      : luoweiqiang
 */
public abstract class RewardAdAdapter extends BaseAdAdapter{

    private RewardAdListener mListener;

    public RewardAdAdapter(Context context, AdKey adKey)
    {
        super(context, adKey);
    }
    public abstract void loadAd();
    public abstract boolean showAd(Activity activity);
    public abstract boolean isAdLoaded();

    public void setListener(RewardAdListener listener) {
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

    protected void notifyOnRewarded() {
        if (mListener != null) {
            mListener.onRewarded(this);
        }
    }

    @Override
    public void onLoadAdTimeout() {
        cancelTimeoutTask();
        notifyOnAdFailedLoad(ERROR_TIMEOUT);
    }
}
