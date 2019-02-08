package com.eyu.common.ad.model;

public class AdConfig {
    private String adGroupConfigStr;
    private String adKeyConfigStr;
    private String adPlaceConfigStr;
    private int maxTryLoadInterstitialAd = 7;
    private int maxTryLoadRewardAd = 7;
    private int maxTryLoadNativeAd = 7;
    private String admobClientId;
    private String unityClientId;
    private String vungleClientId;
    private String ttClientId;
    private String gdtClientId;
    private int loadAdTimeout = 15000;
    private boolean reportEvent = false;
    private String mintegralAppId;
    private String mintegralAppKey;

    public String getAdGroupConfigStr() {
        return adGroupConfigStr;
    }

    public void setAdGroupConfigStr(String adGroupConfigStr) {
        this.adGroupConfigStr = adGroupConfigStr;
    }

    public String getAdKeyConfigStr() {
        return adKeyConfigStr;
    }

    public void setAdKeyConfigStr(String adKeyConfigStr) {
        this.adKeyConfigStr = adKeyConfigStr;
    }

    public String getAdPlaceConfigStr() {
        return adPlaceConfigStr;
    }

    public void setAdPlaceConfigStr(String adPlaceConfigStr) {
        this.adPlaceConfigStr = adPlaceConfigStr;
    }

    public int getMaxTryLoadInterstitialAd() {
        return maxTryLoadInterstitialAd;
    }

    public void setMaxTryLoadInterstitialAd(int maxTryLoadInterstitialAd) {
        this.maxTryLoadInterstitialAd = maxTryLoadInterstitialAd;
    }

    public int getMaxTryLoadRewardAd() {
        return maxTryLoadRewardAd;
    }

    public void setMaxTryLoadRewardAd(int maxTryLoadRewardAd) {
        this.maxTryLoadRewardAd = maxTryLoadRewardAd;
    }

    public int getMaxTryLoadNativeAd() {
        return maxTryLoadNativeAd;
    }

    public void setMaxTryLoadNativeAd(int maxTryLoadNativeAd) {
        this.maxTryLoadNativeAd = maxTryLoadNativeAd;
    }

    public String getAdmobClientId() {
        return admobClientId;
    }

    public void setAdmobClientId(String admobClientId) {
        this.admobClientId = admobClientId;
    }

    public String getUnityClientId() {
        return unityClientId;
    }

    public void setUnityClientId(String unityClientId) {
        this.unityClientId = unityClientId;
    }

    public String getVungleClientId() {
        return vungleClientId;
    }

    public void setVungleClientId(String vungleClientId) {
        this.vungleClientId = vungleClientId;
    }

    public String getTtClientId() {
        return ttClientId;
    }

    public void setTtClientId(String ttClientId) {
        this.ttClientId = ttClientId;
    }

    public String getGdtClientId() {
        return gdtClientId;
    }

    public void setGdtClientId(String gdtClientId) {
        this.gdtClientId = gdtClientId;
    }

    public int getLoadAdTimeout() {
        return loadAdTimeout;
    }

    public void setLoadAdTimeout(int loadAdTimeout) {
        this.loadAdTimeout = loadAdTimeout;
    }

    public void setReportEvent(boolean reportEvent){
        this.reportEvent = reportEvent;
    }

    public boolean getReportEvent(){
        return this.reportEvent;
    }

    public void setMintegralAppId(String mintegralAppId) {
        this.mintegralAppId = mintegralAppId;
    }

    public String getMintegralAppId() {
        return mintegralAppId;
    }

    public void setMintegralAppKey(String mintegralAppKey) {
        this.mintegralAppKey = mintegralAppKey;
    }

    public String getMintegralAppKey() {
        return mintegralAppKey;
    }
}
