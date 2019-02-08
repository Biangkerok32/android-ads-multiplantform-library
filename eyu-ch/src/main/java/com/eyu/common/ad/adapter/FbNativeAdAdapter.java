package com.eyu.common.ad.adapter;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.eyu.common.ad.NativeAdViewContainer;
import com.eyu.common.ad.model.AdKey;
import com.facebook.ads.Ad;
import com.facebook.ads.AdChoicesView;
import com.facebook.ads.AdError;
import com.facebook.ads.AdIconView;
import com.facebook.ads.AdListener;
import com.facebook.ads.MediaView;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Description :
 * <p>
 * Creation    : 2018/3/22
 * Author      : luoweiqiang
 */
public class FbNativeAdAdapter extends NativeAdAdapter {

    private static final String TAG = "FbNativeAdAdapter";
    private NativeAd mNativeAd = null;
    private MediaView mMediaView = null;
    private AdIconView mIconView = null;

    private NativeAdListener mAdListener = new NativeAdListener() {
        @Override
        public void onMediaDownloaded(Ad ad) {
            Log.d(TAG, "mAdListener onMediaDownloaded");
        }

        @Override
        public void onError(Ad ad, AdError error) {
            // Ad error callback
            Log.e(TAG, "mAdListener onError, code = " + error.getErrorCode() + " message = " + error.getErrorMessage());
            isLoading = false;
            destroyAd();
            cancelTimeoutTask();
            notifyOnAdFailedLoad(error.getErrorCode());
        }

        @Override
        public void onAdLoaded(Ad ad) {
            Log.d(TAG, "mAdListener onAdLoaded");
            isLoading = false;
            cancelTimeoutTask();
            notifyOnAdLoaded();
        }

        @Override
        public void onAdClicked(Ad ad) {
            // Ad clicked callback
            Log.d(TAG, "mAdListener onAdClicked");
            notifyOnAdClicked();
        }

        @Override
        public void onLoggingImpression(Ad ad) {
            // Ad impression logged callback
            Log.d(TAG, "mAdListener onLoggingImpression");
            notifyOnAdShowed();
        }
    };

    public FbNativeAdAdapter(Context context, AdKey adKey) {
        super(context, adKey);
    }

    @Override
    public void loadAd() {
        Log.d(TAG, "loadAd mNativeAd = " + mNativeAd);
        if(isAdLoaded()){
            notifyOnAdLoaded();
            return;
        }
        startTimeoutTask();
        if(mNativeAd==null && !isLoading)
        {
            isLoading = true;
            mNativeAd = new NativeAd(this.mContext, this.mAdKey.getKey());
            mNativeAd.setAdListener(this.mAdListener);
            mNativeAd.loadAd();

        }
    }

    @Override
    public void showAd(NativeAdViewContainer nativeAdViewContainer) {
        if (mNativeAd == null) return;
        super.showAd(nativeAdViewContainer);
        Log.d(TAG, "showAd mNativeAd = " + mNativeAd);
        try {
            nativeAdViewContainer.setVisibility(View.VISIBLE);
            mNativeAd.unregisterView();
            List<View> clickableViews = new ArrayList<>();
            FrameLayout mediaLayout = nativeAdViewContainer.getMediaLayout();
            if(mediaLayout!=null ){
                mediaLayout.removeAllViews();
                if(mMediaView!=null && mMediaView.getParent()!=null){
                    ((ViewGroup)mMediaView.getParent()).removeView(mMediaView);
                }

                FrameLayout.LayoutParams adParams =
                        new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                                FrameLayout.LayoutParams.MATCH_PARENT);
                mMediaView = new MediaView(mediaLayout.getContext());
                mediaLayout.addView(mMediaView, adParams);
                clickableViews.add(mediaLayout);
            }

            ImageView iconImg = nativeAdViewContainer.getIcon();
            if(iconImg!=null && iconImg.getParent()!=null) {
                if(mIconView!=null && mIconView.getParent()!=null)
                {
                    ((ViewGroup)mIconView.getParent()).removeView(mIconView);
                }
                ViewGroup iconLayout = (ViewGroup)iconImg.getParent();
                ViewGroup.LayoutParams layoutParams = iconImg.getLayoutParams();
                mIconView = new AdIconView(iconImg.getContext());
                iconLayout.addView(mIconView, layoutParams);
//                NativeAd.downloadAndDisplayImage(mNativeAd.getAdIcon(), iconImg);
                clickableViews.add(mIconView);
            }

            Button actBtn = nativeAdViewContainer.getAdBtn();
            if( actBtn!=null)
            {
                actBtn.setText(mNativeAd.getAdCallToAction());
                clickableViews.add(actBtn);
            }

            TextView titleTxt = nativeAdViewContainer.getTitle();
            if(titleTxt!=null){
                nativeAdViewContainer.setTitle(mNativeAd.getAdvertiserName());
//                titleTxt.setText(mNativeAd.getAdTitle());
//                titleTxt.setEllipsize(TextUtils.TruncateAt.MARQUEE);
//                titleTxt.setSingleLine(true);
//                titleTxt.setSelected(true);
//                titleTxt.setFocusable(true);
//                titleTxt.setFocusableInTouchMode(true);
                clickableViews.add(titleTxt);
            }

            TextView descTxt = nativeAdViewContainer.getDesc();
            if(descTxt != null){
                nativeAdViewContainer.setDescription(mNativeAd.getAdSocialContext());
//                descTxt.setText(mNativeAd.getAdSocialContext());
//                descTxt.setEllipsize(TextUtils.TruncateAt.MARQUEE);
//                descTxt.setSingleLine(true);
//                descTxt.setSelected(true);
//                descTxt.setFocusable(true);
//                descTxt.setFocusableInTouchMode(true);
                clickableViews.add(descTxt);
            }

            FrameLayout adChoicesLayout = nativeAdViewContainer.getAdChoicesLayout();
            if(adChoicesLayout !=null) {
                adChoicesLayout.removeAllViews();
                AdChoicesView adChoicesView = new AdChoicesView(mContext, mNativeAd, true);
                adChoicesLayout.addView(adChoicesView);
            }

            mNativeAd.registerViewForInteraction(nativeAdViewContainer.getRootLayout(), mMediaView, mIconView, clickableViews);
        } catch (Exception ex) {
            Log.e(TAG, "updateNativeAd error", ex);
        }
    }

    @Override
    public boolean isAdLoaded() {
        return mNativeAd != null && mNativeAd.isAdLoaded();
    }

    @Override
    protected void destroyAd() {
        if (mNativeAd != null) {
            mNativeAd.unregisterView();
            mNativeAd.destroy();
            mNativeAd = null;
        }
        if(mMediaView!=null && mMediaView.getParent()!=null){
            ((ViewGroup)mMediaView.getParent()).removeView(mMediaView);
        }

        if(mIconView!=null && mIconView.getParent()!=null){
            ((ViewGroup)mIconView.getParent()).removeView(mIconView);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        destroyAd();
    }
}
