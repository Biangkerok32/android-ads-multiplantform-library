package com.eyu.common.ad.model;

/**
 * Description :
 * <p>
 * Creation    : 2018/3/20
 * Author      : luoweiqiang
 */
public class AdKey {
    private String id;
    private String network;
    private String key;

    public AdKey(String id, String network, String key)
    {
        this.id = id;
        this.network = network;
        this.key = key;
    }

    public String getId() {
        return id;
    }

    public String getNetwork() {
        return network;
    }

    public String getKey() {
        return key;
    }

    @Override
    public String toString() {
        return "AdKey{" +
                "id='" + id + '\'' +
                ", network='" + network + '\'' +
                ", key='" + key + '\'' +
                '}';
    }
}
