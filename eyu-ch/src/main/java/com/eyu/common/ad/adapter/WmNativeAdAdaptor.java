package com.eyu.common.ad.adapter;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.callback.AQuery2;
import com.androidquery.callback.ImageOptions;
import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTFeedAd;
import com.bytedance.sdk.openadsdk.TTImage;
import com.bytedance.sdk.openadsdk.TTNativeAd;
import com.eyu.common.ad.EyuAdManager;
import com.eyu.common.ad.NativeAdViewContainer;
import com.eyu.common.ad.model.AdKey;

import java.util.ArrayList;
import java.util.List;

public class WmNativeAdAdaptor extends NativeAdAdapter {
    private static final String TAG = "WmNativeAdAdaptor";
    private TTAdNative mTTAdNative;
    private boolean mAdLoaded = false;
    private AdSlot mAdSlot;
    private TTAdNative.FeedAdListener mFeedAdListener;
    private TTFeedAd mTTFeedAd = null;
    private View mMediaView = null;
    private ImageView mIconView = null;
    private ImageView mChoiceView = null;
    private AQuery2 mQuery;

    public WmNativeAdAdaptor(Context context, AdKey adKey) {
        super(context, adKey);
        TTAdManager ttAdManager = EyuAdManager.getInstance().getTtAdManager();
        mTTAdNative = ttAdManager.createAdNative(context);
        mQuery = new AQuery2(context);

        mAdSlot = new AdSlot.Builder()
                .setCodeId(adKey.getKey())
                .setSupportDeepLink(true)
                .setImageAcceptedSize(640, 320)
                .setAdCount(1)
                .setOrientation(TTAdConstant.HORIZONTAL)
                .build();

        mFeedAdListener = new TTAdNative.FeedAdListener() {
            @Override
            public void onError(int code, String message) {
                Log.e(TAG, "onError code="+code+" message="+message);
                cancelTimeoutTask();
                isLoading = false;
                notifyOnAdFailedLoad(code);
            }

            @Override
            public void onFeedAdLoad(List<TTFeedAd> ads) {
                cancelTimeoutTask();
                if (ads == null || ads.isEmpty()) {
                    Log.e(TAG, "onDrawFeedAdLoad ad is null! ");
                    notifyOnAdFailedLoad(ERROR_LOAD_WM_NATIVE_AD);
                    return;
                }
                Log.d(TAG, "onDrawFeedAdLoad");
                mAdLoaded = true;
                isLoading = false;
                notifyOnAdLoaded();
                if (ads.size() > 0) {
                    mTTFeedAd = ads.get(0);
                }
            }
        };

    }

    private void showNativeAdView(TTFeedAd nativeAd, NativeAdViewContainer nativeAdViewContainer)
    {
        nativeAdViewContainer.setVisibility(View.VISIBLE);
        List<View> clickableViews = new ArrayList<>();
        FrameLayout mediaLayout = nativeAdViewContainer.getMediaLayout();
        if(mediaLayout!=null ){
            mediaLayout.removeAllViews();
            if(mMediaView!=null && mMediaView.getParent()!=null){
                ((ViewGroup)mMediaView.getParent()).removeView(mMediaView);
                mMediaView = null;
            }

            if (nativeAd.getImageMode() == TTAdConstant.IMAGE_MODE_SMALL_IMG) {
            } else if (nativeAd.getImageMode() == TTAdConstant.IMAGE_MODE_LARGE_IMG) {
                if (nativeAd.getImageList() != null && !nativeAd.getImageList().isEmpty()) {
                    mMediaView = new ImageView(mediaLayout.getContext());
                    TTImage image = nativeAd.getImageList().get(0);
                    if (image != null && image.isValid()) {
                        mQuery.id(mMediaView).image(image.getImageUrl());
                    }
                }
            } else if (nativeAd.getImageMode() == TTAdConstant.IMAGE_MODE_GROUP_IMG) {
            } else if (nativeAd.getImageMode() == TTAdConstant.IMAGE_MODE_VIDEO) {
                mMediaView = nativeAd.getAdView();
            }

            if (mMediaView != null && mMediaView.getParent() == null) {
                FrameLayout.LayoutParams adParams =
                        new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                                FrameLayout.LayoutParams.WRAP_CONTENT);
                mediaLayout.addView(mMediaView, adParams);
                clickableViews.add(mMediaView);
            }

        }

        ImageView iconImg = nativeAdViewContainer.getIcon();
        if(iconImg!=null && iconImg.getParent()!=null) {
//            if(mIconView!=null && mIconView.getParent()!=null)
//            {
//                ((ViewGroup)mIconView.getParent()).removeView(mIconView);
//            }
//            ViewGroup iconLayout = (ViewGroup)iconImg.getParent();
//            ViewGroup.LayoutParams layoutParams = iconImg.getLayoutParams();
//            mIconView = new ImageView(iconImg.getContext());
//            iconLayout.addView(mIconView, layoutParams);
//            clickableViews.add(mIconView);

            if (nativeAd.getIcon() != null && nativeAd.getIcon().isValid()) {
                ImageOptions options = new ImageOptions();
//                mQuery.id(mIconView).image(nativeAd.getIcon().getImageUrl(), options);
                mQuery.id(iconImg).image(nativeAd.getIcon().getImageUrl(), options);
            }
        }

        FrameLayout adChoicesLayout = nativeAdViewContainer.getAdChoicesLayout();
        if (adChoicesLayout != null) {
            if(mChoiceView!=null && mChoiceView.getParent()!=null)
            {
                ((ViewGroup)mChoiceView.getParent()).removeView(mChoiceView);
            }
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            mChoiceView = new ImageView(adChoicesLayout.getContext());
            mChoiceView.setImageBitmap(nativeAd.getAdLogo());
            adChoicesLayout.addView(mChoiceView, layoutParams);
        }

        Button actBtn = nativeAdViewContainer.getAdBtn();
        if( actBtn!=null)
        {
            actBtn.setText(nativeAd.getButtonText());
            clickableViews.add(actBtn);
        }

        TextView titleTxt = nativeAdViewContainer.getTitle();
        if(titleTxt!=null){
            nativeAdViewContainer.setTitle(nativeAd.getTitle());
            clickableViews.add(titleTxt);
        }

        TextView descTxt = nativeAdViewContainer.getDesc();
        if(descTxt != null){
            nativeAdViewContainer.setDescription(nativeAd.getDescription());
            clickableViews.add(descTxt);
        }

        nativeAd.registerViewForInteraction((ViewGroup) nativeAdViewContainer.getRootLayout(), clickableViews, null, new TTNativeAd.AdInteractionListener() {
            @Override
            public void onAdClicked(View var1, TTNativeAd var2) {

            }

            @Override
            public void onAdCreativeClick(View var1, TTNativeAd var2) {

            }

            @Override
            public void onAdShow(TTNativeAd var1) {

            }
        });
    }

    @Override
    public void loadAd() {
        Log.d(TAG, "loadAd mAdLoaded = " + mAdLoaded);
        if (isAdLoaded()) {
            notifyOnAdLoaded();
            return;
        }
        startTimeoutTask();
        if(mTTAdNative != null && !isAdLoading()) {
            isLoading = true;
            mTTAdNative.loadFeedAd(mAdSlot, mFeedAdListener);
        }
    }

    @Override
    public boolean isAdLoaded() {
        return mAdLoaded;
    }

    @Override
    protected void destroyAd() {
        if(mMediaView!=null && mMediaView.getParent()!=null){
            ((ViewGroup)mMediaView.getParent()).removeView(mMediaView);
        }
        if(mIconView!=null && mIconView.getParent()!=null){
            ((ViewGroup)mIconView.getParent()).removeView(mIconView);
        }
        if(mChoiceView!=null && mChoiceView.getParent()!=null){
            ((ViewGroup)mChoiceView.getParent()).removeView(mChoiceView);
        }
    }

    @Override
    public void showAd(NativeAdViewContainer nativeAdViewContainer) {
        super.showAd(nativeAdViewContainer);
        try {
            if (mTTFeedAd == null) {
                Log.e(TAG, "showAd mNativeContentAd is null");
                return;
            }
            Log.d(TAG, "showAd");
            if (isAdLoaded()) {
                showNativeAdView(mTTFeedAd, nativeAdViewContainer);
            }
        } catch (Exception e) {
            Log.e(TAG, "showAd", e);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        destroyAd();
    }

}
