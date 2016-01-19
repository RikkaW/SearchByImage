package rikka.searchbyimage;

import android.app.Application;

import java.io.InputStream;

import rikka.searchbyimage.support.CrashHandler;

/**
 * Created by Rikka on 2015/12/31.
 */
public class SearchByImageApplication extends Application {
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

        if (!BuildConfig.hideOtherEngine) {
            CrashHandler.init(getApplicationContext());
            CrashHandler.register();
        }
    }
}
