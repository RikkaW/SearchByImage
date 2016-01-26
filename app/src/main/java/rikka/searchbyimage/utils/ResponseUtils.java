package rikka.searchbyimage.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import rikka.searchbyimage.BuildConfig;
import rikka.searchbyimage.R;
import rikka.searchbyimage.ui.ChromeCustomTabsActivity;
import rikka.searchbyimage.ui.ResultActivity;
import rikka.searchbyimage.ui.WebViewActivity;

/**
 * Created by Rikka on 2016/1/26.
 */
public class ResponseUtils {
    public static class ErrorMessage {
        public String title;
        public String message;

        public ErrorMessage(String title, String message) {
            this.title = title;
            this.message = message;
        }

        public ErrorMessage(Exception e, String message) {
            if (BuildConfig.DEBUG) {
                this.title = message;
                this.message = getExceptionText(e);
            } else {
                this.title = null;
                this.message = message;
            }
        }
    }

    public static class HttpUpload {
        public String url;
        public int siteId;
        public ErrorMessage error;
        public String html;
    }

    public static String getModifiedGoogleUrl(Context context, String url) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean safeSearch = sharedPref.getBoolean("safe_search_preference", true);
        boolean noRedirect = sharedPref.getString("google_region_preference", "0").equals("1");
        boolean customRedirect = sharedPref.getString("google_region_preference", "0").equals("2");

        url += safeSearch ? "&safe=active" : "&safe=off";

        if (noRedirect || customRedirect) {
            url += "?gws_rd=cr";
        }

        int start = url.indexOf("www.google.");
        int end = url.indexOf("/", start);

        String googleUri = "www.google.com";

        if (customRedirect) {
            googleUri = sharedPref.getString("google_region", googleUri);
        }

        return url.substring(0, start) + googleUri + url.substring(end);
    }

    public static String getUrlFromBaiduJSON(Context context, InputStream stream) throws Exception {
        int err_no = 0;
        String contsign = "";
        String obj_url = "";
        String simid = "";
        String error_msg = "";

        JsonReader reader = null;
        try {
            reader = new JsonReader(new InputStreamReader(stream));
            reader.beginObject();

            while (reader.hasNext()) {
                String keyName = reader.nextName();
                switch (keyName) {
                    case "errno":
                        err_no = reader.nextInt();
                        break;
                    case "msg":
                        error_msg = reader.nextString();
                        break;
                    case "json_data":
                        reader.beginObject();
                        break;
                    case "contsign":
                        contsign = reader.nextString();
                        break;
                    case "obj_url":
                        obj_url = reader.nextString();
                        break;
                    case "simid":
                        simid = reader.nextString();
                        break;
                    default:
                        reader.skipValue();
                        break;
                }
            }
            reader.endObject();
        } catch (IllegalStateException | IOException e) {
            e.printStackTrace();
            throw new Exception(error_msg + "\n\n" + context.getString(R.string.notice_baidu) + (BuildConfig.DEBUG ? "\n\n" + ResponseUtils.getExceptionText(e) : ""));
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (err_no != 0) {
            throw new Exception(error_msg + "\n\n" + context.getString(R.string.notice_baidu));
        }

        return "http://image.baidu.com/n/mo_search?guess=1&rn=30&appid=0&tag=1&isMobile=0" + "&queryImageUrl=" + obj_url + "&querySign=" + contsign + "&simid=" + simid;
    }

    public static void openURL(Activity activity, HttpUpload result) {
        Intent intent;

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
        switch (sharedPref.getString("show_result_in", URLUtils.SHOW_IN_WEBVIEW)) {
            case URLUtils.SHOW_IN_WEBVIEW:
            case URLUtils.SHOW_IN_CHROME:
                intent = new Intent(activity, ChromeCustomTabsActivity.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                } else {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                intent.putExtra(ChromeCustomTabsActivity.EXTRA_URL, result.url);
                intent.putExtra(ChromeCustomTabsActivity.EXTRA_SITE_ID, result.siteId);

                activity.startActivity(intent);
                break;
            case URLUtils.SHOW_IN_BROWSER:
                URLUtils.OpenBrowser(activity, Uri.parse(result.url));
                break;
        }
    }

    public static void openIqdbResult(Activity activity, HttpUpload result) {
        Intent intent;

        intent = new Intent(activity, ResultActivity.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        intent.putExtra(ResultActivity.EXTRA_FILE, result.html);
        intent.putExtra(ResultActivity.EXTRA_SITE_ID, result.siteId);

        activity.startActivity(intent);
    }

    public static void openHTMLinWebView(Activity activity, HttpUpload result) {
        Intent intent;
        intent = new Intent(activity, WebViewActivity.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        intent.putExtra(WebViewActivity.EXTRA_FILE, result.html);
        intent.putExtra(WebViewActivity.EXTRA_SITE_ID, result.siteId);

        activity.startActivity(intent);
    }

    public static String getExceptionText(Exception e) {
        String result = "ErrorMessage: " + e.toString() + "\nFile: ";

        for (StackTraceElement stackTraceElement :
                e.getStackTrace()) {
            //if (stackTraceElement.getFileName().startsWith("HttpUtil") || stackTraceElement.getFileName().startsWith("UploadActivity"))
            result += "\n" + stackTraceElement.getFileName() + " (" + stackTraceElement.getLineNumber() + ")";
        }

        return result;
    }
}
