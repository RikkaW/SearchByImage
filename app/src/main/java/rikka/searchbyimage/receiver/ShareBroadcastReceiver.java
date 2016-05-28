package rikka.searchbyimage.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import rikka.searchbyimage.R;
import rikka.searchbyimage.utils.IntentUtils;

/**
 * Created by Rikka on 2015/12/21.
 */
public class ShareBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String url = intent.getDataString();

        if (url != null) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, url);

            Intent chooserIntent = Intent.createChooser(shareIntent, context.getString(R.string.share_url));
            chooserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            IntentUtils.startOtherActivity(context,intent);
        }
    }
}
