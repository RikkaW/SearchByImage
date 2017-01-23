package rikka.searchbyimage.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

/**
 * Created by Rikka on 2016/12/3.
 */

public class PackageUtils {

    public static boolean isPackageInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static boolean isPackageEnabled(Context context, String packageName) {
        try {
            return context.getPackageManager().getApplicationInfo(packageName, 0).enabled;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static Intent getLaunchIntent(Context context, String packageName) {
        if (!isPackageEnabled(context, packageName)) {
            return null;
        }

        try {
            return context.getPackageManager().getLaunchIntentForPackage(packageName);
        } catch (Exception ignored) {
            return null;
        }
    }
}
