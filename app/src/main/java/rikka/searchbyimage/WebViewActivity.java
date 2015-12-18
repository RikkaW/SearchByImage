package rikka.searchbyimage;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import rikka.searchbyimage.utils.ClipBoardUtils;

public class WebViewActivity extends AppCompatActivity {
    private WebView mWebView;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        mContext = this;

        mWebView = (WebView) findViewById(R.id.webView);
        WebSettings webSettings = mWebView.getSettings();

        mWebView.clearCache(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        registerForContextMenu(mWebView);


        Intent intent = getIntent();
        if (intent.hasExtra("EXTRA_INPUT")) {
            //mWebView.loadData(intent.getStringExtra("EXTRA_INPUT"), "text/html", "UTF-8");
            //mWebView.loadUrl("file://" + intent.getStringExtra("EXTRA_INPUT"));

            File file = new File(intent.getStringExtra("EXTRA_INPUT"));

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

            mWebView.loadDataWithBaseURL("http://iqdb.org",
                    sb.toString(),
                    "text/html",
                    "utf-8",
                    null);
        } else {
            mWebView.loadUrl("http://www.iqdb.org/");
        }
    }

    @Override
    public boolean onKeyDown(final int keyCode, final KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
            mWebView.goBack();
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