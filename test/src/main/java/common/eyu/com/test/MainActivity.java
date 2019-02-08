package common.eyu.com.test;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.eyu.common.SdkHelper;
import com.eyu.common.ad.EyuAdManager;
import com.eyu.common.ad.EyuAdsListener;
import com.eyu.common.ad.model.AdConfig;
import com.eyu.common.firebase.EyuRemoteConfigHelper;
import com.eyu.piano.R;

public class MainActivity extends AppCompatActivity {
    private static String TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        SdkHelper.init(this.getApplicationContext());
//        SdkHelper.initCrashlytics("","");
        reloadAdConfig();
    }

    private void reloadAdConfig() {
        Log.d(TAG, "reloadAdConfig");
        //广告配置
        AdConfig adConfig = new AdConfig();
        adConfig.setAdPlaceConfigStr(EyuRemoteConfigHelper.readRawString(this, R.raw.ad_setting));
        adConfig.setAdKeyConfigStr(EyuRemoteConfigHelper.readRawString(this, R.raw.ad_key_setting));
        adConfig.setAdGroupConfigStr(EyuRemoteConfigHelper.readRawString(this, R.raw.ad_cache_setting));

//        adConfig.setUnityClientId(EyuRemoteConfigHelper.getString(RemoteConfigHelper.KEY_UNITY_CLIENT_ID));
//        adConfig.setVungleClientId(EyuRemoteConfigHelper.getString(RemoteConfigHelper.KEY_VUNGLE_CLIENT_ID));
        adConfig.setTtClientId("5010560");
        adConfig.setMaxTryLoadRewardAd(1);
        adConfig.setMaxTryLoadNativeAd(1);
        adConfig.setMaxTryLoadInterstitialAd(1);
        adConfig.setMintegralAppId("111417");
        adConfig.setMintegralAppKey("a339a16bbaca844012276afad6f59eaa");

        EyuAdManager.getInstance().config(MainActivity.this, adConfig, new EyuAdsListener() {
            @Override
            public void onAdReward(String type, String placeId) {

            }

            @Override
            public void onAdLoaded(String type, String placeId) {

            }

            @Override
            public void onAdShowed(String type, String placeId) {

            }

            @Override
            public void onAdClosed(String type, String placeId) {

            }

            @Override
            public void onAdClicked(String type, String placeId) {

            }

            @Override
            public void onDefaultNativeAdClicked() {

            }
        });
    }
}
