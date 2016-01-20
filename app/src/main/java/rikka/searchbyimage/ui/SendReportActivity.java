package rikka.searchbyimage.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import rikka.searchbyimage.R;
import rikka.searchbyimage.utils.IntentUtils;

public class SendReportActivity extends AppCompatActivity {
    public static final String EXTRA_EMAIL_BODY =
            "rikka.searchbyimage.ui.WebViewActivity.EXTRA_URL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_report);

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
        /*Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"rikka@xing.moe"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "SearchByImage crash log");
        intent.putExtra(Intent.EXTRA_TEXT, body);
        startActivity(Intent.createChooser(intent, "Send crash log by Email"));*/

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("mailto:?subject=" + "SearchByImage crash log" + "&body=" + body + "&to=" + "rikka@xing.moe"));
        intent = Intent.createChooser(intent, getString(R.string.send_via));
        if (IntentUtils.canOpenWith(this, intent)) {
            startActivity(intent);
        } else {
            Toast.makeText(this, R.string.app_crash_no_email_client, Toast.LENGTH_SHORT).show();
        }

    }
}