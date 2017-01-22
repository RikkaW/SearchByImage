package rikka.searchbyimage.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import rikka.searchbyimage.R;
import rikka.searchbyimage.utils.IntentUtils;

public class SendReportActivity extends BaseActivity {
    public static final String EXTRA_EMAIL_BODY =
            "rikka.searchbyimage.ui.WebViewActivity.EXTRA_URL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_EMAIL_BODY)) {
            handleSendEmail(intent);
        } else {
            throw new RuntimeException("Crash test!");
        }
    }

    private void handleSendEmail(final Intent intent) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.app_crash_title)
                .setMessage(R.string.app_crash_message)
                .setPositiveButton(R.string.app_crash_send_email, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendEmail(intent.getStringExtra(EXTRA_EMAIL_BODY));
                        finish();
                    }
                })
                .setNegativeButton(R.string.app_crash_send_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        finish();
                    }
                })
                .show();
    }

    private void sendEmail(String body) {
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "rikkanyaaa+imageSearchBugReport@gmail.com", null));
        intent.putExtra(Intent.EXTRA_CC, new String[]{"xmu.miffy+imageSearchBugReport@gmail.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "SearchByImage crash log");
        intent.putExtra(Intent.EXTRA_TEXT, body);
        IntentUtils.startOtherActivity(this, intent);
    }
}