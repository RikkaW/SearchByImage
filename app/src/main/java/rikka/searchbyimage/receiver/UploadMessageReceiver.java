package rikka.searchbyimage.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;

import java.util.Locale;

import rikka.searchbyimage.R;
import rikka.searchbyimage.service.UploadService;

/**
 * Created by Yulan on 2016/5/28.
 * receiver message which send by {@link rikka.searchbyimage.service.UploadService}
 * register in manifest,lower level than {@link rikka.searchbyimage.ui.UploadActivity}
 * only when {@link rikka.searchbyimage.ui.UploadActivity} not exist,will received message
 * todo not receive anything
 */

public class UploadMessageReceiver extends BroadcastReceiver {

    private static final int NOTIFICATION_ID = 0x8;

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Service.NOTIFICATION_SERVICE);
        Notification notification = null;
        switch (intent.getAction()) {
            case UploadService.INTENT_ACTION_RETRY:
                int times = intent.getIntExtra(UploadService.INTENT_RETRY_TIMES, 0);
                notification = new NotificationCompat.Builder(context)
                        .setColor(0xFF3F51B5)
                        .setSmallIcon(R.drawable.ic_stat)
                        .setContentTitle("Retrying")
                        .setContentText(String.format(Locale.getDefault(), "Retrying:%d times", times))
                        .build();
                notificationManager.notify(NOTIFICATION_ID, notification);
                break;
            case UploadService.INTENT_ACTION_SUCCESS:
                notificationManager.cancel(NOTIFICATION_ID);
                break;
            case UploadService.INTENT_ACTION_ERROR:
                String title = intent.getStringExtra(UploadService.INTENT_ERROR_TITLE);
                String message = intent.getStringExtra(UploadService.INTENT_ERROR_MESSAGE);
                notification = new NotificationCompat.Builder(context)
                        .setContentTitle(title == null ? context.getString(R.string.upload_form_add) : title)
                        .setContentText(message)
                        .setColor(0xFF3F51B5)
                        .setSmallIcon(R.drawable.ic_stat)
                        .setAutoCancel(true)
                        .build();
                notificationManager.notify(NOTIFICATION_ID, notification);
            default:
                break;
        }
    }
}
