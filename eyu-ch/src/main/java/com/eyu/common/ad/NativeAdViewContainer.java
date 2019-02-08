package com.eyu.common.ad;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.eyu.common.R;
import com.eyu.common.ad.adapter.NativeAdAdapter;

public class NativeAdViewContainer {
    private static final String TAG = "NativeAdViewContainer";
    private View mRootLayout;
    private FrameLayout mMediaLayout;
    private ImageView mIcon;
    private TextView mTitle;
    private TextView mDesc;
    private FrameLayout mAdChoicesLayout;
    private Button mAdBtn;
    private boolean mCanShow = false;
    private View mCloseBtn;
    private NativeAdAdapter mAdapter;
    private boolean mNeedUpdate = true;
    private View mBgView;

    public NativeAdViewContainer(View rootLayout) {
        mRootLayout = rootLayout;
        mBgView = rootLayout.findViewById(R.id.native_ad_bg_layout);

        mMediaLayout = rootLayout.findViewById(R.id.native_ad_media_layout);
        mIcon = rootLayout.findViewById(R.id.native_ad_icon);
        mTitle = rootLayout.findViewById(R.id.native_ad_title);
        mDesc = rootLayout.findViewById(R.id.native_ad_desc);
        mAdChoicesLayout = rootLayout.findViewById(R.id.native_ad_choices_framelayout);
        mAdBtn = rootLayout.findViewById(R.id.native_ad_call_btn);
        mCloseBtn = rootLayout.findViewById(R.id.native_ad_close_btn);
        if(mMediaLayout != null){
            mMediaLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EyuAdManager.getInstance().onDefaultNativeAdClicked();
                }
            });
        }
        if(mIcon != null){
            mIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EyuAdManager.getInstance().onDefaultNativeAdClicked();
                }
            });
        }
        if(mTitle != null){
            mTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EyuAdManager.getInstance().onDefaultNativeAdClicked();
                }
            });
        }
        if(mDesc != null){
            mDesc.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EyuAdManager.getInstance().onDefaultNativeAdClicked();
                }
            });
        }
        if(mAdBtn != null){
            mAdBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EyuAdManager.getInstance().onDefaultNativeAdClicked();
                }
            });
        }
        if (mCloseBtn != null) {
            mCloseBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setCanShow(false);
                    setVisibility(View.GONE);
                }
            });
        }
        setVisibility(View.GONE);
    }

    public boolean isCanShow() {
        return mCanShow;
    }
    public void setCanShow(boolean canShow) {
        mCanShow = canShow;
    }

    public void setVisibility(int visibility) {
        if (mRootLayout != null) {
            mRootLayout.setVisibility(visibility);
        }
    }

    public NativeAdAdapter getNativeAdAdapter()
    {
        return mAdapter;
    }

    public void updateNativeAd(NativeAdAdapter adapter)
    {
        try {
            if (mAdapter != null) {
                mAdapter.destroy();
            }
            mAdapter = adapter;
            mAdapter.showAd(this);
            mNeedUpdate = false;
            if (isCanShow()) {
                setVisibility(View.VISIBLE);
            }
        }catch (Exception ex)
        {
            Log.e(TAG,"updateNativeAd error", ex);
        }
    }

    public void setTitle(String title) {
        if (mTitle == null || title == null) return;

        mTitle.setText(title);
        mTitle.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        mTitle.setSingleLine(true);
        mTitle.setSelected(true);
        mTitle.setFocusable(true);
        mTitle.setFocusableInTouchMode(true);
    }

    public TextView getTitle() {
        return mTitle;
    }

    public void setDescription(String description) {
        if (mDesc == null || description == null) return;

        mDesc.setText(description);
        mDesc.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        mDesc.setSingleLine(true);
        mDesc.setSelected(true);
        mDesc.setFocusable(true);
        mDesc.setFocusableInTouchMode(true);
    }

    public TextView getDesc() {
        return mDesc;
    }

    public FrameLayout getMediaLayout() {
        return mMediaLayout;
    }

    public FrameLayout getAdChoicesLayout() {
        return mAdChoicesLayout;
    }

    public ImageView getIcon() {
        return mIcon;
    }

    public Button getAdBtn() {
        return mAdBtn;
    }

    public View getRootLayout() {
        return mRootLayout;
    }

    public void setNeedUpdate(boolean needUpdate)
    {
        this.mNeedUpdate = needUpdate;
    }

    public boolean isNeedUpdate()
    {
        return this.mNeedUpdate;
    }

    public View getBgView() {
        return mBgView;
    }
}
