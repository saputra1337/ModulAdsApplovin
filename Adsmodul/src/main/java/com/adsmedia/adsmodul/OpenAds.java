package com.adsmedia.adsmodul;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxAppOpenAd;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;

import java.util.Date;

public class OpenAds implements LifecycleObserver, Application.ActivityLifecycleCallbacks {
    public static String IDOPEN = "";
    public static MyApplication myApplication;
    public static AppOpenAdManager appOpenAdManager;
    public static Activity currentActivity;

    public OpenAds(MyApplication myApplication) {
        OpenAds.myApplication = myApplication;
        OpenAds.myApplication.registerActivityLifecycleCallbacks(this);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

    public static void LoadOpenAds(String idOpenAds) {
        IDOPEN = idOpenAds;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    protected void onMoveToForeground() {
        // Show the ad (if available) when the app moves to foreground.
        appOpenAdManager.showAdIfAvailable(currentActivity);
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        if (!appOpenAdManager.isShowingAd) {
            currentActivity = activity;
        }
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
    }


    public interface OnShowAdCompleteListener {
        void onShowAdComplete();
    }

    public static class AppOpenAdManager {
        public static MaxAppOpenAd appOpenAdApplovin = null;
        private static boolean isLoadingAd = false;
        static boolean isShowingAd = false;
        private static long loadTime = 0;

        public AppOpenAdManager() {
        }

        public static void loadAd(Context context) {
            if (isLoadingAd || isAdAvailable()) {
                return;
            }

            isLoadingAd = true;
            appOpenAdApplovin = new MaxAppOpenAd(IDOPEN, context);
            appOpenAdApplovin.loadAd();
        }

        private static boolean wasLoadTimeLessThanNHoursAgo(long numHours) {
            long dateDifference = (new Date()).getTime() - loadTime;
            long numMilliSecondsPerHour = 3600000;
            return (dateDifference < (numMilliSecondsPerHour * numHours));
        }

        private static boolean isAdAvailable() {
            return appOpenAdApplovin != null;
        }

        public static void showAdIfAvailable(@NonNull final Activity activity) {
            showAdIfAvailable(activity, new OnShowAdCompleteListener() {
                @Override
                public void onShowAdComplete() {

                }
            });
        }

        public static void showAdIfAvailable(
                @NonNull final Activity activity,
                @NonNull OnShowAdCompleteListener onShowAdCompleteListener) {
            if (isShowingAd) {
                return;
            }

            if (!isAdAvailable()) {
                onShowAdCompleteListener.onShowAdComplete();
                loadAd(activity);
                return;
            }

            appOpenAdApplovin.setListener(new MaxAdListener() {
                @Override
                public void onAdLoaded(MaxAd ad) {
                    isLoadingAd = true;
                }

                @Override
                public void onAdDisplayed(MaxAd ad) {
                    isLoadingAd = false;
                    appOpenAdApplovin = null;
                    isShowingAd = false;
                    loadAd(activity);
                }

                @Override
                public void onAdHidden(MaxAd ad) {
                    isShowingAd = false;
                    onShowAdCompleteListener.onShowAdComplete();
                    loadAd(activity);
                }

                @Override
                public void onAdClicked(MaxAd ad) {
                    isLoadingAd = false;
                    appOpenAdApplovin = null;
                    isShowingAd = false;
                    loadAd(activity);
                }

                @Override
                public void onAdLoadFailed(String adUnitId, MaxError error) {
                    isLoadingAd = false;
                    appOpenAdApplovin = null;
                    isShowingAd = false;
                    onShowAdCompleteListener.onShowAdComplete();
                    loadAd(activity);
                }

                @Override
                public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                    isLoadingAd = false;
                    appOpenAdApplovin = null;
                    isShowingAd = false;
                    onShowAdCompleteListener.onShowAdComplete();
                    loadAd(activity);
                }
            });

            isShowingAd = true;
            appOpenAdApplovin.showAd();
        }
    }
}