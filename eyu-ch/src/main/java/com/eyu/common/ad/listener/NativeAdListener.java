package com.eyu.common.ad.listener;


import com.eyu.common.ad.adapter.NativeAdAdapter;

/**
 * Description :
 * <p>
 * Creation    : 2018/6/26
 * Author      : luoweiqiang
 */
public interface NativeAdListener {
    public void onAdLoaded(NativeAdAdapter adapter);

    public void onAdFailedLoad(NativeAdAdapter adapter, int errcode);

    public void onAdShowed(NativeAdAdapter adapter);

    public void onAdClicked(NativeAdAdapter adapter);
}
