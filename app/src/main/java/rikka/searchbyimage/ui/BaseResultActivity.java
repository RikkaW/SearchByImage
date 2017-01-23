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

    protected UploadResult mUploadResult;
    protected ActivityManager.TaskDescription mTaskDescription;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUploadResult = UploadResultUtils.getResultFromIntent(getIntent(), EXTRA_RESULT);
        if (mUploadResult == null) {
            return;
        }

        setTaskDescription();
    }

    private void setTaskDescription() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }

        if (mUploadResult == null) {
            return;
        }

        if (mTaskDescription == null) {
            int siteId = mUploadResult.getEngineId();
            String siteName = null;
            if (siteId <= 5) {
                siteName = getResources().getStringArray(R.array.search_engines)[siteId];
            } else {
                SearchEngine item = SearchEngine.getItemById(siteId);
                if (item != null) {
                    siteName = item.getName();
                }
            }

            mTaskDescription = new ActivityManager.TaskDescription(
                    String.format(getString(R.string.search_result), siteName),
                    null,
                    ContextCompat.getColor(this, R.color.colorPrimary));
        }

        setTaskDescription(mTaskDescription);
    }
}
