package com.eyu.common.ad.group;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;


import com.eyu.common.ad.EyuAdManager;
import com.eyu.common.ad.EyuAdsListener;
import com.eyu.common.ad.adapter.AdmobInterstitialAdAdapter;
import com.eyu.common.ad.adapter.FbInterstitialAdAdapter;
import com.eyu.common.ad.adapter.InterstitialAdAdapter;
import com.eyu.common.ad.adapter.MintegralInterstitialVideoAdAdapter;
import com.eyu.common.ad.adapter.WmInterstitialAdAdapter;
import com.eyu.common.ad.adapter.UnityInterstitialAdAdapter;
import com.eyu.common.ad.adapter.VungleInterstitialAdAdapter;
import com.eyu.common.ad.listener.InterstitialAdListener;
import com.eyu.common.ad.model.AdCache;
import com.eyu.common.ad.model.AdConfig;
import com.eyu.common.ad.model.AdKey;
import com.eyu.common.firebase.EventHelper;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Description :
 * <p>
 * Creation    : 2018/7/9
 * Author      : luoweiqiang
 */
public class InterstitialAdCacheGroup {

    private static final String TAG = "InterAdCacheGroup";
    private HashMap<String, Class<? extends InterstitialAdAdapter>> mAdapterClassMap = new HashMap<>();
    private ArrayList<InterstitialAdAdapter> mAdapterList = new ArrayList<>();
    private int mTryLoadAdCounter = 0;
    private long mMaxTryLoadAd = 7;
    private int mCurLoadingIndex = -1;
    private Context mContext = null;
    private AdCache mAdCache = null;
    private String mAdPlaceId = null;
    private EyuAdsListener mAdsListener;
    private Handler mHandler;
    private boolean mReportEvent = false;

    public void init(Context context, AdCache adCache, AdConfig adConfig, EyuAdsListener adsListener) {
        this.mContext = context;
        this.mAdCache = adCache;
        this.mAdsListener = adsListener;
        mHandler = new Handler(Looper.getMainLooper());
        mMaxTryLoadAd = adConfig.getMaxTryLoadInterstitialAd();
        mReportEvent = adConfig.getReportEvent();
        mAdapterClassMap.put(EyuAdManager.NETWORK_FACEBOOK, FbInterstitialAdAdapter.class);
        mAdapterClassMap.put(EyuAdManager.NETWORK_ADMOB, AdmobInterstitialAdAdapter.class);
        mAdapterClassMap.put(EyuAdManager.NETWORK_UNITY, UnityInterstitialAdAdapter.class);
        mAdapterClassMap.put(EyuAdManager.NETWORK_VUNGLE, VungleInterstitialAdAdapter.class);
//        mAdapterClassMap.put(EyuAdManager.NETWORK_APPLOVIN, AppLovinInterstitialAdAdapter.class);
        mAdapterClassMap.put(EyuAdManager.NETWORK_WM, WmInterstitialAdAdapter.class);
        mAdapterClassMap.put(EyuAdManager.NETWORK_MINTEGRAL, MintegralInterstitialVideoAdAdapter.class);
        initAdapterList();
    }

    private void initAdapterList() {
        List<String> keyIdList = mAdCache.getKeyIdArray();
        List<String> keyRemoveList = new ArrayList<>();
        for (String adId : keyIdList) {
            AdKey adKey = EyuAdManager.getInstance().getAdKey(adId);
            InterstitialAdAdapter adapter = getAdapter(adKey);
            if (adapter != null) {
                mAdapterList.add(adapter);
            } else {
                keyRemoveList.add(adId);
            }
        }
        keyIdList.removeAll(keyRemoveList);
        keyRemoveList.clear();
    }

    private InterstitialAdAdapter getAdapter(AdKey adKey) {
        Log.d(TAG, "getAdapter adCache = " + adKey);
        InterstitialAdAdapter adapter = null;
        if (adKey != null) {
            Class<? extends InterstitialAdAdapter> adClass = mAdapterClassMap.get(adKey.getNetwork());
            if (adClass != null) {
                try {
                    Class[] cl = {Context.class, AdKey.class};
                    Constructor cons = adClass.getDeclaredConstructor(cl);
                    adapter = (InterstitialAdAdapter) cons.newInstance(mContext, adKey);
                    adapter.setListener(new InterstitialAdListener() {

                        @Override
                        public void onAdLoaded(InterstitialAdAdapter adapter) {
                            InterstitialAdCacheGroup.this.onAdLoaded(adapter);
                        }

                        @Override
                        public void onAdFailedLoad(InterstitialAdAdapter adapter, int errcode) {
                            InterstitialAdCacheGroup.this.onAdFailedToLoad(adapter, errcode);
                        }

                        @Override
                        public void onAdShowed(InterstitialAdAdapter adapter) {
                            InterstitialAdCacheGroup.this.onAdShowed(adapter);
                        }

                        @Override
                        public void onAdClicked(InterstitialAdAdapter adapter) {
                            InterstitialAdCacheGroup.this.onAdClicked(adapter);
                        }

                        @Override
                        public void onAdClosed(InterstitialAdAdapter adapter) {
                            InterstitialAdCacheGroup.this.onAdClosed(adapter);
                        }
                    });
                } catch (Exception ex) {
                    Log.e(TAG, "getAdapter error", ex);
                }
            }
        }
        Log.d(TAG, "getAdapter adapter = " + adapter);
        return adapter;
    }

    public void loadAd(String adPlaceId) {
        Log.d(TAG, "loadAd mCurLoadingIndex = " + mCurLoadingIndex + " adPlaceId" + adPlaceId);
        this.mAdPlaceId = adPlaceId;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mAdapterList.size() <= 0) return;//配置错误
                if (mCurLoadingIndex == 0) return;//正在加载
                mCurLoadingIndex = 0;
                mTryLoadAdCounter = 1;
                mAdapterList.get(0).loadAd();
                if(mReportEvent) {
                    EventHelper.logEvent(mAdCache.getId() + EyuAdManager.EVENT_LOADING, "{ \"type\":\"" + mAdapterList.get(0).getAdKey().getId() + "\"}");
                }
            }
        });
    }

    public boolean isAdLoaded() {
        for (InterstitialAdAdapter adapter : mAdapterList) {
            if (adapter.isAdLoaded()) {
                return true;
            }
        }
        return false;
    }

    public void showAd(final Activity activity,final String adPlaceId) {
        Log.d(TAG, "showAd");
        this.mAdPlaceId = adPlaceId;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "showAd " + " adCache = " + mAdCache + " mCurLoadingIndex = " + mCurLoadingIndex);
                for (InterstitialAdAdapter adapter : mAdapterList) {
                    if (adapter.isAdLoaded()) {
                        adapter.showAd(activity);
                        break;
                    }
                }
            }
        });
    }

    public void destroy(Context context) {
        try {
            for (InterstitialAdAdapter adAdapter : mAdapterList) {
                adAdapter.destroy();
            }
        } catch (Exception ex) {
            Log.e(TAG, "onDestroy error ", ex);
        }
    }

    private void onAdLoaded(final InterstitialAdAdapter adAdapter) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onAdLoaded mCurLoadingIndex = " + mCurLoadingIndex);
                if (mCurLoadingIndex >= 0 && mAdapterList.get(mCurLoadingIndex) == adAdapter) {
                    mCurLoadingIndex = -1;
                }
                if(mReportEvent) {
                    EventHelper.logEvent(mAdCache.getId() + EyuAdManager.EVENT_LOAD_SUCCESS, "{  \"type\":\"" + adAdapter.getAdKey().getId() + "\"}");
                }
                if (mAdsListener != null) {
                    mAdsListener.onAdLoaded(EyuAdManager.TYPE_INTERSTITIAL_AD, mAdPlaceId);
                }
            }
        });
    }

    private void onAdFailedToLoad(final InterstitialAdAdapter adAdapter, final int code) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                AdKey adKey = adAdapter.getAdKey();
                Log.d(TAG, "onAdFailedToLoad adKey = " + adKey + " mTryLoadAdCounter = " + mTryLoadAdCounter);
                if(mReportEvent) {
                    EventHelper.logEvent(mAdCache.getId() + EyuAdManager.EVENT_LOAD_FAILED, "{ \"code\":" + code + ", \"type\":\"" + adKey.getId() + "\"}");
                }
                if (mCurLoadingIndex >= 0 && mAdapterList.get(mCurLoadingIndex) == adAdapter) {
                    if (mTryLoadAdCounter >= mMaxTryLoadAd) {
                        mCurLoadingIndex = -1;
                    } else {
                        mTryLoadAdCounter++;
                        mCurLoadingIndex = (mCurLoadingIndex + 1) % mAdapterList.size();
                        InterstitialAdAdapter adapter = mAdapterList.get(mCurLoadingIndex);
                        Log.d(TAG, "onAdFailedToLoad try to load next ad, adapter = " + adapter + " mTryLoadAdCounter = " + mTryLoadAdCounter + " mCurLoadingIndex = " + mCurLoadingIndex);
                        adapter.loadAd();
                        if(mReportEvent) {
                            EventHelper.logEvent(mAdCache.getId() + EyuAdManager.EVENT_LOADING, "{ \"type\":\"" + adapter.getAdKey().getId() + "\"}");
                        }
                    }
                }
            }
        });

    }

    private void onAdShowed(InterstitialAdAdapter adAdapter) {
        Log.d(TAG, "onAdShowed adAdapter = " + adAdapter + " mAdPlaceId = " + mAdPlaceId);
        if(mReportEvent) {
            EventHelper.logEvent(mAdCache.getId() + EyuAdManager.EVENT_SHOW, "{  \"type\":\"" + adAdapter.getAdKey().getId() + "\"}");
        }
        if (mAdsListener != null) {
            mAdsListener.onAdShowed(EyuAdManager.TYPE_INTERSTITIAL_AD, mAdPlaceId);
        }
    }

    private void onAdClicked(InterstitialAdAdapter adAdapter) {
        Log.d(TAG, "onAdClicked adAdapter = " + adAdapter + " mAdPlaceId = " + mAdPlaceId);
        if(mReportEvent) {
            EventHelper.logEvent(mAdCache.getId() + EyuAdManager.EVENT_CLICKED, "{  \"type\":\"" + adAdapter.getAdKey().getId() + "\"}");
        }
        if (mAdsListener != null) {
            mAdsListener.onAdClicked(EyuAdManager.TYPE_INTERSTITIAL_AD, mAdPlaceId);
        }
    }

    private void onAdClosed(InterstitialAdAdapter adAdapter) {
        Log.d(TAG, "onAdClosed adAdapter = " + adAdapter + " mAdPlaceId = " + mAdPlaceId);
        if (mAdCache.isAutoLoad()) {
            loadAd(mAdPlaceId);
        }
        if (mAdsListener != null) {
            mAdsListener.onAdClosed(EyuAdManager.TYPE_INTERSTITIAL_AD, mAdPlaceId);
        }
    }
}
