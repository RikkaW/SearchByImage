package rikka.searchbyimage;

import rikka.searchbyimage.staticdata.SearchEngine;
import rikka.searchbyimage.support.CrashHandler;

/**
 * Created by Rikka on 2015/12/31.
 */
public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();

        SearchEngine.getList(this);

        CrashHandler.init(getApplicationContext());
        CrashHandler.register();
    }
}
