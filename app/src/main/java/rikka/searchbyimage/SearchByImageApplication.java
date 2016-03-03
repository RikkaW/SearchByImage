package rikka.searchbyimage;

import android.app.Application;

import java.io.InputStream;

import moe.xing.daynightmode.DayNightMode;
import rikka.searchbyimage.support.CrashHandler;

/**
 * Created by Rikka on 2015/12/31.
 */
public class SearchByImageApplication extends Application {
    // TODO: change this to save to file
    private InputStream imageInputStream;

    public InputStream getImageInputStream() {
        return imageInputStream;
    }

    public void setImageInputStream(InputStream imageInputStream) {
        this.imageInputStream = imageInputStream;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        DayNightMode.setDefaultNightMode(this, DayNightMode.MODE_NIGHT_AUTO);

        if (!BuildConfig.hideOtherEngine && !BuildConfig.DEBUG) {
            CrashHandler.init(getApplicationContext());
            CrashHandler.register();
        }
    }
}
