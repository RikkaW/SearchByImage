package rikka.searchbyimage;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import rikka.searchbyimage.utils.ClipBoardUtils;
import rikka.searchbyimage.utils.IntentUtils;
import rikka.searchbyimage.utils.URLUtils;

public class WebViewActivity extends AppCompatActivity {
    public static final String EXTRA_URL =
            "rikka.searchbyimage.WebViewActivity.EXTRA_URL";

    public static final String EXTRA_FILE =
            "rikka.searchbyimage.WebViewActivity.EXTRA_FILE";

    public static final String EXTRA_SITE_ID =
            "rikka.searchbyimage.WebViewActivity.EXTRA_SITE_ID";

    private static final String[] SITE_URL = {
            "", // google
            "", // baidu
            "", // iqdb
            "", // tineye
            "http://saucenao.com/" //saucenao
    };

    private WebView mWebView;
    private WebSettings mWebSettings;
    private Context mContext;
    private Activity mActivity;
    private Toolbar mToolbar;
    private AppBarLayout mAppBarLayout;
    private ProgressBar mProgressBar;
    private CoordinatorLayout mCoordinatorLayout;
    private String htmlFilePath;
    private String mImageUrl;

    private String baseUrl;
    private int siteId;

    private boolean mNormalMode = true;

    private DownloadManager downloadManager;
    private long downloadReference;

    private int intentActivitiesSize;

    private File savedFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);

        mAppBarLayout = (AppBarLayout) findViewById(R.id.view);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mProgressBar.setMax(100);

        mContext = this;
        mActivity = this;

        mWebView = (WebView) findViewById(R.id.webView);
        mWebSettings = mWebView.getSettings();

        mWebView.clearCache(true);
        mWebSettings.setJavaScriptEnabled(true);

        //webSettings.setSupportZoom(true);
        mWebSettings.setBuiltInZoomControls(true);
        mWebSettings.setDisplayZoomControls(false);

        //mWebSettings.setUseWideViewPort(true);
        //mWebSettings.setLoadWithOverviewMode(true);

        mWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                super.onProgressChanged(view, progress);

                mProgressBar.setProgress(progress);
            }
        });

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                /**/
                /*view.loadUrl(url);
                mToolbar.setTitle(url);
                mProgressBar.setProgress(0);
                setMyProgressBarVisibility(true);

                return true;

                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mActivity);
                if (sharedPref.getBoolean("use_chrome_custom_tabs", true)) {

                }*/

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

                if (IntentUtils.canOpenWith(mActivity, intent, intentActivitiesSize)) {
                    mActivity.startActivity(intent);

                    return true;
                } else {
                    view.loadUrl(url);
                    mToolbar.setTitle(url);
                    mProgressBar.setProgress(0);
                    setMyProgressBarVisibility(true);

                    return true;
                }

                /*if (url.startsWith(SITE_URL[siteId])) {

                } else {


                    return true;
                }*/

            }

            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(final WebView view, String url) {
                if (url.contains("saucenao-new.css")) {
                    return getCssWebResourceResponseFromAsset();
                } else {
                    return super.shouldInterceptRequest(view, url);
                }
            }

            private WebResourceResponse getCssWebResourceResponseFromAsset() {
                try {
                    return getUtf8EncodedCssWebResourceResponse(getAssets().open("saucenao-new.css"));
                } catch (IOException e) {
                    return null;
                }
            }

            private WebResourceResponse getUtf8EncodedCssWebResourceResponse(InputStream data) {
                return new WebResourceResponse("text/css", "UTF-8", data);
            }


            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                mToolbar.setTitle(url);
                setMyProgressBarVisibility(false);
            }
        });

        registerForContextMenu(mWebView);

        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_FILE)) {
            handleSendFile(intent);
        } else if (intent.hasExtra(EXTRA_URL)) {
            handleSendUrl(intent);
        }


        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, final Intent intent) {
                long reference = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (downloadReference == reference) {
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(downloadReference);
                    Cursor cursor = downloadManager.query(query);

                    if (cursor.moveToFirst()) {
                        final String fileName = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TITLE));

                        Snackbar snackbar = Snackbar.make(mCoordinatorLayout, String.format(getString(R.string.downloaded), fileName) , Snackbar.LENGTH_LONG);
                        snackbar.setActionTextColor(getResources().getColor(R.color.openAction));

                        snackbar.setAction(R.string.open, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent1 = new Intent(Intent.ACTION_VIEW);
                                intent1.setDataAndType(Uri.fromFile(new File(savedFile.getParent() + "/" + fileName)), "image/*");
                                intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                mActivity.startActivity(intent1);
                            }
                        });
                        snackbar.show();
                    }
                }
            }
        };

        registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);


        Intent intent1 = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com"));
        intentActivitiesSize = IntentUtils.getSize(this, intent1);
    }

    private void handleSendFile(Intent intent) {
        mWebSettings.setSupportZoom(false);

        siteId = intent.getIntExtra(EXTRA_SITE_ID, 3);
        baseUrl = SITE_URL[siteId];
        mNormalMode = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String siteName = getResources().getStringArray(R.array.search_engines)[siteId];
            String title = String.format(getString(R.string.search_result), siteName);

            setTaskDescription(new ActivityManager.TaskDescription(
                    title,
                    null,
                    getResources().getColor(R.color.colorPrimary)));

            getSupportActionBar().setTitle(title);
        }

        loadSearchResult(intent.getStringExtra(EXTRA_FILE), baseUrl);
    }

    private void handleSendUrl(Intent intent) {
        mToolbar.setTitle(intent.getStringExtra(EXTRA_URL));
        mWebView.loadUrl(intent.getStringExtra(EXTRA_URL));
        mNormalMode = true;
    }

    private void setMyProgressBarVisibility(boolean visible) {
        if (visible) {
            mProgressBar.setVisibility(ProgressBar.VISIBLE);
            mAppBarLayout.setExpanded(true, true);
        } else {
            mProgressBar.setVisibility(ProgressBar.GONE);
            mAppBarLayout.setExpanded(false, true);
        }
    }

    @Override
    public boolean onKeyDown(final int keyCode, final KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
            if (mNormalMode) {
                mWebView.goBack();
                return true;
            }

            WebBackForwardList webBackForwardList = mWebView.copyBackForwardList();
            String lastUrl = webBackForwardList.getItemAtIndex(webBackForwardList.getCurrentIndex() - 1).getUrl();
            if (lastUrl.equals(baseUrl)) {
                loadSearchResult(htmlFilePath, baseUrl);
            } else {
                mWebView.goBack();
            }

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private static final int REQUEST_CODE = 0;

    private void getPermission(String permission) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startDownload();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void startDownload() {
        Uri uri = Uri.parse(mImageUrl);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        File destinationFile = new File (new File (Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Environment.DIRECTORY_PICTURES) + "/SearchByImage", uri.getLastPathSegment());
        savedFile = destinationFile.getAbsoluteFile();
        request.setDestinationUri(Uri.fromFile(destinationFile));
        downloadReference = downloadManager.enqueue(request);
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);

        WebView.HitTestResult result = mWebView.getHitTestResult();

        MenuItem.OnMenuItemClickListener handler = new MenuItem.OnMenuItemClickListener() {

            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case 0: {
                        ClipBoardUtils.putTextIntoClipboard(mContext, mImageUrl);
                        break;
                    }
                    case 1: {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                                ContextCompat.checkSelfPermission(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            getPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                            break;
                        }

                        startDownload();
                        break;
                    }
                    case 2: {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mImageUrl));
                        mActivity.startActivity(intent);

                        break;
                    }
                }
                return true;
            }
        };

        if (result.getType() == WebView.HitTestResult.SRC_ANCHOR_TYPE) {
            mImageUrl = result.getExtra();
            menu.setHeaderTitle(mImageUrl);
            menu.add(0, 2, 0, R.string.open_with).setOnMenuItemClickListener(handler);
            menu.add(0, 0, 1, R.string.save_link).setOnMenuItemClickListener(handler);
        }

        if (result.getType() == WebView.HitTestResult.IMAGE_TYPE ||
                result.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {

            mImageUrl = result.getExtra();
            menu.setHeaderTitle(mImageUrl);
            menu.add(0, 0, 0, R.string.save_link).setOnMenuItemClickListener(handler);
            menu.add(0, 1, 1, R.string.save_image).setOnMenuItemClickListener(handler);
        }
    }

    private void loadSearchResult(String path, String baseUrl) {
        htmlFilePath = path;

        setMyProgressBarVisibility(true);

        File file = new File(htmlFilePath);

        BufferedInputStream fileStream = null;
        StringBuilder sb = new StringBuilder();

        try {
            byte[] buffer = new byte[4096];

            fileStream = new BufferedInputStream(new FileInputStream(file));
            while ((fileStream.read(buffer)) != -1) {
                sb.append(new String(buffer, Charset.forName("UTF-8")));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileStream != null)
                try {
                    fileStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }

        mWebView.loadDataWithBaseURL(baseUrl,
                sb.toString(),
                "text/html",
                "utf-8",
                baseUrl);
    }
}