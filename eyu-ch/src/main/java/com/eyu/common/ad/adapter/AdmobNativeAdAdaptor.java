package com.eyu.common.ad.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.eyu.common.ad.NativeAdViewContainer;
import com.eyu.common.ad.model.AdKey;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.formats.MediaView;
import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;

import java.util.List;

public class AdmobNativeAdAdaptor extends NativeAdAdapter {
    private static final String TAG = "AdmobNativeAdAdaptor";
    private AdLoader mAdLoader;
    private boolean mAdLoaded = false;
    private UnifiedNativeAd mNativeAd;
    private MediaView mMediaView = null;
    private ImageView mImageView = null;
    private UnifiedNativeAdView mAdView = null;

    public AdmobNativeAdAdaptor(Context context, AdKey adKey) {
        super(context, adKey);
        mAdLoader = new AdLoader.Builder(context.getApplicationContext(), adKey.getKey())
                .forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
                    @Override
                    public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
                        isLoading = false;
                        mAdLoaded = true;
                        mNativeAd = unifiedNativeAd;
                        cancelTimeoutTask();
                        Log.d(TAG, "onContentAdLoaded");
                        notifyOnAdLoaded();
                    }
                })
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(int i) {
                        Log.d(TAG, "onAdFailedToLoad errorCode = " + i);
                        isLoading = false;
                        cancelTimeoutTask();
                        notifyOnAdFailedLoad(i);
                    }

                    @Override
                    public void onAdOpened() {
                        notifyOnAdShowed();
                    }

                    @Override
                    public void onAdClicked() {
                        notifyOnAdClicked();
                    }
                })
                .withNativeAdOptions(new NativeAdOptions.Builder()
                        .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_LEFT)
                        .build()
                )
                .build();
    }

    @Override
    public void loadAd() {
        Log.d(TAG, "loadAd mAdLoaded = " + mAdLoaded);
        if(isAdLoaded()){
            notifyOnAdLoaded();
            return;
        }
        startTimeoutTask();
        if (!mAdLoader.isLoading()) {
            isLoading = false;
            mAdLoader.loadAd(new AdRequest.Builder()
                    .build());
        }
    }

    @Override
    public boolean isAdLoaded() {
        return mAdLoaded;
    }

    @Override
    protected void destroyAd() {
        if (mAdView != null) {
            mAdView.setMediaView(null);
            mAdView.destroy();
        }
        if (mNativeAd != null) {
            mNativeAd.destroy();
        }

        if (mMediaView != null && mMediaView.getParent() != null) {
            ((FrameLayout)mMediaView.getParent()).removeView(mMediaView);
            mMediaView = null;
        }
        if (mImageView != null && mImageView.getParent() != null) {
            ((FrameLayout)mImageView.getParent()).removeView(mImageView);
            mImageView = null;
        }
    }

    private void showNativeAdView(UnifiedNativeAd nativeAd, NativeAdViewContainer nativeAdViewContainer)
    {
        FrameLayout mediaLayout = nativeAdViewContainer.getMediaLayout();
        TextView titleText = nativeAdViewContainer.getTitle();
        TextView descText = nativeAdViewContainer.getDesc();
        ImageView iconImg = nativeAdViewContainer.getIcon();
        FrameLayout adChoicesLayout = nativeAdViewContainer.getAdChoicesLayout();
        Button actBtn = nativeAdViewContainer.getAdBtn();

        if (adChoicesLayout == null) return;
        adChoicesLayout.removeAllViews();
        UnifiedNativeAdView adView = new UnifiedNativeAdView(mContext);
        adView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT));
        adChoicesLayout.addView(adView);
        mAdView = adView;
        mAdView.setClickable(false);
        if (titleText != null) {
            titleText.setText(nativeAd.getHeadline());
            adView.setHeadlineView(titleText);
            titleText.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            titleText.setSingleLine(true);
            titleText.setSelected(true);
            titleText.setFocusable(true);
            titleText.setFocusableInTouchMode(true);
        }

        if (iconImg != null) {
            if (nativeAd.getIcon() != null) {
                if (nativeAd.getIcon().getDrawable() != null) {
                    Log.d(TAG, "展示 logo drawable");
                    iconImg.setImageDrawable(nativeAd.getIcon().getDrawable());
                } else if (nativeAd.getIcon().getUri() != null) {
                    Log.d(TAG, "展示 logo URI");
                    iconImg.setImageURI(nativeAd.getIcon().getUri());
                }
            } else {
                Log.d(TAG, "展示 images");
                List<NativeAd.Image> images = nativeAd.getImages();
                if (images.size() > 0) {
                    iconImg.setImageDrawable(images.get(0).getDrawable());
                }
            }
            adView.setIconView(iconImg);
        }

        if (descText != null) {
            descText.setText(nativeAd.getBody());
            adView.setBodyView(descText);
            descText.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            descText.setSingleLine(true);
            descText.setSelected(true);
            descText.setFocusable(true);
            descText.setFocusableInTouchMode(true);
        }

        if(actBtn != null){
            actBtn.setText(nativeAd.getCallToAction());
            adView.setCallToActionView(actBtn);
        }

        if (mediaLayout != null) {
            mediaLayout.removeAllViews();
            if (mMediaView != null && mMediaView.getParent() != null) {
                ((FrameLayout)mMediaView.getParent()).removeView(mMediaView);
            }
            mMediaView = new MediaView(mContext);
            mediaLayout.addView(mMediaView);
            adView.setMediaView(mMediaView);
        }
        adView.setNativeAd(nativeAd);
    }

    @Override
    public void showAd(NativeAdViewContainer nativeAdViewContainer) {
        super.showAd(nativeAdViewContainer);
        try {
            if (mNativeAd == null) {
                    Log.e(TAG, "showAd mNativeContentAd is null");
                return;
            }
            Log.d(TAG, "showAd");

            showNativeAdView(mNativeAd, nativeAdViewContainer);

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
