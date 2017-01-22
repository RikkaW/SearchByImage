package rikka.searchbyimage.utils;

import java.net.FileNameMap;
import java.net.URLConnection;

/**
 * Created by Rikka on 2016/1/22.
 */
public class HttpUtils {

    public static String guessMimeType(String path) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String contentTypeFor = fileNameMap.getContentTypeFor(path);
        if (contentTypeFor == null) {
            contentTypeFor = "application/octet-stream";
        }
        return contentTypeFor;
    }
}
