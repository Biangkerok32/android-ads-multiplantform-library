package com.eyu.common.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;


import com.eyu.common.R;


/**
 * Description :
 *
 * Creation    : 2016/12/8
 * Author      : luoweiqiang
 */
public class LoadingDialog extends Dialog {
    private static final String TAG = "LoadingDialog";
    public LoadingDialog(Activity activity) {
        this(activity, R.style.loadDialog);
    }

    public LoadingDialog(Activity activity, int theme) {
        super(activity, theme);
        this.setOwnerActivity(activity);
        this.setContentView(R.layout.dialog_loading);
        this.setCancelable(false);
    }

    @Override
    public void show() {
        super.show();
//        try {
//            ((ProgressBar)findViewById(R.id.rotateloading));
//        } catch (Exception e) {
//            Log.e(TAG, "show rotate loading error " + e);
//        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }
}
