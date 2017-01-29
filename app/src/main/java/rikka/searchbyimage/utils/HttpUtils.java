package rikka.searchbyimage.utils;

import android.text.TextUtils;

import java.net.FileNameMap;
import java.net.URLConnection;

/**
 * Created by Rikka on 2016/1/22.
 */
public class HttpUtils {

    public static String guessMimeType(String path) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String contentTypeFor;
        try {
            contentTypeFor = fileNameMap.getContentTypeFor(path);
        } catch (Exception e) {
            contentTypeFor = "application/octet-stream";
        }

        if (TextUtils.isEmpty(contentTypeFor)) {
            contentTypeFor = "application/octet-stream";
        }
        return contentTypeFor;
    }
}
