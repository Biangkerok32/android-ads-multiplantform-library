package com.eyu.common.ad.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.eyu.common.ad.NativeAdViewContainer;
import com.eyu.common.ad.model.AdKey;
import com.mintegral.msdk.MIntegralConstans;
import com.mintegral.msdk.nativex.view.MTGMediaView;
import com.mintegral.msdk.out.Campaign;
import com.mintegral.msdk.out.Frame;
import com.mintegral.msdk.out.MtgNativeHandler;
import com.mintegral.msdk.out.NativeListener;
import com.mintegral.msdk.out.OnImageLoadListener;
import com.mintegral.msdk.widget.MTGAdChoice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MintegralNativeVideoAdAdapter extends NativeAdAdapter {
    private final static String TAG = "MtgNativeVideoAd";
    private MtgNativeHandler mNativeHandler;
    private final int AD_NUM = 1;
    private Campaign mCampaign;
    private MTGMediaView mMediaview;
    private MTGAdChoice mMtgAdChoice;
    private NativeAdViewContainer mNativeAdViewContainer;
    private boolean videoSupport = true;//support native video

    public MintegralNativeVideoAdAdapter(Context context, AdKey adKey) {
        super(context, adKey);

        Map<String, Object> properties = MtgNativeHandler.getNativeProperties(adKey.getKey());
        properties.put(MIntegralConstans.NATIVE_VIDEO_WIDTH, 720);
        properties.put(MIntegralConstans.NATIVE_VIDEO_HEIGHT, 480);
        properties.put(MIntegralConstans.NATIVE_VIDEO_SUPPORT, videoSupport);
        properties.put(MIntegralConstans.PROPERTIES_AD_NUM, AD_NUM);

        mNativeHandler = new MtgNativeHandler(properties, context);
        mNativeHandler.setAdListener(new NativeListener.NativeAdListener() {
            @Override
            public void onAdLoaded(List<Campaign> list, int i) {
                isLoading = false;
                cancelTimeoutTask();
                if (list != null && list.size() > 0) {
                    Log.d(TAG, "onAdLoaded " + list.size());
                    mCampaign = list.get(0);
                    notifyOnAdLoaded();
                } else {
                    notifyOnAdFailedLoad(ERROR_LOAD_MINTEGRAL_NATIVE_AD);
                }

            }

            @Override
            public void onAdLoadError(String s) {
                cancelTimeoutTask();
                Log.w(TAG, "onAdLoadError " + s);
                isLoading = false;
                notifyOnAdFailedLoad(ERROR_LOAD_MINTEGRAL_NATIVE_AD);
            }

            @Override
            public void onAdClick(Campaign campaign) {
                notifyOnAdClicked();
            }

            @Override
            public void onAdFramesLoaded(List<Frame> list) {

            }

            @Override
            public void onLoggingImpression(int i) {
                notifyOnAdShowed();
            }
        });
        mNativeHandler.setTrackingListener(new NativeListener.NativeTrackingListener() {
            @Override
            public boolean onInterceptDefaultLoadingDialog() {
                return false;
            }

            @Override
            public void onShowLoading(Campaign campaign) {

            }

            @Override
            public void onDismissLoading(Campaign campaign) {

            }

            @Override
            public void onStartRedirection(Campaign campaign, String s) {

            }

            @Override
            public void onFinishRedirection(Campaign campaign, String s) {

            }

            @Override
            public void onRedirectionFailed(Campaign campaign, String s) {

            }

            @Override
            public void onDownloadStart(Campaign campaign) {

            }

            @Override
            public void onDownloadFinish(Campaign campaign) {

            }

            @Override
            public void onDownloadProgress(int i) {

            }
        });
    }

    @Override
    public void loadAd() {
        Log.d(TAG, "loadAd");
        if (isAdLoaded()) {
            notifyOnAdLoaded();
            return;
        }
        startTimeoutTask();
        if (mNativeHandler != null && !isAdLoading()) {
            isLoading = true;
            mNativeHandler.load();
        }
    }

    @Override
    public boolean isAdLoaded() {
        return mCampaign != null;
    }

    @Override
    protected void destroyAd() {
        if (mCampaign != null) {
            mCampaign.setOnImageLoadListener(null);
        }
        if (mMediaview != null) {
            if (mMediaview.getParent() != null) {
                ((ViewGroup) mMediaview.getParent()).removeView(mMediaview);
            }
            mMediaview.destory();
            mMediaview = null;
        }
        if (mMtgAdChoice != null) {
            if (mMtgAdChoice.getParent() != null) {
                ((ViewGroup)mMtgAdChoice.getParent()).removeView(mMtgAdChoice);
            }
            mMtgAdChoice = null;
        }
        if (mNativeHandler != null) {
            if (mNativeAdViewContainer != null && mCampaign != null && mNativeAdViewContainer.getBgView() != null) {
                List<View> views = getClickableViews(mNativeAdViewContainer);
                if (views.size() > 0) {
                    mNativeHandler.unregisterView(mNativeAdViewContainer.getBgView(), views, mCampaign);
                } else {
                    mNativeHandler.unregisterView(mNativeAdViewContainer.getBgView(), mCampaign);
                }
            }
            mNativeHandler.release();
            mNativeHandler = null;
        }
    }

    @Override
    public void showAd(NativeAdViewContainer nativeAdViewContainer) {
        super.showAd(nativeAdViewContainer);

        mNativeAdViewContainer = nativeAdViewContainer;

        if (isAdLoaded()) {
            if (mMediaview == null) {
                FrameLayout mediaLayout = nativeAdViewContainer.getMediaLayout();
                if (mediaLayout != null) {
                    mediaLayout.removeAllViews();
                    mMediaview = new MTGMediaView(mediaLayout.getContext());
                    mediaLayout.addView(mMediaview, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
                    mMediaview.setNativeAd(mCampaign);
                }
            }

            ImageView iconView = nativeAdViewContainer.getIcon();
            Drawable iconDrawable = mCampaign.getIconDrawable();
            if (iconView != null) {
                if (iconDrawable != null) {
                    iconView.setImageDrawable(iconDrawable);
                } else {
                    if (!TextUtils.isEmpty(mCampaign.getIconUrl())) {
                        mCampaign.loadIconUrlAsyncWithBlock(new OnImageLoadListener() {
                            @Override
                            public void loadSuccess(Drawable drawable, int i) {
                                iconView.setImageDrawable(drawable);
                            }

                            @Override
                            public void loadError(String s) {
                                Log.e(TAG, "load icon error " + s);
                            }
                        });
                    }
                }
            }

            String appName = mCampaign.getAppName();
            if (appName != null) {
                nativeAdViewContainer.setTitle(appName);
            }

            String appDesc = mCampaign.getAppDesc();
            if (appDesc != null) {
                nativeAdViewContainer.setDescription(appDesc);
            }

            if (nativeAdViewContainer.getBgView() != null) {
                if (nativeAdViewContainer.getAdBtn() != null && mCampaign.getAdCall() != null) {
                    nativeAdViewContainer.getAdBtn().setText(mCampaign.getAdCall());
                }

                List<View> views = getClickableViews(nativeAdViewContainer);
                if (views.size() > 0) {
                    mNativeHandler.registerView(nativeAdViewContainer.getBgView(), views, mCampaign);
                } else {
                    mNativeHandler.registerView(nativeAdViewContainer.getBgView(), mCampaign);
                }
            } else {
                Log.e(TAG, "root layout is null");
            }

            FrameLayout adChoicesLayout = nativeAdViewContainer.getAdChoicesLayout();
            if (adChoicesLayout != null) {
                mMtgAdChoice = new MTGAdChoice(adChoicesLayout.getContext());
                ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.width = mCampaign.getAdchoiceSizeWidth();
                layoutParams.height = mCampaign.getAdchoiceSizeHeight();
                adChoicesLayout.addView(mMtgAdChoice, layoutParams);
                mMtgAdChoice.setCampaign(mCampaign);
            }

        }
    }

    @Override
    public void destroy() {
        super.destroy();
        destroyAd();
    }

    private List<View> getClickableViews(NativeAdViewContainer nativeAdViewContainer) {
        List<View> views = new ArrayList<>();

        if (nativeAdViewContainer.getTitle() != null) {
            views.add(nativeAdViewContainer.getTitle());
        }

        if (nativeAdViewContainer.getDesc() != null) {
            views.add(nativeAdViewContainer.getDesc());
        }

        if (nativeAdViewContainer.getAdBtn() != null) {
            views.add(nativeAdViewContainer.getAdBtn());
        }

        if (nativeAdViewContainer.getIcon() != null) {
            views.add(nativeAdViewContainer.getIcon());
        }

        return views;
    }
}
