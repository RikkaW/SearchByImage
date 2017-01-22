package rikka.searchbyimage.ui;

import android.app.ActivityManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import rikka.searchbyimage.R;
import rikka.searchbyimage.service.UploadResult;
import rikka.searchbyimage.staticdata.SearchEngine;
import rikka.searchbyimage.utils.UploadResultUtils;

/**
 * Created by Rikka on 2017/1/22.
 */

public class BaseResultActivity extends BaseActivity {

    public static final String EXTRA_RESULT =
            "rikka.searchbyimage.ui.BaseResultActivity.EXTRA_RESULT";

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            UploadResult result = UploadResultUtils.getResultFromIntent(getIntent(), EXTRA_RESULT);
            if (result == null) {
                return;
            }

            int siteId = result.getEngineId();
            String siteName = null;
            if (siteId <= 5) {
                siteName = getResources().getStringArray(R.array.search_engines)[siteId];
            } else {
                SearchEngine item = SearchEngine.getItemById(siteId);
                if (item != null) {
                    siteName = item.getName();
                }
            }

            String title = String.format(getString(R.string.search_result), siteName);
            setTaskDescription(new ActivityManager.TaskDescription(
                    title,
                    null,
                    ContextCompat.getColor(this, R.color.colorPrimary)));
        }
    }
}
