package com.eyu.common.ad.group;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.eyu.common.R;
import com.eyu.common.ad.EyuAdManager;
import com.eyu.common.ad.EyuAdsListener;
import com.eyu.common.ad.adapter.AdmobRewardAdAdapter;
import com.eyu.common.ad.adapter.FbRewardAdAdapter;
import com.eyu.common.ad.adapter.MintegralRewardAdAdapter;
import com.eyu.common.ad.adapter.RewardAdAdapter;
import com.eyu.common.ad.adapter.WmRewardAdAdapter;
import com.eyu.common.ad.adapter.UnityRewardAdAdapter;
import com.eyu.common.ad.adapter.VungleRewardAdAdapter;
import com.eyu.common.ad.listener.RewardAdListener;
import com.eyu.common.ad.model.AdCache;
import com.eyu.common.ad.model.AdConfig;
import com.eyu.common.ad.model.AdKey;
import com.eyu.common.dialog.LoadingDialog;
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
public class RewardAdCacheGroup {

    private static final String TAG = "RewardAdCacheGroup";
    private HashMap<String, Class<? extends RewardAdAdapter>> mRewardAdapterClassMap = new HashMap<>();
    private ArrayList<RewardAdAdapter> mRewardAdAdapterList = new ArrayList<>();
    private int mTryLoadRewardAdCounter = 0;
    private long mMaxTryLoadRewardAd = 7;
    private int mCurLoadingIndex = -1;
    private Context mContext = null;
    private AdCache mAdCache = null;
    private LoadingDialog mLoadingDialog;
    private String mAdPlaceId = null;
    private EyuAdsListener mAdsListener;
    private Handler mHandler;
    private Runnable mTimeoutTask;
    private boolean mReportEvent = false;

    public void init(Context context, AdCache adCache, AdConfig adConfig, EyuAdsListener adsListener) {
        this.mContext = context;
        this.mAdCache = adCache;
        this.mAdsListener = adsListener;
        mHandler = new Handler(Looper.getMainLooper());
        mMaxTryLoadRewardAd = adConfig.getMaxTryLoadRewardAd();
        mReportEvent = adConfig.getReportEvent();
        mRewardAdapterClassMap.put(EyuAdManager.NETWORK_FACEBOOK, FbRewardAdAdapter.class);
        mRewardAdapterClassMap.put(EyuAdManager.NETWORK_ADMOB, AdmobRewardAdAdapter.class);
        mRewardAdapterClassMap.put(EyuAdManager.NETWORK_UNITY, UnityRewardAdAdapter.class);
        mRewardAdapterClassMap.put(EyuAdManager.NETWORK_VUNGLE, VungleRewardAdAdapter.class);
//        mRewardAdapterClassMap.put(EyuAdManager.NETWORK_APPLOVIN, AppLovinRewardAdAdapter.class);
        mRewardAdapterClassMap.put(EyuAdManager.NETWORK_WM, WmRewardAdAdapter.class);
        mRewardAdapterClassMap.put(EyuAdManager.NETWORK_MINTEGRAL, MintegralRewardAdAdapter.class);
        initRewardAdAdapterList();
    }

    private void initRewardAdAdapterList() {
        List<String> keyIdList = mAdCache.getKeyIdArray();
        List<String> keyRemoveList = new ArrayList<>();
        for (String adId : keyIdList) {
            AdKey adKey = EyuAdManager.getInstance().getAdKey(adId);
            RewardAdAdapter rewardAdAdapter = getRewardAdAdapter(adKey);
            if (rewardAdAdapter != null) {
                mRewardAdAdapterList.add(rewardAdAdapter);
            } else {
                keyRemoveList.add(adId);
            }
        }
        keyIdList.removeAll(keyRemoveList);
        keyRemoveList.clear();
    }

    private RewardAdAdapter getRewardAdAdapter(AdKey adKey) {
        Log.d(TAG, "getRewardAdAdapter adCache = " + adKey);
        RewardAdAdapter rewardAdAdapter = null;
        if (adKey != null) {
            Class<? extends RewardAdAdapter> adClass = mRewardAdapterClassMap.get(adKey.getNetwork());
            if (adClass != null) {
                try {
                    Class[] cl = {Context.class, AdKey.class};
                    Constructor cons = adClass.getDeclaredConstructor(cl);
                    rewardAdAdapter = (RewardAdAdapter) cons.newInstance(mContext, adKey);
                    rewardAdAdapter.setListener(new RewardAdListener() {
                        @Override
                        public void onAdLoaded(RewardAdAdapter adapter) {
                            onRewardAdLoaded(adapter);
                        }

                        @Override
                        public void onAdFailedLoad(RewardAdAdapter adapter, int errcode) {
                            onRewardAdFailedToLoad(adapter, errcode);
                        }

                        @Override
                        public void onAdShowed(RewardAdAdapter adapter) {
                            onRewardAdShowed(adapter);
                        }

                        @Override
                        public void onAdClicked(RewardAdAdapter adapter) {
                            onRewardAdClicked(adapter);
                        }

                        @Override
                        public void onAdClosed(RewardAdAdapter adapter) {
                            onRewardAdClosed(adapter);
                        }

                        @Override
                        public void onRewarded(RewardAdAdapter adapter) {
                            onRewardAdRewarded(adapter);
                        }
                    });
                } catch (Exception ex) {
                    Log.e(TAG, "getRewardAdAdapter error", ex);
                }
            }
        }
        Log.d(TAG, "getRewardAdAdapter rewardAdAdapter = " + rewardAdAdapter);
        return rewardAdAdapter;
    }

    public void loadRewardedVideoAd(String adPlaceId) {
        Log.d(TAG, "loadRewardedVideoAd");
        this.mAdPlaceId = adPlaceId;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if(mRewardAdAdapterList.size()<= 0) return;//配置错误
                Log.d(TAG, "loadRewardedVideoAd mCurLoadingIndex = " + mCurLoadingIndex);
                if(mCurLoadingIndex == 0) return;//正在加载
                mCurLoadingIndex = 0;
                mTryLoadRewardAdCounter = 1;
                mRewardAdAdapterList.get(0).loadAd();
                if(mReportEvent) {
                    EventHelper.logEvent(mAdCache.getId() + EyuAdManager.EVENT_LOADING, "{ \"type\":\"" + mRewardAdAdapterList.get(0).getAdKey().getId() + "\"}");
                }
            }
        });
    }

    public boolean isAdLoaded()
    {
        for(RewardAdAdapter adapter:mRewardAdAdapterList)
        {
            if(adapter.isAdLoaded())
            {
                return true;
            }
        }
        return false;
    }

    public void showRewardedVideoAd(final Activity activity, final String adPlaceId) {
        Log.d(TAG, "showRewardedVideoAd");
        this.mAdPlaceId = adPlaceId;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "showRewardedVideoAd adCache = " + mAdCache + " mCurLoadingIndex = " + mCurLoadingIndex);
                boolean isShowed = false;
                for(RewardAdAdapter adapter:mRewardAdAdapterList)
                {
                    if(adapter.isAdLoaded())
                    {
                        isShowed = true;
                        mCurLoadingIndex = -1;
                        adapter.showAd(activity);
                        break;
                    }
                }
                if(!isShowed){
                    loadRewardedVideoAd(mAdPlaceId);
                    showLoadingDialog(activity);
                }
            }
        });
    }

    private void showLoadingDialog(Activity activity) {
        Log.d(TAG, "showLoadingDialog mLoadingDialog = " + mLoadingDialog);
        if (mLoadingDialog == null && activity != null && (!activity.isFinishing())) {
            mLoadingDialog = new LoadingDialog(activity);
            mLoadingDialog.show();
            if(mTimeoutTask!=null)
            {
                mHandler.removeCallbacks(mTimeoutTask);
            }
            mTimeoutTask = new Runnable() {
                @Override
                public void run() {
                    mTimeoutTask = null;
                    hideLoadingDialog(false);
                }
            };
            mHandler.postDelayed(mTimeoutTask, 6000);
        }
    }

    private void hideLoadingDialog(boolean isLoaded) {
        Log.d(TAG, "hideLoadingDialog mLoadingDialog = " + mLoadingDialog + " isLoaded = " + isLoaded);
        if (mLoadingDialog != null ) {
            if(mTimeoutTask!=null)
            {
                mHandler.removeCallbacks(mTimeoutTask);
            }
            Activity activity = mLoadingDialog.getOwnerActivity();
            if(activity==null || activity.isFinishing()|| activity.isDestroyed())
            {
                return;
            }
            try {
                mLoadingDialog.dismiss();
            } catch (Exception ex) {
                Log.e(TAG, "hideLoadingDialog Exception", ex);
            }
            mLoadingDialog = null;
            if (isLoaded) {
                showRewardedVideoAd(activity, mAdPlaceId);
            } else {
                RewardAdAdapter loadedRewardAdAdapter = null;
                for (RewardAdAdapter rewardAdAdapter : mRewardAdAdapterList) {//unity或者vungle这些会自动加载
                    if (rewardAdAdapter.isAdLoaded()) {
                        loadedRewardAdAdapter = rewardAdAdapter;
                        break;
                    }
                }
                if (loadedRewardAdAdapter != null) {
                    Log.d(TAG, "showRewardedVideoAd loadedRewardAdAdapter = " + loadedRewardAdAdapter);
                    loadedRewardAdAdapter.showAd(activity);
                } else {
                    Toast.makeText(activity, activity.getString(R.string.ad_failed_to_load), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void destroy(Context context) {
        try {
            for (RewardAdAdapter rewardAdAdapter : mRewardAdAdapterList) {
                rewardAdAdapter.destroy();
            }
        } catch (Exception ex) {
            Log.e(TAG, "onDestroy error ", ex);
        }
    }

    private void onRewardAdLoaded(final RewardAdAdapter adAdapter) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onRewardAdLoaded");
                mCurLoadingIndex = -1;
                if(mReportEvent) {
                    EventHelper.logEvent(mAdCache.getId() + EyuAdManager.EVENT_LOAD_SUCCESS, "{  \"type\":\"" + adAdapter.getAdKey().getId() + "\"}");
                }
                if(mAdsListener!=null)
                {
                    mAdsListener.onAdLoaded(EyuAdManager.TYPE_REWARD_AD, mAdPlaceId);
                }
                hideLoadingDialog(true);
            }
        });
    }

    private void onRewardAdFailedToLoad(final RewardAdAdapter adAdapter, final int code) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                AdKey adKey = adAdapter.getAdKey();
                Log.d(TAG, "onRewardAdFailedToLoad adKey = " + adKey + " mTryLoadRewardAdCounter = " + mTryLoadRewardAdCounter);
                if(mReportEvent) {
                    EventHelper.logEvent(mAdCache.getId() + EyuAdManager.EVENT_LOAD_FAILED, "{ \"code\":" + code + ", \"type\":\"" + adKey.getId() + "\"}");
                }
                if(mCurLoadingIndex >=0 && mRewardAdAdapterList.get(mCurLoadingIndex) == adAdapter) {
                    if (mTryLoadRewardAdCounter >= mMaxTryLoadRewardAd) {
                        mCurLoadingIndex = -1;
                        hideLoadingDialog(false);
                    } else {
                        mTryLoadRewardAdCounter++;
                        mCurLoadingIndex = (mCurLoadingIndex + 1) %mRewardAdAdapterList.size();
                        RewardAdAdapter rewardAdAdapter = mRewardAdAdapterList.get(mCurLoadingIndex);
                        Log.d(TAG, "onRewardAdFailedToLoad try to load next ad, rewardAdAdapter = " + rewardAdAdapter + " mTryLoadRewardAdCounter = " + mTryLoadRewardAdCounter);
                        rewardAdAdapter.loadAd();
                        if(mReportEvent) {
                            EventHelper.logEvent(mAdCache.getId() + EyuAdManager.EVENT_LOADING, "{ \"type\":\"" + rewardAdAdapter.getAdKey().getId() + "\"}");
                        }
                    }
                }
            }
        });

    }

    private void onRewardAdShowed(RewardAdAdapter adAdapter) {
        Log.d(TAG,"onRewardAdShowed adAdapter = " + adAdapter + " mAdPlaceId = " + mAdPlaceId);
        if(mReportEvent) {
            EventHelper.logEvent(mAdCache.getId() + EyuAdManager.EVENT_SHOW, "{  \"type\":\"" + adAdapter.getAdKey().getId() + "\"}");
        }
        if(mAdsListener!=null) {
            mAdsListener.onAdShowed(EyuAdManager.TYPE_REWARD_AD, mAdPlaceId);
        }
    }

    private void onRewardAdClicked(RewardAdAdapter adAdapter) {
        Log.d(TAG,"onRewardAdClicked adAdapter = " + adAdapter + " mAdPlaceId = " + mAdPlaceId);
        if(mReportEvent) {
            EventHelper.logEvent(mAdCache.getId() + EyuAdManager.EVENT_CLICKED, "{  \"type\":\"" + adAdapter.getAdKey().getId() + "\"}");
        }
        if(mAdsListener!=null)
        {
            mAdsListener.onAdClicked(EyuAdManager.TYPE_REWARD_AD, mAdPlaceId);
        }
    }

    private void onRewardAdClosed(RewardAdAdapter adAdapter) {
        Log.d(TAG,"onRewardAdClosed adAdapter = " + adAdapter + " mAdPlaceId = " + mAdPlaceId);
        if(mAdsListener!=null)
        {
            mAdsListener.onAdClosed(EyuAdManager.TYPE_REWARD_AD, mAdPlaceId);
        }
        if(mAdCache.isAutoLoad())
        {
            loadRewardedVideoAd(mAdPlaceId);
        }
    }

    private void onRewardAdRewarded(RewardAdAdapter adAdapter) {
        Log.d(TAG,"onRewardAdRewarded adAdapter = " + adAdapter + " mAdPlaceId = " + mAdPlaceId);
        if(mReportEvent) {
            EventHelper.logEvent(mAdCache.getId() + EyuAdManager.EVENT_REWARDED, "{  \"type\":\"" + adAdapter.getAdKey().getId() + "\"}");
        }
        if(mAdsListener!=null)
        {
            mAdsListener.onAdReward(EyuAdManager.TYPE_REWARD_AD, mAdPlaceId);
        }
    }
}
