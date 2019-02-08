package com.eyu.common.ad.listener;

import com.eyu.common.ad.adapter.InterstitialAdAdapter;

/**
 * Description :
 * <p>
 * Creation    : 2018/6/26
 * Author      : luoweiqiang
 */
public interface InterstitialAdListener {
    public void onAdLoaded(InterstitialAdAdapter adapter);

    public void onAdFailedLoad(InterstitialAdAdapter adapter, int errcode);

    public void onAdShowed(InterstitialAdAdapter adapter);

    public void onAdClicked(InterstitialAdAdapter adapter);

    public void onAdClosed(InterstitialAdAdapter adapter);
}
