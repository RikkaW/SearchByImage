package rikka.searchbyimage.support;

import android.content.Context;
import android.os.Build;

import rikka.searchbyimage.BuildConfig;

/**
 * Created by qixingchen on 16/6/18.
 */

public class GetDeviceInfo {

    private static String ANDROID = Build.VERSION.RELEASE;
    private static String MODEL = Build.MODEL;
    private static String MANUFACTURER = Build.MANUFACTURER;

    public static StringBuilder getAppInfo(Context mContext) {
        StringBuilder sb = new StringBuilder();
        sb.append("Android Version: ").append(ANDROID).append("\n");
        sb.append("Device Model: ").append(MODEL).append("\n");
        sb.append("Device Manufacturer: ").append(MANUFACTURER).append("\n");
        sb.append("App Version: ").append(BuildConfig.VERSION_NAME).append("(")
                .append(BuildConfig.VERSION_CODE).append(")\n");
        sb.append("Flavor: ").append(BuildConfig.FLAVOR).append("\n");

        if (Settings.instance(mContext)
                .getBoolean(Settings.DOWNLOAD_FILE_CRASH, false)) {
            sb.append('\n');
            sb.append("Download image url: ").append(Settings.instance(mContext).getString(Settings.DOWNLOAD_URL, "")).append("\n");
            sb.append("Download image name: ").append(Settings.instance(mContext).getString(Settings.DOWNLOAD_IMAGE, "")).append("\n");
        }
        sb.append("*********************\n");
        return sb;
    }
}
