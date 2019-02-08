package com.eyu.common.ad.adapter;

import android.content.Context;

import com.eyu.common.ad.NativeAdViewContainer;
import com.eyu.common.ad.listener.NativeAdListener;
import com.eyu.common.ad.model.AdKey;


/**
 * Description :
 * <p>
 * Creation    : 2018/2/27
 * Author      : luoweiqiang
 */
public abstract class NativeAdAdapter extends BaseAdAdapter{
    private NativeAdListener mListener;

    public NativeAdAdapter(Context context, AdKey adKey) {
        super(context, adKey);
    }

    public abstract void loadAd();

    public void showAd(NativeAdViewContainer nativeAdViewContainer)
    {
    }

    public abstract boolean isAdLoaded();

    protected abstract void destroyAd();

    public AdKey getAdKey() {
        return mAdKey;
    }

    public void setListener(NativeAdListener listener) {
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

    @Override
    public void onLoadAdTimeout() {
        cancelTimeoutTask();
        notifyOnAdFailedLoad(ERROR_TIMEOUT);
    }
}
