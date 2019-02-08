package com.eyu.common.ad.listener;


import com.eyu.common.ad.adapter.RewardAdAdapter;

/**
 * Description :
 * <p>
 * Creation    : 2018/6/26
 * Author      : luoweiqiang
 */
public interface RewardAdListener {
    public void onAdLoaded(RewardAdAdapter adapter);

    public void onAdFailedLoad(RewardAdAdapter adapter, int errcode);

    public void onAdShowed(RewardAdAdapter adapter);

    public void onAdClicked(RewardAdAdapter adapter);

    public void onAdClosed(RewardAdAdapter adapter);

    public void onRewarded(RewardAdAdapter adapter);
}
