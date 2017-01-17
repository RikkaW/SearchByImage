package rikka.searchbyimage;

import android.app.Application;
import android.support.v7.app.AppCompatDelegate;

/**
 * Created by Rikka on 2015/12/31.
 */
public class SearchByImageApplication extends Application {
    /*private Tracker mTracker;*/

    static {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }
    /*/**
     * Gets the default {@link Tracker} for this {@link Application}.
     *
     * @return tracker
     */
    /*synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker(R.xml.global_tracker);
        }
        return mTracker;
    }*/

    @Override
    public void onCreate() {
        super.onCreate();

        //DayNightMode.setDefaultNightMode(DayNightMode.MODE_NIGHT_AUTO);

        //CrashHandler.init(getApplicationContext());
        //CrashHandler.register();
    }
}
