package rikka.searchbyimage.ui;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import rikka.searchbyimage.utils.BrowsersUtils;

/**
 * Created by Rikka on 2017/1/23.
 */

public class ChromeCustomTabsActivity extends BaseResultActivity {

    private boolean mPaused;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mUploadResult == null || TextUtils.isEmpty(mUploadResult.getUrl())) {
            finish();
            return;
        }
        BrowsersUtils.openChrome(this, Uri.parse(mUploadResult.getUrl()), false, mUploadResult);

        //setIntent(new Intent());
    }

    @Override
    protected void onPause() {
        super.onPause();

        mPaused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mPaused) {
            finish();
        }
    }
}
