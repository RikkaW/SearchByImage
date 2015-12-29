package rikka.searchbyimage.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.List;

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
}
