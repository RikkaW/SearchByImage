package rikka.searchbyimage.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import rikka.searchbyimage.service.UploadResult;
import rikka.searchbyimage.staticdata.SearchEngine;
import rikka.searchbyimage.utils.BrowsersUtils;
import rikka.searchbyimage.utils.UploadResultUtils;

public class ResultActivity extends BaseResultActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UploadResult result = UploadResultUtils.getResultFromIntent(getIntent(), EXTRA_RESULT);
        if (result == null) {
            return;
        }

        switch (result.getResultOpenAction()) {
            case SearchEngine.RESULT_OPEN_ACTION.DEFAULT:
                BrowsersUtils.open(this, result.getUrl(), true);
                break;
            case SearchEngine.RESULT_OPEN_ACTION.BUILD_IN_IQDB:
                openIqdbResult(this, result);
                break;
            case SearchEngine.RESULT_OPEN_ACTION.OPEN_HTML_FILE:
                openHTMLinWebView(this, result);
                break;
        }

        setIntent(new Intent());
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        if (!getIntent().hasExtra(EXTRA_RESULT)) {
            finish();
        }
    }

    private static void openIqdbResult(Context context, UploadResult result) {
        Intent intent = new Intent(context, IqdbResultActivity.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(IqdbResultActivity.EXTRA_RESULT, result);
        intent.putExtra(IqdbResultActivity.EXTRA_FILE, result.getHtmlUri());

        context.startActivity(intent);
    }

    private static void openHTMLinWebView(Context context, UploadResult result) {
        Intent intent = new Intent(context, WebViewActivity.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(WebViewActivity.EXTRA_RESULT, result);
        intent.putExtra(WebViewActivity.EXTRA_FILE, result.getHtmlUri());

        context.startActivity(intent);
    }
}
