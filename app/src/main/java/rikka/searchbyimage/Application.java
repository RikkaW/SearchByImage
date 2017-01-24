package rikka.searchbyimage;

import android.support.v7.app.AppCompatDelegate;

import rikka.searchbyimage.staticdata.SearchEngine;
import rikka.searchbyimage.support.Settings;

/**
 * Created by Rikka on 2015/12/31.
 */
public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();

        SearchEngine.getList(this);

        Settings.instance(this).putInt(Settings.LAST_INSTALLED_VERSION, BuildConfig.VERSION_CODE);
        // use firebase crash reporting
        /*CrashHandler.init(getApplicationContext());
        CrashHandler.register();*/

        AppCompatDelegate.setDefaultNightMode(
                Settings.instance(this).getIntFromString(Settings.NIGHT_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM));
    }
}
