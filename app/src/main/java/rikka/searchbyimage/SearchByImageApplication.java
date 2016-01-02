package rikka.searchbyimage;

import android.app.Application;

import java.io.InputStream;

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
    }
}
