package rikka.searchbyimage.utils;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.customtabs.CustomTabsIntent;

import rikka.searchbyimage.R;
import rikka.searchbyimage.ui.WebViewActivity;
import rikka.searchbyimage.receiver.ShareBroadcastReceiver;

/**
 * Created by Rikka on 2015/12/21.
 */
public class URLUtils {
    public static void Open(String uri, Activity activity) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
        if (sharedPref.getBoolean("use_chrome_custom_tabs", true)) {
            Open(Uri.parse(uri), activity);
        }
        else {
            OpenBrowser(activity, Uri.parse(uri));
        }
    }

    public static void Open(Uri uri, Activity activity) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
        if (!sharedPref.getBoolean("use_chrome_custom_tabs", true)) {
            OpenBrowser(activity, uri);
            return;
        }

        CustomTabsIntent.Builder intentBuilder = new CustomTabsIntent.Builder();

        int color = activity.getResources().getColor(R.color.colorPrimary);
        intentBuilder.setToolbarColor(color);
        intentBuilder.setShowTitle(true);
        intentBuilder.addMenuItem(activity.getString(R.string.share), createPendingShareIntent(activity));

        CustomTabActivityHelper.openCustomTab(
                activity, intentBuilder.build(), uri, new WebViewFallback());
    }

    private static PendingIntent createPendingShareIntent(Activity activity) {
        Intent actionIntent = new Intent(
                activity.getApplicationContext(), ShareBroadcastReceiver.class);

        return PendingIntent.getBroadcast(activity.getApplicationContext(), 0, actionIntent, 0);
    }

    public static class WebViewFallback implements CustomTabActivityHelper.CustomTabFallback {
        @Override
        public void openUri(Activity activity, Uri uri) {
            Intent intent = new Intent(activity, WebViewActivity.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            } else {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            intent.putExtra(WebViewActivity.EXTRA_URL, uri.toString());
            activity.startActivity(intent);
        }
    }

    public static void OpenBrowser(Activity activity, Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uri);
        activity.startActivity(intent);
    }
}
