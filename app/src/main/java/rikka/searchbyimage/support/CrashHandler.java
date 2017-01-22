package rikka.searchbyimage.support;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Build;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import rikka.searchbyimage.BuildConfig;
import rikka.searchbyimage.ui.SendReportActivity;

import static rikka.searchbyimage.support.GetDeviceInfo.getAppInfo;

public class CrashHandler implements Thread.UncaughtExceptionHandler {
    public static String CRASH_DIR;
    public static String CRASH_LOG;

    private static String ANDROID = Build.VERSION.RELEASE;
    private static String MODEL = Build.MODEL;
    private static String MANUFACTURER = Build.MANUFACTURER;

    public static String VERSION = "Unknown";

    private Thread.UncaughtExceptionHandler mPrevious;
    private static Context mContext;

    public static void init(Context context) {
        mContext = context;

        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            VERSION = info.versionName;

            CRASH_DIR = context.getFilesDir().getAbsolutePath() + "/";
            CRASH_LOG = CRASH_DIR + "last_crash.log";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void register() {
        new CrashHandler();
    }

    private CrashHandler() {
        //mPrevious = Thread.currentThread().getUncaughtExceptionHandler();
        mPrevious = Thread.getDefaultUncaughtExceptionHandler();
        //Thread.currentThread().setUncaughtExceptionHandler(this);
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        File f = new File(CRASH_LOG);
        if (f.exists()) {
            f.delete();
        } else {
            try {
                new File(CRASH_DIR).mkdirs();
                f.createNewFile();
            } catch (Exception e) {
                return;
            }
        }

        PrintWriter p;
        try {
            p = new PrintWriter(f);
        } catch (Exception e) {
            return;
        }

        p.write("Android Version: " + ANDROID + "\n");
        p.write("Device Model: " + MODEL + "\n");
        p.write("Device Manufacturer: " + MANUFACTURER + "\n");
        p.write("App Version: " + VERSION + "\n");
        p.write("*********************\n");
        throwable.printStackTrace(p);
        p.close();

        StringBuilder sb = getAppInfo(mContext);

        StringWriter sw = new StringWriter();
        throwable.printStackTrace(new PrintWriter(sw));
        sb.append(sw.toString());


        /*((Application) mContext.getApplicationContext()).getDefaultTracker()
                .send(new HitBuilders.ExceptionBuilder()
                        .setDescription(new StandardExceptionParser(mContext, null)
                                .getDescription(thread.getName(), throwable))
                        .setFatal(true)
                        .build());*/

        Intent intent = new Intent();
        intent.setAction("rikka.searchbyimage.SEND_LOG");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(SendReportActivity.EXTRA_EMAIL_BODY, sb.toString());
        mContext.startActivity(intent);

        /*if (mPrevious != null) {
            mPrevious.uncaughtException(thread, throwable);
        }*/
        if (BuildConfig.DEBUG) {
            throwable.printStackTrace();
        }
        System.exit(1);
    }
}