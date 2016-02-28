package rikka.searchbyimage.ui;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import rikka.searchbyimage.R;
import rikka.searchbyimage.SearchByImageApplication;
import rikka.searchbyimage.staticdata.CustomEngine;
import rikka.searchbyimage.utils.HttpUtils;
import rikka.searchbyimage.utils.ImageUtils;
import rikka.searchbyimage.utils.ResponseUtils;
import rikka.searchbyimage.utils.Utils;

public class UploadActivity extends AppCompatActivity {
    public final static int SITE_GOOGLE = 0;
    public final static int SITE_BAIDU = 1;
    public final static int SITE_IQDB = 2;
    public final static int SITE_TINEYE = 3;
    public final static int SITE_SAUCENAO = 4;
    public final static int SITE_ASCII2D = 5;

    private class UploadTask extends AsyncTask<Uri, Integer, ResponseUtils.HttpUpload> {

        private Activity mActivity;
        private SharedPreferences mSharedPref;

        private ResponseUtils.HttpUpload mHttpUpload;

        private List<CustomEngine> mData;

        public UploadTask(Activity activity) {
            mActivity = activity;
        }

        protected ResponseUtils.HttpUpload doInBackground(Uri... imageUrl) {
            mSharedPref = PreferenceManager.getDefaultSharedPreferences(mActivity);
            mData = CustomEngine.getList(mActivity);

            mHttpUpload = new ResponseUtils.HttpUpload();
            mHttpUpload.siteId = Integer.parseInt(mSharedPref.getString("search_engine_preference", "0"));

            SearchByImageApplication application = (SearchByImageApplication) getApplication();
            InputStream inputStream = application.getImageInputStream();

            if (mSharedPref.getBoolean("resize_image", false)) {
                inputStream = ImageUtils.ResizeImage(inputStream);
            }

            String uploadUri;
            String key;

            HttpUtils.Body body = new HttpUtils.Body();

            CustomEngine item = CustomEngine.getItemById(mHttpUpload.siteId);
            uploadUri = item.getUpload_url();
            key = item.getPost_file_key();

            switch (mHttpUpload.siteId) {
                case SITE_IQDB:
                    Set<String> iqdb_service = mSharedPref.getStringSet("iqdb_service", new HashSet<String>());
                    String[] selected = iqdb_service.toArray(new String[iqdb_service.size()]);

                    for (String aSelected : selected) {
                        body.add("service[]", aSelected);
                    }

                    if (mSharedPref.getBoolean("iqdb_forcegray", false)) {
                        body.add("forcegray", "on");
                    }
                    break;
                case SITE_SAUCENAO:
                    body.add("hide", mSharedPref.getString("saucenao_hide", "0"));
                    body.add("database", mSharedPref.getString("saucenao_database", "999"));
                    break;
                default:
                    if (item.post_text_key.size() > 0) {
                        for (int i = 0; i < item.post_text_key.size(); i++) {
                            body.add(item.post_text_key.get(i), item.post_text_value.get(i));
                        }
                    }
                    break;
            }

            body.add(key, getImageFileName(imageUrl[0]), Utils.streamToCacheFile(mActivity, inputStream, "image.png"));

            try {
                HttpUtils.postForm(uploadUri,
                        new HttpUtils.Header()
                                .add("accept", "*/*")
                                .add("accept-encoding", "deflate")
                                .add("cache-control", "no-cache")
                                .add("connection", "close")
                                .add("user-agent",
                                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.80 Safari/537.36")
                        ,
                        body,
                        new HttpUtils.Callback() {
                            @Override
                            public void onSuccess(String url, int code, InputStream stream) {
                                switch (mHttpUpload.siteId) {
                                    case SITE_GOOGLE:
                                        url = ResponseUtils.getModifiedGoogleUrl(mActivity, url);
                                        break;
                                    case SITE_BAIDU:
                                        try {
                                            url = ResponseUtils.getUrlFromBaiduJSON(mActivity, stream);
                                        } catch (Exception e) {
                                            mHttpUpload.error = new ResponseUtils.ErrorMessage("Message from image.baidu.com:", e.getMessage());
                                        }
                                        break;
                                    case SITE_IQDB:
                                    case SITE_SAUCENAO:
                                        Utils.streamToCacheFile(mActivity, stream, "html", "result.html");
                                        mHttpUpload.html = mActivity.getCacheDir().getAbsolutePath() + "/" + "html" + "/" + "result.html";
                                        break;
                                }

                                mHttpUpload.url = url;
                            }

                            @Override
                            public void onFail(int code) {
                                mHttpUpload.error = new ResponseUtils.ErrorMessage(getString(R.string.socket_exception), Integer.toString(code));
                            }

                            @Override
                            public void onRetry(int retry) {
                                publishProgress(retry);
                            }
                        }
                );
            } catch (UnknownHostException e) {
                e.printStackTrace();
                mHttpUpload.error = new ResponseUtils.ErrorMessage(e, mActivity.getString(R.string.unknown_host_exception));
            } catch (SocketTimeoutException e) {
                e.printStackTrace();
                mHttpUpload.error = new ResponseUtils.ErrorMessage(e, mActivity.getString(R.string.timeout_exception));
            } catch (IOException e) {
                e.printStackTrace();
                mHttpUpload.error = new ResponseUtils.ErrorMessage(e, mActivity.getString(R.string.socket_exception));
            }

            return mHttpUpload;
        }

        private void dismissDialog() {
            if (UploadActivity.this.isFinishing()) {
                return;
            }

            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
        }

        protected void onProgressUpdate(Integer... values) {
            Toast.makeText(mActivity, "Retry: " + Integer.toString(values[0]), Toast.LENGTH_SHORT).show();
            mProgressDialog.setMessage("Retrying: " + Integer.toString(values[0]));
        }

        protected void onPostExecute(ResponseUtils.HttpUpload result) {
            if (result.error != null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                builder.setMessage(result.error.message);
                builder.setTitle(result.error.title);
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                    }
                });

                builder.show();

                dismissDialog();
                return;
            }

            CustomEngine item = CustomEngine.getItemById(mHttpUpload.siteId);
            switch (item.getResult_open_action()) {
                case CustomEngine.RESULT_OPEN_ACTION.BUILD_IN_IQDB:
                    ResponseUtils.openIqdbResult(mActivity, result);
                    break;
                case CustomEngine.RESULT_OPEN_ACTION.OPEN_HTML_FILE:
                    ResponseUtils.openHTMLinWebView(mActivity, result);
                    break;
                case CustomEngine.RESULT_OPEN_ACTION.DEFAULT:
                    ResponseUtils.openURL(mActivity, result);
                    break;
            }

            dismissDialog();
            finish();
        }
    }

    UploadTask mUploadTask;
    ProgressDialog mProgressDialog;

    public static final String EXTRA_URI =
            "rikka.searchbyimage.ui.UploadActivity.EXTRA_URI";

    private static final int REQUEST_CODE_READ_EXTERNAL_STORAGE = 0;

    private Intent mIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_upload);

        mIntent = getIntent();
        String action = mIntent.getAction();
        String type = mIntent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.startsWith("image/") && mIntent.getParcelableExtra(Intent.EXTRA_STREAM) != null) {
                try {
                    getContentResolver().openInputStream((Uri) mIntent.getParcelableExtra(Intent.EXTRA_STREAM));

                    handleSendImage(mIntent);
                } catch (FileNotFoundException e) {
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.permission_require)
                            .setMessage(R.string.permission_require_detail)
                            .setPositiveButton(R.string.get_permission, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    getPermission(Manifest.permission.READ_EXTERNAL_STORAGE,
                                            REQUEST_CODE_READ_EXTERNAL_STORAGE);
                                }
                            })
                            .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    finish();
                                }
                            })
                            .show();
                }
            }
        }

        if (mIntent.hasExtra(EXTRA_URI)) {
            handleSendImageUri((Uri) mIntent.getParcelableExtra(EXTRA_URI));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_READ_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    handleSendImage(mIntent);
                } else {
                    finish();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void handleSendImage(Intent intent) {
        SearchByImageApplication application = (SearchByImageApplication) getApplication();
        try {
            application.setImageInputStream(getContentResolver().openInputStream((Uri) mIntent.getParcelableExtra(Intent.EXTRA_STREAM)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();

            finish();
        }

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        if (sharedPref.getBoolean("setting_each_time", true)) {
            Intent newIntent = new Intent(this, PopupSettingsActivity.class);
            newIntent.putExtra(PopupSettingsActivity.EXTRA_URI, mIntent.getParcelableExtra(Intent.EXTRA_STREAM));
            startActivity(newIntent);

            finish();
        } else {
            handleSendImageUri((Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM));
        }
    }

    private void handleSendImageUri(Uri uri) {
        mProgressDialog = showDialog();
        mUploadTask = (UploadTask) new UploadTask(this).execute(uri);
    }

    private ProgressDialog showDialog() {
        ProgressDialog progressDialog;

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP || Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP_MR1)
            progressDialog = new ProgressDialog(this, R.style.DialogStyle);
        else
            progressDialog = new ProgressDialog(this);

        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage(getString(R.string.uploading));

        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                mUploadTask.cancel(true);
                finish();
            }
        });

        progressDialog.show();

        return progressDialog;
    }

    private static String getImageFileName(Uri uri) {
        String decodeUri = "/image";
        try {
            decodeUri = URLDecoder.decode(uri.toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        int last = decodeUri.lastIndexOf("/");
        String fileName = decodeUri.substring(last + 1);
        if (!fileName.contains(".")) {
            fileName += ".jpg";
        }
        return fileName;
    }

    private void getPermission(String permission, int requestCode)
    {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
        }
    }

}
