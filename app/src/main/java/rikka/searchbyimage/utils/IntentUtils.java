package rikka.searchbyimage.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.widget.Toast;

import java.util.List;

import rikka.searchbyimage.R;

/**
 * Created by Rikka on 2015/12/28.
 */
public class IntentUtils {
    public static int getSize(Context context, Intent intent) {
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
        return activities.size();
    }

    public static boolean canOpenWith(Context context, Intent intent) {
        return canOpenWith(context, intent, 0);
    }

    public static boolean canOpenWith(Context context, Intent intent, int minSize) {
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
        return activities.size() > minSize;
    }

    /**
     * start other app's activity
     * may not found
     */
    public static void startOtherActivity(Context context, Intent intent) {
        startOtherActivity(context, intent, context.getString(R.string.target_app_not_found));
    }

    public static void startOtherActivity(Context context, Intent intent, String notFoundMessage) {
        if (canOpenWith(context, intent)) {
            if (!(context instanceof Activity)) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            context.startActivity(intent);
        } else {
            Toast.makeText(context, notFoundMessage, Toast.LENGTH_LONG).show();
        }
    }
}
