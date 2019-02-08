package com.eyu.common.ad.listener;

import com.unity3d.ads.UnityAds;

/**
 * Description :
 * <p>
 * Creation    : 2018/6/26
 * Author      : luoweiqiang
 */
public interface UnityAdListener {
    public void onUnityAdsReady();

    public void onUnityAdsStart();

    public void onUnityAdsFinish(UnityAds.FinishState finishState);

    public void onUnityAdsError(UnityAds.UnityAdsError unityAdsError);
}
