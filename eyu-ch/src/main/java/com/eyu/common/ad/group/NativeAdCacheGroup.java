package com.eyu.common.ad.group;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.eyu.common.ad.EyuAdManager;
import com.eyu.common.ad.EyuAdsListener;
import com.eyu.common.ad.adapter.AdmobNativeAdAdaptor;
import com.eyu.common.ad.adapter.FbNativeAdAdapter;
import com.eyu.common.ad.adapter.MintegralNativeVideoAdAdapter;
import com.eyu.common.ad.adapter.NativeAdAdapter;
import com.eyu.common.ad.adapter.WmNativeAdAdaptor;
import com.eyu.common.ad.listener.NativeAdListener;
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
public class NativeAdCacheGroup {

    private static final String TAG = "NativeAdCacheGroup";
    private HashMap<String, Class<? extends NativeAdAdapter>> mAdapterClassMap = new HashMap<>();
    private ArrayList<NativeAdAdapter> mAdapterList = new ArrayList<>();
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
        mMaxTryLoadAd = adConfig.getMaxTryLoadNativeAd();
        mReportEvent = adConfig.getReportEvent();
        mAdapterClassMap.put(EyuAdManager.NETWORK_FACEBOOK, FbNativeAdAdapter.class);
        mAdapterClassMap.put(EyuAdManager.NETWORK_ADMOB, AdmobNativeAdAdaptor.class);
//        mAdapterClassMap.put(EyuAdManager.NETWORK_APPLOVIN, AppLovinNativeAdAdapter.class);
        mAdapterClassMap.put(EyuAdManager.NETWORK_WM, WmNativeAdAdaptor.class);
        mAdapterClassMap.put(EyuAdManager.NETWORK_MINTEGRAL, MintegralNativeVideoAdAdapter.class);
        initAdapterList();
    }

    private void initAdapterList() {
        List<String> keyIdList = mAdCache.getKeyIdArray();
        List<String> keyRemoveList = new ArrayList<>();
        for (String adId : keyIdList) {
            AdKey adKey = EyuAdManager.getInstance().getAdKey(adId);
            NativeAdAdapter adapter = getAdapter(adKey);
            if (adapter != null) {
                mAdapterList.add(adapter);
            } else {
                keyRemoveList.add(adId);
            }
        }
        keyIdList.removeAll(keyRemoveList);
        keyRemoveList.clear();
    }

    private NativeAdAdapter getAdapter(AdKey adKey) {
        Log.d(TAG, "getAdapter adCache = " + adKey);
        NativeAdAdapter adapter = null;
        if (adKey != null) {
            Class<? extends NativeAdAdapter> adClass = mAdapterClassMap.get(adKey.getNetwork());
            if (adClass != null) {
                try {
                    Class[] cl = {Context.class, AdKey.class};
                    Constructor cons = adClass.getDeclaredConstructor(cl);
                    adapter = (NativeAdAdapter) cons.newInstance(mContext, adKey);
                    adapter.setListener(new NativeAdListener() {

                        @Override
                        public void onAdLoaded(NativeAdAdapter adapter) {
                            NativeAdCacheGroup.this.onAdLoaded(adapter);
                        }

                        @Override
                        public void onAdFailedLoad(NativeAdAdapter adapter, int errcode) {
                            NativeAdCacheGroup.this.onAdFailedToLoad(adapter, errcode);
                        }

                        @Override
                        public void onAdShowed(NativeAdAdapter adapter) {
                            NativeAdCacheGroup.this.onAdShowed(adapter);
                        }

                        @Override
                        public void onAdClicked(NativeAdAdapter adapter) {
                            NativeAdCacheGroup.this.onAdClicked(adapter);
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
        Log.d(TAG, "loadAd mCurLoadingIndex = " + mCurLoadingIndex + " adPlaceId = " + adPlaceId);
        this.mAdPlaceId = adPlaceId;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mAdapterList.size() <= 0) {Log.e(TAG, "mAdapterList.size() <= 0"); return;}//配置错误
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
        for (NativeAdAdapter adapter : mAdapterList) {
            if (adapter.isAdLoaded()) {
                return true;
            }
        }
        return false;
    }

    public NativeAdAdapter getAvailableAdapter(String adPlaceId) {
        Log.d(TAG, "getAvailableAdapter adPlaceId = " + adPlaceId);
            this.mAdPlaceId = adPlaceId;
            Log.d(TAG, "getAvailableAdapter " + " adCache = " + mAdCache + " mCurLoadingIndex = " + mCurLoadingIndex);
            int i = 0;
            for (NativeAdAdapter adapter : mAdapterList) {
                if (adapter.isAdLoaded()) {
                    AdKey adKey = adapter.getAdKey();
                    NativeAdAdapter newAdapter = getAdapter(adKey);
                    mAdapterList.remove(adapter);
                    mAdapterList.add(i, newAdapter);
                    if(mAdCache.isAutoLoad())
                    {
                        newAdapter.loadAd();
                    }
                    return adapter;
                }
                i++;
            }
        return null;
    }

    private void onAdLoaded(final NativeAdAdapter adAdapter) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onAdLoaded mCurLoadingIndex = " + mCurLoadingIndex + " adAdapter = " + adAdapter);
                if (mCurLoadingIndex >= 0 && mAdapterList.get(mCurLoadingIndex) == adAdapter) {
                    mCurLoadingIndex = -1;
                }
                if(mReportEvent) {
                    EventHelper.logEvent(mAdCache.getId() + EyuAdManager.EVENT_LOAD_SUCCESS, "{  \"type\":\"" + adAdapter.getAdKey().getId() + "\"}");
                }
                if(mAdsListener!=null)
                {
                    mAdsListener.onAdLoaded(EyuAdManager.TYPE_NATIVE_AD, mAdPlaceId);
                }
            }
        });

    }

    private void onAdFailedToLoad(final NativeAdAdapter adAdapter, final int code) {
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
                        NativeAdAdapter adapter = mAdapterList.get(mCurLoadingIndex);
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

    private void onAdShowed(NativeAdAdapter adAdapter) {
        if(mReportEvent) {
            EventHelper.logEvent(mAdCache.getId() + EyuAdManager.EVENT_SHOW, "{  \"type\":\"" + adAdapter.getAdKey().getId() + "\"}");
        }
        if(mAdsListener!=null)
        {
            mAdsListener.onAdShowed(EyuAdManager.TYPE_NATIVE_AD, mAdPlaceId);
        }
    }

    private void onAdClicked(NativeAdAdapter adAdapter) {
        Log.d(TAG, "onAdClicked adAdapter = " + adAdapter + " mAdPlaceId = " + mAdPlaceId);
        if(mReportEvent) {
            EventHelper.logEvent(mAdCache.getId() + EyuAdManager.EVENT_CLICKED, "{  \"type\":\"" + adAdapter.getAdKey().getId() + "\"}");
        }
        if(mAdsListener!=null)
        {
            mAdsListener.onAdClicked(EyuAdManager.TYPE_NATIVE_AD, mAdPlaceId);
        }
    }

    public void destroy(Context context) {
        try {
            for (NativeAdAdapter adAdapter : mAdapterList) {
                adAdapter.destroy();
            }
        } catch (Exception ex) {
            Log.e(TAG, "onDestroy error ", ex);
        }
    }
}
