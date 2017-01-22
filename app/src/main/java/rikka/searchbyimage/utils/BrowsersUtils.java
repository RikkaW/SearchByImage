package rikka.searchbyimage.utils;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.customtabs.CustomTabsIntent;
import android.text.TextUtils;
import android.widget.Toast;

import rikka.searchbyimage.R;
import rikka.searchbyimage.receiver.ShareBroadcastReceiver;
import rikka.searchbyimage.ui.WebViewActivity;

/**
 * Created by Rikka on 2015/12/21.
 */
public class BrowsersUtils {

    private static final int SHOW_IN_WEBVIEW = 0;
    private static final int SHOW_IN_BROWSER = 1;
    private static final int SHOW_IN_CHROME = 2;

    public static void open(Activity activity, String uri, boolean newTask) {
        if (TextUtils.isEmpty(uri)) {
            Toast.makeText(activity, R.string.something_wrong, Toast.LENGTH_SHORT).show();
            return;
        }
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
        switch (Integer.parseInt(sharedPref.getString("show_result_in", "2"))) {
            case SHOW_IN_WEBVIEW:
                openWebView(activity, Uri.parse(uri), newTask);
                break;
            case SHOW_IN_CHROME:
                openChrome(activity, Uri.parse(uri), newTask);
                break;
            case SHOW_IN_BROWSER:
                openBrowserApp(activity, Uri.parse(uri), newTask);
                break;
        }
    }

    private static void openChrome(Activity activity, Uri uri, boolean newTask) {
        CustomTabsIntent.Builder intentBuilder = new CustomTabsIntent.Builder();

        Intent actionIntent = new Intent(
                activity.getApplicationContext(), ShareBroadcastReceiver.class);

        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(activity.getApplicationContext(), 0, actionIntent, 0);

        int color = activity.getResources().getColor(R.color.colorPrimary);
        intentBuilder.setToolbarColor(color);
        intentBuilder.setShowTitle(true);
        intentBuilder.addMenuItem(activity.getString(R.string.share), pendingIntent);

        CustomTabsIntent intent = intentBuilder.build();

        if (newTask) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                intent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            } else {
                intent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            intent.intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        CustomTabActivityHelper.openCustomTab(
                activity, intent, uri, new WebViewFallback());
    }

    private static class WebViewFallback implements CustomTabActivityHelper.CustomTabFallback {
        @Override
        public void openUri(Activity activity, Uri uri) {
            openWebView(activity, uri, false);
        }
    }

    private static void openWebView(Activity activity, Uri uri, boolean newTask) {
        Intent intent = new Intent(activity, WebViewActivity.class);
        if (newTask) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            } else {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        intent.putExtra(WebViewActivity.EXTRA_URL, uri.toString());
        activity.startActivity(intent);
    }

    private static void openBrowserApp(Context context, Uri uri, boolean newTask) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (newTask) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            } else {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        intent.setData(uri);
        IntentUtils.startOtherActivity(context, intent);
    }
}
