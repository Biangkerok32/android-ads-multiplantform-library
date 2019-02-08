package com.eyu.common.ad;

public interface EyuAdsListener {
    void onAdReward(String type, String placeId);

    void onAdLoaded(String type, String placeId);

    void onAdShowed(String type, String placeId);

    void onAdClosed(String type, String placeId);

    void onAdClicked(String type, String placeId);

    void onDefaultNativeAdClicked();
}