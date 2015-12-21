package rikka.searchbyimage.utils;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.support.customtabs.CustomTabsIntent;

import rikka.searchbyimage.R;
import rikka.searchbyimage.WebViewActivity;

/**
 * Created by Rikka on 2015/12/21.
 */
public class URLUtils {
    public static void Open(String uri, Activity activity) {
        Open(Uri.parse(uri), activity);
    }

    public static void Open(Uri uri, Activity activity) {
        CustomTabsIntent.Builder intentBuilder = new CustomTabsIntent.Builder();

        int color = activity.getResources().getColor(R.color.colorPrimary);
        intentBuilder.setToolbarColor(color);
        intentBuilder.setShowTitle(true);
        intentBuilder.addMenuItem(activity.getString(R.string.share), createPendingShareIntent(uri.toString(), activity));

        CustomTabActivityHelper.openCustomTab(
                activity, intentBuilder.build(), uri, new WebViewFallback());
    }

    private static PendingIntent createPendingShareIntent(String uri, Activity activity) {
        Intent actionIntent = new Intent(Intent.ACTION_SEND);
        actionIntent.setType("text/plain");
        actionIntent.putExtra(Intent.EXTRA_TEXT, uri);
        return PendingIntent.getActivity(activity.getApplicationContext(), 0, actionIntent, 0);
    }

    public static class WebViewFallback implements CustomTabActivityHelper.CustomTabFallback {
        @Override
        public void openUri(Activity activity, Uri uri) {
            Intent intent = new Intent(activity, WebViewActivity.class);
            intent.putExtra(WebViewActivity.EXTRA_URL, uri.toString());
            activity.startActivity(intent);
        }
    }
}
