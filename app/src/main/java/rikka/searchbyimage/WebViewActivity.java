package rikka.searchbyimage;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import rikka.searchbyimage.utils.ClipBoardUtils;

public class WebViewActivity extends AppCompatActivity {
    private WebView mWebView;
    private Context mContext;
    private Activity mActivity;
    private Toolbar mToolbar;
    private AppBarLayout mAppBarLayout;
    private ProgressBar mProgressBar;

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
        WebSettings webSettings = mWebView.getSettings();

        mWebView.clearCache(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);

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
                //mToolbar.setVisibility(View.VISIBLE);
                setAppbarVisibility(true);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                mToolbar.setTitle(url);
                //mToolbar.setVisibility(View.INVISIBLE);
                setAppbarVisibility(false);
            }
        });

        registerForContextMenu(mWebView);


        Intent intent = getIntent();
        if (intent.hasExtra("EXTRA_INPUT")) {
            //mWebView.loadData(intent.getStringExtra("EXTRA_INPUT"), "text/html", "UTF-8");
            //mWebView.loadUrl("file://" + intent.getStringExtra("EXTRA_INPUT"));
            loadSearchResult(intent.getStringExtra("EXTRA_INPUT"));

        } else {
            mWebView.loadUrl("http://www.iqdb.org/");
        }
    }

    private void setAppbarVisibility(boolean visible) {
        if (visible) {
            //mAppBarLayout.animate().translationY(0).setInterpolator(new DecelerateInterpolator()).start();
            //mWebView.animate().translationY(0).setInterpolator(new DecelerateInterpolator()).start();
            mProgressBar.setVisibility(ProgressBar.VISIBLE);
            mAppBarLayout.setExpanded(true, true);
        }
        else {
            //mAppBarLayout.animate().translationY(-mToolbar.getBottom()).setInterpolator(new AccelerateInterpolator()).start();
            //mWebView.animate().translationY(-mToolbar.getBottom()).setInterpolator(new AccelerateInterpolator()).start();
            mProgressBar.setVisibility(ProgressBar.GONE);
            mAppBarLayout.setExpanded(false, true);
        }
    }

    private String htmlFilePath;

    private void loadSearchResult(String path) {
        mToolbar.setTitle("http://iqdb.org");

        htmlFilePath = path;

        loadSearchResult();
    }

    private void loadSearchResult() {
        setAppbarVisibility(true);

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
        mWebView.getUrl();
        mWebView.loadDataWithBaseURL("http://iqdb.org",
                sb.toString(),
                "text/html",
                "utf-8",
                "http://iqdb.org");
    }


    @Override
    public boolean onKeyDown(final int keyCode, final KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
            WebBackForwardList webBackForwardList = mWebView.copyBackForwardList();

            String lastUrl = webBackForwardList.getItemAtIndex(webBackForwardList.getCurrentIndex() - 1).getUrl();
            if (lastUrl.equals("http://iqdb.org/") || lastUrl.equals("https://iqdb.org/")) {
                loadSearchResult();
            } else {
                mWebView.goBack();
            }

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private String mImageUrl;

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
            //menu.add(0, 1, 1, R.string.save_image).setOnMenuItemClickListener(handler);
        }
    }
}