package rikka.searchbyimage.support;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * Created by Rikka on 2017/1/22.
 */

public class OkHttpClientProvider {

    private static OkHttpClient sClient;

    public static OkHttpClient get() {
        if (sClient == null) {
            sClient = new OkHttpClient.Builder()
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .build();
        }
        return sClient;
    }
}
