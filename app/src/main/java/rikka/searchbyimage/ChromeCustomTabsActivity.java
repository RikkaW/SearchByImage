package rikka.searchbyimage;

import android.app.ActivityManager;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import rikka.searchbyimage.utils.URLUtils;

public class ChromeCustomTabsActivity extends AppCompatActivity {
    public static final String EXTRA_URL =
            "rikka.searchbyimage.ChromeCustomTabsActivity.EXTRA_URL";

    public static final String EXTRA_SITE_ID =
            "rikka.searchbyimage.ChromeCustomTabsActivity.EXTRA_SITE_ID";

    boolean mIsURLOpened = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chrome_custom_tabs);

        Intent intent = getIntent();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int siteId = intent.getIntExtra(EXTRA_SITE_ID, 2);
            String siteName = getResources().getStringArray(R.array.search_engines)[siteId];
            String label = String.format(getString(R.string.search_result), siteName);

            setTaskDescription(new ActivityManager.TaskDescription(
                    label,
                    null,
                    getResources().getColor(R.color.colorPrimary)));
            setTitle(label);
        }

        URLUtils.Open(intent.getStringExtra(EXTRA_URL), this);

        // 只能用这种奇怪的办法了
        getWindow().getDecorView().postDelayed(URLOpened, 500);
    }

    private Runnable URLOpened = new Runnable() {
        @Override
        public void run() {
            mIsURLOpened = true;
        }
    };

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int siteId = intent.getIntExtra(EXTRA_SITE_ID, 2);
            String siteName = getResources().getStringArray(R.array.search_engines)[siteId];
            String label = String.format(getString(R.string.search_result), siteName);

            setTaskDescription(new ActivityManager.TaskDescription(
                    label,
                    null,
                    getResources().getColor(R.color.colorPrimary)));
            setTitle(label);
        }

        URLUtils.Open(intent.getStringExtra(EXTRA_URL), this);

        // 只能用这种奇怪的办法了
        getWindow().getDecorView().postDelayed(URLOpened, 500);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        if (mIsURLOpened)
            finish();
    }
}
