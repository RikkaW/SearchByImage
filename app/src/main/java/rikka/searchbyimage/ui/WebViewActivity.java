package rikka.searchbyimage.ui;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import rikka.searchbyimage.R;
import rikka.searchbyimage.support.Settings;
import rikka.searchbyimage.utils.ClipBoardUtils;
import rikka.searchbyimage.utils.DownloadManagerResolver;
import rikka.searchbyimage.utils.IntentUtils;
import rikka.searchbyimage.utils.Utils;
import rikka.searchbyimage.view.ContextMenuTitleView;
import rikka.searchbyimage.view.WebViewToolBar;
import rikka.searchbyimage.widget.InfoBar;

public class WebViewActivity extends BaseResultActivity {
    public static final String EXTRA_URL =
            "rikka.searchbyimage.ui.WebViewActivity.EXTRA_URL";

    public static final String EXTRA_FILE =
            "rikka.searchbyimage.ui.WebViewActivity.EXTRA_FILE";

    public static final String EXTRA_SITE_ID =
            "rikka.searchbyimage.ui.WebViewActivity.EXTRA_SITE_ID";

    private static final String[] SITE_URL = {
            "", // google
            "", // baidu
            "", // iqdb
            "", // tineye
            "http://saucenao.com/", //saucenao
            ""
    };

    private WebView mWebView;
    private WebSettings mWebSettings;
    private Context mContext;
    private WebViewToolBar mToolbar;
    private AppBarLayout mAppBarLayout;
    private CoordinatorLayout mCoordinatorLayout;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private InfoBar mInfoBar;

    private AppBarLayout.Behavior mBehavior;

    private String htmlFilePath;
    private String mImageUrl;

    private String baseUrl;
    private int siteId;

    private boolean mNormalMode = true;

    private DownloadManager mDownloadManager;
    private long downloadReference;

    private int intentActivitiesSize;

    private File savedFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_webview);
        } catch (UnsatisfiedLinkError linkError) {
            Toast.makeText(getApplicationContext(), R.string.webview_version_error, Toast.LENGTH_LONG).show();
            finish();
        } catch (RuntimeException runtime) {
            Toast.makeText(getApplicationContext(), R.string.no_webview, Toast.LENGTH_LONG).show();
            finish();
        }

        mContext = this;

        mToolbar = (WebViewToolBar) findViewById(R.id.toolbar);
        //mToolbar.setTitle("");
        setSupportActionBar(mToolbar);

        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorAccent));
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mToolbar.setProgress(0);
                mToolbar.setCanDrawProgress(true);
                mWebView.reload();
                //mProgressBar.setVisibility(View.VISIBLE);
                //mProgressBar.setProgress(0);
            }
        });

        mAppBarLayout = (AppBarLayout) findViewById(R.id.view);

        mWebView = (WebView) findViewById(R.id.webView);
        mWebSettings = mWebView.getSettings();

        //mWebView.clearCache(true);
        mWebSettings.setJavaScriptEnabled(true);
        //webSettings.setSupportZoom(true);
        mWebSettings.setBuiltInZoomControls(true);
        mWebSettings.setDisplayZoomControls(false);
        //mWebSettings.setUseWideViewPort(true);
        //mWebSettings.setLoadWithOverviewMode(true);
        mWebView.setWebChromeClient(new MyWebChromeClient());
        mWebView.setWebViewClient(new MyWebViewClient());
        mWebView.setOnTouchListener(new MyOnTouchListener());

        registerForContextMenu(mWebView);

        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_FILE)) {
            handleSendFile(intent);
        } else if (intent.hasExtra(EXTRA_URL)) {
            handleSendUrl(intent);
        }


        mDownloadBroadcastReceiver = new DownloadBroadcastReceiver();
        registerReceiver(mDownloadBroadcastReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        mDownloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

        intentActivitiesSize = IntentUtils.getSize(this, new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com")));
    }

    private DownloadBroadcastReceiver mDownloadBroadcastReceiver;

    @Override
    protected void onDestroy() {
        if (mDownloadBroadcastReceiver != null) {
            unregisterReceiver(mDownloadBroadcastReceiver);
        }
        super.onDestroy();
    }

    private void handleSendFile(Intent intent) {
        mWebSettings.setSupportZoom(false);

        siteId = intent.getIntExtra(EXTRA_SITE_ID, 3);
        baseUrl = SITE_URL[siteId];
        mNormalMode = false;

        loadSearchResult(intent.getStringExtra(EXTRA_FILE), baseUrl);
    }

    private void handleSendUrl(Intent intent) {
        mToolbar.setTitle(" ");
        mToolbar.setSubtitle(intent.getStringExtra(EXTRA_URL));
        mWebView.loadUrl(intent.getStringExtra(EXTRA_URL));
        mNormalMode = true;
    }


    private boolean toolBarVisibility = true;

    private void setToolBarVisibility(boolean visible) {
        if (visible == toolBarVisibility)
            return;

        if (visible) {
            //mProgressBar.setVisibility(ProgressBar.VISIBLE);
            toolBarVisibility = true;
            mAppBarLayout.setExpanded(true, true);
        } else {
            toolBarVisibility = false;
            //mProgressBar.setVisibility(ProgressBar.GONE);
            mAppBarLayout.setExpanded(false, true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.webview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_refresh:
                mToolbar.setProgress(0);
                mToolbar.setCanDrawProgress(true);
                mWebView.reload();
                return true;
            case R.id.menu_item_share:
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, mWebView.getUrl());

                Intent chooserIntent = Intent.createChooser(shareIntent, mContext.getString(R.string.share_url));
                chooserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                IntentUtils.startOtherActivity(mContext, chooserIntent);
                return true;
            case R.id.menu_item_copy_link:
                ClipBoardUtils.putTextIntoClipboard(mContext, mWebView.getUrl());
                Snackbar.make(mCoordinatorLayout, String.format(getString(R.string.copy_to_clipboard), mWebView.getUrl()), Snackbar.LENGTH_SHORT).show();
                return true;
            case R.id.menu_item_open_in:
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mWebView.getUrl()));
                IntentUtils.startOtherActivity(this, intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
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
        finish();
        return true;
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
                    try {
                        startDownload();
                    } catch (NullPointerException npe) {
                        npe.printStackTrace();
                        Toast.makeText(getApplicationContext(), getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void startDownload() throws NullPointerException {
        final Uri uri = Uri.parse(mImageUrl);
        String fileName = uri.getLastPathSegment();
        if (fileName == null) {
            fileName = "image.png";

            Settings.instance(this)
                    .edit()
                    .putBoolean(Settings.DOWNLOAD_FILE_CRASH, true)
                    .putString(Settings.DOWNLOAD_URL, mImageUrl)
                    .putString(Settings.DOWNLOAD_IMAGE, fileName)
                    .apply();
        }

        int dot = fileName.lastIndexOf(".");
        if (dot == -1) {
            fileName += ".jpg";
        }

        final File destinationFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                + "/SearchByImage", fileName);

        if (!destinationFile.getParentFile().exists()) {
            if (!destinationFile.getParentFile().mkdirs()) {
                Snackbar.make(mCoordinatorLayout, R.string.save_failed, Snackbar.LENGTH_INDEFINITE);
                return;
            }
        }

        if (destinationFile.exists()) {
            if (mInfoBar != null) {
                mInfoBar.hide();
            }
            mInfoBar = new InfoBar(mCoordinatorLayout);
            mInfoBar.setMessage(Html.fromHtml(String.format(
                    mContext.getString(R.string.file_overwrite),
                    fileName,
                    "Pictures/SearchByImage")));
            mInfoBar.setNegativeButton(R.string.create_new_file, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mInfoBar.hide();
                    downloadFile(uri, destinationFile);
                }
            });
            mInfoBar.setPositiveButton(R.string.replace_file, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mInfoBar.hide();
                    destinationFile.delete();
                    downloadFile(uri, destinationFile);
                }
            });
            mInfoBar.show();
        } else {
            downloadFile(uri, destinationFile);
        }
    }

    private void downloadFile(Uri uri, File file) {
        savedFile = file.getAbsoluteFile();
        String BASE64_HEAD = "base64,";
        if (uri.toString().contains(BASE64_HEAD)) {
            String[] uriPart = uri.toString().split(",");
            saveBase64File(uriPart[1], file);
            return;
        }
        if (DownloadManagerResolver.resolve(this)) {
            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setDestinationUri(Uri.fromFile(file));
            downloadReference = mDownloadManager.enqueue(request);
        }
    }

    private void saveBase64File(String base64, File file) {
        byte[] decodedString = android.util.Base64.decode(base64, android.util.Base64.DEFAULT);
        try {
            OutputStream stream = new FileOutputStream(file);
            stream.write(decodedString);
            downloadSuccessful(file.getName());
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.save_failed, Toast.LENGTH_SHORT).show();
        }
    }

    MenuItem.OnMenuItemClickListener handler = new MenuItem.OnMenuItemClickListener() {
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case 0: {
                    ClipBoardUtils.putTextIntoClipboard(mContext, mImageUrl);
                    Snackbar.make(mCoordinatorLayout, String.format(getString(R.string.copy_to_clipboard), mWebView.getUrl()), Snackbar.LENGTH_SHORT).show();
                    break;
                }
                case 1: {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                            ContextCompat.checkSelfPermission(WebViewActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        getPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                        break;
                    }

                    startDownload();
                    break;
                }
                case 2: {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mImageUrl));
                    IntentUtils.startOtherActivity(WebViewActivity.this, intent);

                    break;
                }
                case 3: {
                    mWebView.loadUrl("https://www.google.com/searchbyimage?image_url=" + mImageUrl);

                    break;
                }
            }
            return true;
        }
    };

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);

        WebView.HitTestResult result = mWebView.getHitTestResult();

        if (result.getType() == WebView.HitTestResult.SRC_ANCHOR_TYPE) {
            mImageUrl = result.getExtra();
            menu.setHeaderView(new ContextMenuTitleView(this, mImageUrl));
            //menu.setHeaderTitle(mImageUrl);
            menu.add(Menu.NONE, 2, 0, R.string.open_with).setOnMenuItemClickListener(handler);
            menu.add(Menu.NONE, 0, 1, R.string.copy_url).setOnMenuItemClickListener(handler);
        }

        if (result.getType() == WebView.HitTestResult.IMAGE_TYPE ||
                result.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {

            mImageUrl = result.getExtra();
            //menu.setHeaderTitle(mImageUrl);
            menu.setHeaderView(new ContextMenuTitleView(this, mImageUrl));
            menu.add(Menu.NONE, 0, 0, R.string.copy_url).setOnMenuItemClickListener(handler);
            menu.add(Menu.NONE, 1, 1, R.string.save_image).setOnMenuItemClickListener(handler);
            menu.add(Menu.NONE, 3, 2, R.string.search_this_image_by_google).setOnMenuItemClickListener(handler);

        }
    }

    private void loadSearchResult(String path, String baseUrl) {
        htmlFilePath = path;
        //mToolbar.setCanDrawProgress(false);
        //mAppBarLayout.setExpanded(false, false);
        //setToolBarVisibility(true);

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

    private void downloadSuccessful(final String fileName) {
        /*set notification*/
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(savedFile.getParent() + "/" + fileName)), "image/*");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Notification notification = new NotificationCompat.Builder(WebViewActivity.this)
                .setContentTitle(fileName)
                .setContentText(getString(R.string.download_complete))
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setAutoCancel(true)
                .setContentIntent(PendingIntent.getActivity(WebViewActivity.this, 0, intent, PendingIntent.FLAG_ONE_SHOT))
                //.setColor(0xFF3F51B5)
                .build();

        final NotificationManager notificationManager =
                (NotificationManager) this.getSystemService(Service.NOTIFICATION_SERVICE);

        notificationManager.notify(fileName.hashCode(), notification);

        /*set snackbar*/
        Snackbar snackbar = Snackbar.make(mCoordinatorLayout, String.format(getString(R.string.downloaded), fileName), Snackbar.LENGTH_LONG);
        snackbar.setActionTextColor(getResources().getColor(R.color.openAction));

        snackbar.setAction(R.string.open, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(Intent.ACTION_VIEW);
                intent1.setDataAndType(Uri.fromFile(new File(savedFile.getParent() + "/" + fileName)), "image/*");
                intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                IntentUtils.startOtherActivity(WebViewActivity.this, intent1);
                notificationManager.cancel(fileName.hashCode());
            }
        });
        snackbar.show();
    }

    private class MyWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            mToolbar.setProgress(newProgress);
            mToolbar.setCanDrawProgress(true);

            if (view.getTitle() != mToolbar.getSubtitle()) {
                mToolbar.setTitle(view.getTitle());
            }
        }
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (mInfoBar != null) {
                mInfoBar.hide();
            }

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

            if (IntentUtils.canOpenWith(WebViewActivity.this, intent, intentActivitiesSize)) {
                WebViewActivity.this.startActivity(intent);

                return true;
            } else {
                view.loadUrl(url);
                mToolbar.setTitle(" ");
                mToolbar.setSubtitle(url);
                setToolBarVisibility(true);

                return true;
            }
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
        public void onPageFinished(WebView webView, String url) {
            super.onPageFinished(webView, url);

            if (!toolBarVisibility) {
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        setToolBarVisibility(false);
                    }
                }, 1500);
            }

            mToolbar.setTitle(webView.getTitle());
            //mToolbar.setSubtitle(webView.getUrl());
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    private class MyOnTouchListener implements View.OnTouchListener {
        int DIRECTION_CHANGE = Utils.dpToPx(10);

        float mLocation;
        float mStart;
        int mState = 0;
        int mOldOffset = 0;
        int mNewOffset = 0;

        float mOldDistance;
        boolean mIsDown = false;

        ValueAnimator mAnimator;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int action = event.getAction();
            float y = event.getRawY();

            if (mBehavior == null) {
                mBehavior = (AppBarLayout.Behavior) ((CoordinatorLayout.LayoutParams) mAppBarLayout.getLayoutParams()).getBehavior();
            }

            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    mOldDistance = 0;
                    mStart = y;
                    mOldOffset = mBehavior.getTopAndBottomOffset();

                    if (mAnimator != null && mAnimator.isRunning()) {
                        mAnimator.cancel();
                    }

                    break;
                case MotionEvent.ACTION_MOVE:
                    mLocation = y;

                    float distance = (mStart - mLocation);
                    if (distance - mOldDistance > DIRECTION_CHANGE && !mIsDown) {
                        mStart = y + DIRECTION_CHANGE;
                        mIsDown = true;
                    }

                    if (distance - mOldDistance < -DIRECTION_CHANGE && mIsDown) {
                        mStart = y - DIRECTION_CHANGE;
                        mIsDown = false;
                    }

                    mNewOffset = mOldOffset - (int) distance;
                    if (mNewOffset <= 0 && mNewOffset >= -mAppBarLayout.getHeight()) {
                        mBehavior.setTopAndBottomOffset(mNewOffset);
                    }

                    mSwipeRefreshLayout.setEnabled(mNewOffset >= 0);

                    if (mInfoBar != null) {
                        if (distance > 0 && mInfoBar.getView().getTranslationY() < mInfoBar.getView().getHeight()) {
                            mInfoBar.getView().setTranslationY(distance);
                            mState = 0;
                        }

                        if (distance < 0 && mInfoBar.getView().getTranslationY() > 0) {
                            mInfoBar.getView().setTranslationY(mInfoBar.getView().getHeight() + distance);
                            mState = 1;
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (mInfoBar != null) {
                        mInfoBar.animateView(mState);
                    }

                    if (mAnimator != null && mAnimator.isRunning()) {
                        mAnimator.cancel();
                    }

                    int old = mBehavior.getTopAndBottomOffset();
                    if (old != 0) {
                        //setToolBarVisibility(-mNewOffset < mAppBarLayout.getHeight() / 2);
                        //int target = -mNewOffset < mAppBarLayout.getHeight() / 2 ? 0 : -mAppBarLayout.getHeight();
                        int target = (mOldOffset - mNewOffset < 0) ? 0 : -mAppBarLayout.getHeight();

                        mAnimator = ValueAnimator.ofInt(old, target);
                        mAnimator.setInterpolator(new LinearOutSlowInInterpolator());
                        mAnimator.setDuration(Math.round((float) Math.abs(target - old) / mAppBarLayout.getHeight() * 300));
                        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                mBehavior.setTopAndBottomOffset((int) animation.getAnimatedValue());
                            }
                        });
                        mAnimator.start();
                    }

                    break;
            }

            return false;
        }
    }

    private class DownloadBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, final Intent intent) {
            long reference = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (downloadReference == reference) {
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(downloadReference);
                Cursor cursor = mDownloadManager.query(query);

                if (cursor.moveToFirst()) {
                    final String fileName = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TITLE));
                    downloadSuccessful(fileName);

                }
            }
        }
    }
}