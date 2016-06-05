package rikka.searchbyimage.staticdata;

/**
 * Created by Rikka on 2016/6/1.
 */
public class StaticData {
    private static StaticData sInstance;
    public boolean fileIsReady;

    public static synchronized StaticData instance() {
        if (sInstance == null) {
            sInstance = new StaticData();
        }

        return sInstance;
    }
}
