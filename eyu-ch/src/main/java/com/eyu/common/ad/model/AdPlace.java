package com.eyu.common.ad.model;

/**
 * Description :
 * <p>
 * Creation    : 2018/3/20
 * Author      : luoweiqiang
 */
public class AdPlace {
    private String id;
    private boolean isEnabled;
    private String cacheGroupId;
    private String nativeAdLayout;

    public AdPlace(String id, boolean isEnabled, String cacheGroupId, String nativeAdLayout) {
        this.id = id;
        this.isEnabled = isEnabled;
        this.cacheGroupId = cacheGroupId;
        this.nativeAdLayout = nativeAdLayout.toLowerCase();
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public String getCacheGroupId() {
        return cacheGroupId;
    }

    public String getNativeAdLayout() {
        return nativeAdLayout;
    }

    @Override
    public String toString() {
        return "AdPlace{" +
                "id='" + id + '\'' +
                ", isEnabled=" + isEnabled +
                ", nativeAdLayout=" + nativeAdLayout +
                ", cacheGroupId='" + cacheGroupId + '\'' +
                '}';
    }
}
