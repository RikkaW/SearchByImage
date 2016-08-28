package rikka.searchbyimage;

import android.app.Application;
import android.support.annotation.NonNull;

import com.avos.avoscloud.AVAnalytics;
import com.avos.avoscloud.AVOSCloud;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import moe.xing.daynightmode.DayNightMode;

/**
 * Created by Rikka on 2015/12/31.
 */
public class SearchByImageApplication extends Application {

    private Tracker mTracker;

    /**
     * Gets the default {@link Tracker} for this {@link Application}.
     *
     * @return tracker
     */
    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker(R.xml.global_tracker);
        }
        return mTracker;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        DayNightMode.setDefaultNightMode(DayNightMode.MODE_NIGHT_AUTO);

        if (!BuildConfig.hideOtherEngine) {
            AVOSCloud.initialize(this, "jn5rOQdydwKneRsJUxR8UXzv-gzGzoHsz", "fw3fTe1JtGn8ockywhkoWIEq");
            AVAnalytics.enableCrashReport(this, true);
        }
    }
}
