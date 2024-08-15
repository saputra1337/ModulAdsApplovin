package com.adsmedia.adsmodul;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

import android.app.Activity;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;


import com.adsmedia.mastermodul.MasterAdsHelper;
import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdViewAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxAdView;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.applovin.sdk.AppLovinPrivacySettings;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkConfiguration;
import com.applovin.sdk.AppLovinSdkSettings;
import com.applovin.sdk.AppLovinSdkUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;


public class AdsHelper {
    public static boolean openads = true;
    public static boolean directData = false;
    public static void gdpr(Activity activity, Boolean childDirected, String keypos, String gameAppId) {
        AppLovinSdk.initializeSdk(activity, new AppLovinSdk.SdkInitializationListener() {
            @Override
            public void onSdkInitialized(final AppLovinSdkConfiguration configuration) {
                if (configuration.getConsentDialogState() == AppLovinSdkConfiguration.ConsentDialogState.APPLIES) {
                    // Show user consent dialog
                } else if (configuration.getConsentDialogState() == AppLovinSdkConfiguration.ConsentDialogState.DOES_NOT_APPLY) {
                    // No need to show consent dialog, proceed with initialization
                } else {
                    // Consent dialog state is unknown. Proceed with initialization, but check if the consent
                    // dialog should be shown on the next application initialization
                }
            }
        });
        AppLovinPrivacySettings.setHasUserConsent(true, activity);
        AppLovinPrivacySettings.setIsAgeRestrictedUser(childDirected, activity);
    }

    public static void initializeAdsPrime(Activity activity, String keypos, String gameAppId) {
        AppLovinSdk.getInstance(activity).setMediationProvider("max");
        AppLovinSdk.initializeSdk(activity, new AppLovinSdk.SdkInitializationListener() {
            @Override
            public void onSdkInitialized(final AppLovinSdkConfiguration configuration) {
            }
        });
        MasterAdsHelper.initializeAds(activity,keypos);
    }

    public static void debugModePrime(Boolean debug) {
        MasterAdsHelper.debugMode(debug);

    }

    public static void debugModePrime(Boolean debug, Activity activity) {
        MasterAdsHelper.debugMode(debug);
        String android_id = Settings.Secure.getString(activity.getContentResolver(), Settings.Secure.ANDROID_ID);
        String deviceId = md5(android_id).toUpperCase();
        AppLovinSdkSettings settings = new AppLovinSdkSettings( activity );
        settings.setTestDeviceAdvertisingIds(Arrays.asList(deviceId));
        AppLovinSdk sdk = AppLovinSdk.getInstance( settings, activity );
    }

    public static MaxAdView adViewMax;

    public static void showBannerPrime(Activity activity, RelativeLayout layout, String admobId) {
        adViewMax = new MaxAdView(admobId, activity);
        directData = true;
        MaxAdViewAdListener listener = new MaxAdViewAdListener() {
            @Override
            public void onAdExpanded(MaxAd ad) {
            }

            @Override
            public void onAdCollapsed(MaxAd ad) {

            }

            @Override
            public void onAdLoaded(MaxAd ad) {

            }

            @Override
            public void onAdDisplayed(MaxAd ad) {

            }

            @Override
            public void onAdHidden(MaxAd ad) {

            }

            @Override
            public void onAdClicked(MaxAd ad) {

            }

            @Override
            public void onAdLoadFailed(String adUnitId, MaxError error) {
                if (adViewMax != null) {
                    adViewMax.destroy();
                }
                MasterAdsHelper.showBanner(activity,layout);
            }

            @Override
            public void onAdDisplayFailed(MaxAd ad, MaxError error) {

            }
        };
        adViewMax.setListener(listener);
        final boolean isTablet = AppLovinSdkUtils.isTablet(activity);
        final int heightPx = AppLovinSdkUtils.dpToPx(activity, isTablet ? 90 : 50);
        adViewMax.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, heightPx));
        layout.addView(adViewMax);
        adViewMax.loadAd();
    }

    public static MaxInterstitialAd interstitialAd;

    public static void loadInterstitialPrime(Activity activity, String admobId) {
        interstitialAd = new MaxInterstitialAd(admobId, activity);
        directData = true;
        interstitialAd.loadAd();
        MasterAdsHelper.loadInterstitial(activity);
    }

    public static int count = 0;

    public static void showInterstitialPrime(Activity activity, String admobId, int interval) {
        if (count >= interval) {
            if (interstitialAd.isReady()) {
                interstitialAd.showAd();
            } else {
                MasterAdsHelper.showInterstitial(activity);
            }
            loadInterstitialPrime(activity, admobId);
            count = 0;
        } else {
            count++;
        }
    }

    public static final String md5(final String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest
                    .getInstance("MD5");
            digest.update(s.getBytes());
            byte[] messageDigest = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
                String h = Integer.toHexString(0xFF & messageDigest[i]);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            //Logger.logStackTrace(TAG,e);
        }
        return "";
    }
}
