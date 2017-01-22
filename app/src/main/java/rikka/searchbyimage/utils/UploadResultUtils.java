package rikka.searchbyimage.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.JsonReader;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import rikka.searchbyimage.BuildConfig;
import rikka.searchbyimage.R;
import rikka.searchbyimage.service.UploadResult;
import rikka.searchbyimage.service.UploadService;
import rikka.searchbyimage.support.Settings;
import rikka.searchbyimage.ui.ResultActivity;

/**
 * Created by Rikka on 2016/1/26.
 */
public class UploadResultUtils {

    public static String getModifiedGoogleUrl(Context context, String url) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean safeSearch = sharedPref.getBoolean("safe_search_preference", true);
        boolean noRedirect = sharedPref.getString("google_region_preference", "0").equals("1");
        boolean customRedirect = sharedPref.getString("google_region_preference", "0").equals("2");

        if (BuildConfig.hideOtherEngine)
            url += safeSearch ? "&safe=active" : "&safe=image";
        else
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
            throw new Exception(error_msg + "\n\n" + context.getString(R.string.notice_baidu) /*+ (BuildConfig.DEBUG ? "\n\n" + UploadResultUtils.getExceptionText(e) : "")*/);
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

    @Nullable
    public static UploadResult getResultFromIntent(Intent intent, String name) {
        Parcelable parcelable = intent.getParcelableExtra(name);
        if (parcelable == null) {
            Log.e("UploadResultUtils", "handleResult: no data");
            return null;
        }

        if (!(parcelable instanceof UploadResult)) {
            Log.wtf("UploadMessageReceiver", "handleResult: wrong type");
            return null;
        }

        return (UploadResult) parcelable;
    }

    public static void handleResult(Context context, Intent intent, boolean background) {
        UploadResult result = getResultFromIntent(intent, UploadService.EXTRA_RESULT);

        if (result != null) {
            handleResult(context, result, background);
        }
    }

    public static void handleResult(Context context, UploadResult result, boolean background) {
        String errorMessage = null;
        int errorCode = result.getErrorCode();
        Intent intent = null;
        if (errorCode != 0) {
            errorMessage = result.getErrorMessage();
        } else {
            intent = new Intent(context, ResultActivity.class);
            intent.putExtra(ResultActivity.EXTRA_RESULT, result);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        if (background && Settings.instance(context).getBoolean(Settings.SHOW_NOTIFICATION, false)) {
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Service.NOTIFICATION_SERVICE);
            Notification notification;

            // TODO group notification for android 7.0+

            int code = result.getFileUri().hashCode();
            if (errorCode != 0) {
                notification = new NotificationCompat.Builder(context)
                        .setColor(0xFF3F51B5)
                        .setSmallIcon(R.drawable.ic_stat)
                        .setContentTitle(result.getFilename())
                        .setContentText("上传失败")
                        .build();
            } else {
                PendingIntent pendingIntent = PendingIntent.getActivity(context, code, intent, PendingIntent.FLAG_ONE_SHOT);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                        .setColor(0xFF3F51B5)
                        .setSmallIcon(R.drawable.ic_stat)
                        .setContentTitle(result.getFilename() + " 已上传完成")
                        .setContentText("轻触查看结果")
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder.setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setVibrate(new long[0]);
                }

                notification = builder.build();
            }

            notificationManager.notify(code, notification);
        } else {
            if (errorCode != 0) {
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
                return;
            }

            context.startActivity(intent);
        }
    }
}
