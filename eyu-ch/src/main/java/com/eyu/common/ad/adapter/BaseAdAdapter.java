package com.eyu.common.ad.adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.eyu.common.ad.EyuAdManager;
import com.eyu.common.ad.model.AdKey;


/**
 * Description :
 * <p>
 * Creation    : 2018/2/27
 * Author      : luoweiqiang
 */
public abstract class BaseAdAdapter {
    private static String TAG = "BaseAdAdapter";
    protected Context mContext;
    protected boolean isLoading;
    protected AdKey mAdKey;
    private Handler mHandler;
    private TimeoutTask mTimeoutTask;

    private long mTimeoutTime = 15*1000;
    protected static final int ERROR_SDK_UNINITED                   = -10001;
    protected static final int ERROR_OTHER_ADMOB_REWARD_AD_LOADED   = -11001;
    protected static final int ERROR_OTHER_ADMOB_REWARD_AD_LOADING  = -11002;
    protected static final int ERROR_UNITY_AD_NOT_LOADED            = -12001;
    protected static final int ERROR_TIMEOUT                        = -13001;
    protected static final int ERROR_VUNGLE_AD_LOADED_ERROR         = -14001;
    protected static final int ERROR_LOAD_MINTEGRAL_NATIVE_AD       = -15001;
    protected static final int ERROR_LOAD_MINTEGRAL_INTERSTITIAL_AD = -15002;
    protected static final int ERROR_LOAD_MINTEGRAL_REWARD_AD       = -15003;
    protected static final int ERROR_LOAD_WM_NATIVE_AD              = -16001;
    protected static final int ERROR_LOAD_WM_INTERSTITIAL_AD        = -16002;
    protected static final int ERROR_LOAD_WM_REWARD_AD              = -16003;


    public BaseAdAdapter(Context context, AdKey adKey)
    {
        this.mContext = context;
        this.mAdKey = adKey;
        this.isLoading = false;
        this.mHandler = new Handler(Looper.getMainLooper());
        this.mTimeoutTime = EyuAdManager.getInstance().getLoadAdTimeout();
    }

    public AdKey getAdKey() {
        return mAdKey;
    }

    public boolean isAdLoading() {
        return isLoading;
    }

    public abstract void onLoadAdTimeout();

    protected void startTimeoutTask()
    {
        if(this.mTimeoutTask == null)
        {
            this.mTimeoutTask = new TimeoutTask();
            mHandler.postDelayed(this.mTimeoutTask,this.mTimeoutTime);
        }
    }

    protected void cancelTimeoutTask()
    {
        if(this.mTimeoutTask != null)
        {
            mHandler.removeCallbacks(mTimeoutTask);
            mTimeoutTask = null;
        }
    }

    class TimeoutTask implements Runnable {
        @Override
        public void run() {
            mTimeoutTask = null;
            onLoadAdTimeout();
        }
    }

    public void destroy()
    {
        cancelTimeoutTask();
    }
}
