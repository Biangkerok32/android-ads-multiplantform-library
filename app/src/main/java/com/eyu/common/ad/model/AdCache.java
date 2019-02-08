package com.eyu.common.ad.model;

import android.util.Log;

import org.json.JSONArray;

import java.util.ArrayList;

/**
 * Description :
 * <p>
 * Creation    : 2018/3/20
 * Author      : luoweiqiang
 */
public class AdCache {
    private static final String TAG = "AdCache";
    private String id;
    private ArrayList<String> keyIdArray = new ArrayList<>();
    private boolean isAutoLoad = false;
    private String type;

    public AdCache(String id, String keysJson, boolean isAutoLoad, String type) {
        this.id = id;
        this.isAutoLoad = isAutoLoad;
        this.type = type;
        try {
            JSONArray keysJsonArray = new JSONArray(keysJson);
            for (int i = 0; i < keysJsonArray.length(); i++) {
                String key = keysJsonArray.getString(i);
                keyIdArray.add(key);
            }
        } catch (Exception ex) {
            Log.e(TAG, "AdCache parse json error, keysJson = " + keysJson, ex);
        }
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "AdCache{" +
                "id='" + id + '\'' +
                ", keyIdArray=" + keyIdArray +
                ", isAutoLoad=" + isAutoLoad +
                ", type='" + type + '\'' +
                '}';
    }

    public ArrayList<String> getKeyIdArray() {
        return keyIdArray;
    }

    public boolean isAutoLoad() {
        return isAutoLoad;
    }

    public String getType() {
        return type;
    }
}
