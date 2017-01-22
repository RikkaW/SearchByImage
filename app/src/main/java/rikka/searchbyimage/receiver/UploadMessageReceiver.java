package rikka.searchbyimage.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import rikka.searchbyimage.utils.UploadResultUtils;

/**
 * Created by Yulan on 2016/5/28.
 * receiver message which send by {@link rikka.searchbyimage.service.UploadService}
 * register in manifest, lower level than {@link rikka.searchbyimage.ui.UploadActivity}
 * only when {@link rikka.searchbyimage.ui.UploadActivity} not exist, will received message
 */

public class UploadMessageReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        UploadResultUtils.handleResult(context, intent, true);
    }
}
