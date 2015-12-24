package rikka.searchbyimage;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import rikka.searchbyimage.utils.ClipBoardUtils;

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
    private String htmlFilePath;
    private String mImageUrl;

    private String baseUrl;

    private boolean mNormalMode = true;

    private DownloadManager downloadManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

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
                view.loadUrl(url);
                mToolbar.setTitle(url);
                mProgressBar.setProgress(0);
                setMyProgressBarVisibility(true);
                return true;
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

        downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
    }

    private void handleSendFile(Intent intent) {
        mWebSettings.setSupportZoom(false);

        int siteId = intent.getIntExtra(EXTRA_SITE_ID, 3);
        baseUrl = SITE_URL[siteId];
        mNormalMode = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String siteName = getResources().getStringArray(R.array.search_engines)[siteId];

            setTaskDescription(new ActivityManager.TaskDescription(
                    String.format(getString(R.string.search_result), siteName),
                    null,
                    getResources().getColor(R.color.colorPrimary)));
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
                        Uri uri = Uri.parse(mImageUrl);
                        DownloadManager.Request request = new DownloadManager.Request(uri);
                        File destinationFile = new File (new File (Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Environment.DIRECTORY_DOWNLOADS), uri.getLastPathSegment());
                        request.setDestinationUri(Uri.fromFile(destinationFile));
                        // Add it to the manager
                        downloadManager.enqueue(request);

                        break;
                    }
                }
                return true;
            }
        };

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

    private static String INJECT_SAUCENAO_CSS(String html) {
        StringBuilder css_sb = new StringBuilder();
        css_sb.append("<style type=\"text/css\" media=\"screen\">");
        css_sb.append("<!--");
        css_sb.append("#footerarea, #headerarea, #message, #left, #randomMessage { display: none; !important }");
        css_sb.append("#mainarea, #headerarea, #footerarea { max-width:100%; !important; min-width: 0%; !important }");
        css_sb.append("body, #middle, #footer-middle { margin-left: 0%; !important; margin-right: 0%; !important }");
        css_sb.append("-->");
        css_sb.append("</style>");

        StringBuilder sb = new StringBuilder();

        int head = html.indexOf("<head>");
        sb.append(html.substring(0, head + "<head>".length()));
        sb.append(css_sb.toString());
        sb.append(html.substring(head + "<head>".length() + 1));

        return sb.toString();
    }
}