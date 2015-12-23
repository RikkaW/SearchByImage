package rikka.searchbyimage;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
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
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import rikka.searchbyimage.utils.HttpRequestUtils;
import rikka.searchbyimage.utils.URLUtils;

public class UploadActivity extends AppCompatActivity {
    public final static int SITE_GOOGLE = 0;
    public final static int SITE_BAIDU = 1;
    public final static int SITE_IQDB = 2;
    public final static int SITE_TINEYE = 3;


    private class HttpUpload {
        public String url;
        public String html;
        public String uploadUrl;
        public int siteId;

        HttpUpload() {

        }

        HttpUpload(String uploadUrl, String url, String html, int siteId) {
            this.url = url;
            this.uploadUrl = uploadUrl;
            this.html = html;
            this.siteId = siteId;
        }
    }

    private class UploadTask extends AsyncTask<Uri, Void, HttpUpload> {

        private Activity mActivity;

        public UploadTask(Activity activity) {
            mActivity = activity;
        }

        protected HttpUpload doInBackground(Uri... imageUrl) {

            String uploadUri = null;
            String name = null;
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mActivity);

            int siteId = Integer.parseInt(sharedPref.getString("search_engine_preference", "0"));
            switch (siteId) {
                case SITE_GOOGLE: {
                    uploadUri = "http://www.google.com/searchbyimage/upload";
                    name = "encoded_image";
                    break;
                }
                case SITE_BAIDU: {
                    uploadUri = "http://image.baidu.com/pictureup/uploadshitu";
                    name = "image";
                    break;
                }
                case SITE_IQDB: {
                    uploadUri = "http://iqdb.org/";
                    name = "file";
                    break;
                }

                case SITE_TINEYE: {
                    uploadUri = "http://www.tineye.com/search";
                    name = "image";
                }
            }


            HttpRequestUtils httpRequest = new HttpRequestUtils(uploadUri, "POST");
            String responseUri;

            if (siteId == SITE_IQDB) {
                //httpRequest.addFormData("MAX_FILE_SIZE", "8388608");

                Set<String> iqdb_service = sharedPref.getStringSet("iqdb_service", new HashSet<String>());
                String[] selected = iqdb_service.toArray(new String[iqdb_service.size()]);

                for (String aSelected : selected) {
                    httpRequest.addFormData("service[]", aSelected);
                }

                if (sharedPref.getBoolean("iqdb_forcegray", false)) {
                    httpRequest.addFormData("forcegray", "on");
                }
            }

            try {
                httpRequest.addFormData(name, getImageFileName(imageUrl[0]), mActivity.getContentResolver().openInputStream(imageUrl[0]));
                responseUri = httpRequest.getResponseUri(mActivity);

                if (!responseUri.startsWith("http")) {
                    return new HttpUpload(uploadUri, responseUri, null, siteId);
                }

                if (siteId == SITE_GOOGLE) {
                    boolean safeSearch = sharedPref.getBoolean("safe_search_preference", false);
                    boolean noRedirect = sharedPref.getString("google_region_preference", "0").equals("1");
                    boolean customRedirect = sharedPref.getString("google_region_preference", "0").equals("2");

                    responseUri += safeSearch ? "&safe=active" : "&safe=off";

                    if (noRedirect || customRedirect) {
                        responseUri += "?gws_rd=cr";
                    }

                    int start = responseUri.indexOf("www.google.");
                    int end = responseUri.indexOf("/", start);

                    String googleUri = "www.google.com";

                    if (customRedirect) {
                        googleUri = sharedPref.getString("google_region", googleUri);
                    }

                    responseUri = responseUri.substring(0, start) + googleUri + responseUri.substring(end);
                }
            } catch (FileNotFoundException e) {
                responseUri = null;
            } catch (IOException e) {
                e.printStackTrace();

                responseUri = "Error: " + e.toString() +"\nFile: ";

                for (StackTraceElement stackTraceElement:
                        e.getStackTrace()) {
                    if (stackTraceElement.getFileName().startsWith("HttpRequestUtil") || stackTraceElement.getFileName().startsWith("UploadActivity"))
                        responseUri += "\n" + stackTraceElement.getFileName() + " (" + stackTraceElement.getLineNumber() + ")";
                }
            }

            return new HttpUpload(uploadUri, responseUri, httpRequest.getHtml(), siteId);
        }

        protected void onPostExecute(HttpUpload result) {
            mProgressDialog.cancel();

            if (result.url == null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                builder.setMessage(result.url);
                builder.setTitle(R.string.permission_require);
                builder.setMessage(R.string.permission_require_detail);
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
                    }
                });
                builder.show();
            }

            if (result.url.startsWith("http")) {

                if (result.url.equals(result.uploadUrl)) {
                    Intent intent = new Intent(getApplicationContext(), ResultActivity.class);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                    } else {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    }
                    intent.putExtra(ResultActivity.EXTRA_FILE, result.html);
                    intent.putExtra(ResultActivity.EXTRA_SITE_ID, result.siteId);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(mActivity, ChromeCustomTabsActivity.class);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                    } else {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    }
                    intent.putExtra(ChromeCustomTabsActivity.EXTRA_URL, result.url);
                    intent.putExtra(ChromeCustomTabsActivity.EXTRA_SITE_ID, result.siteId);

                    /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                        List<ActivityManager.AppTask> appTasks = activityManager.getAppTasks();
                        ActivityManager.AppTask appTask = appTasks.get(0);
                        appTask.startActivity(mActivity, intent, null);
                    }
                    else */{
                        startActivity(intent);
                    }

                    //ActivityManager.AppTask.startActivity(mActivity, intent, null);
                    //URLUtils.Open(result.url, mActivity);
                }
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                builder.setMessage(result.url);
                builder.setTitle("出错了 OAO");

                builder.show();
            }
        }
    }

    UploadTask mUploadTask;
    ProgressDialog mProgressDialog;

    public static final String EXTRA_URI =
            "rikka.searchbyimage.UploadActivity.EXTRA_URI";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_upload);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

                if (sharedPref.getBoolean("setting_each_time", true)) {
                    Intent newIntent = new Intent(this, PopupSettingsActivity.class);
                    newIntent.putExtra(PopupSettingsActivity.EXTRA_URI, intent.getParcelableExtra(Intent.EXTRA_STREAM).toString());
                    startActivity(newIntent);

                    finish();
                } else {
                    handleSendImage(intent);
                }
            }
        }

        if (intent.hasExtra(EXTRA_URI)) {
            handleSendImage(intent.getStringExtra(EXTRA_URI));
        }
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

    private void handleSendImage(Intent intent) {
        Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
            mProgressDialog = showDialog();
            //mUploadTask.cancel(true);
            mUploadTask = (UploadTask) new UploadTask(this).execute(imageUri);
        }
    }

    private void handleSendImage(String uri) {
        mProgressDialog = showDialog();
        mUploadTask = (UploadTask) new UploadTask(this).execute(Uri.parse(uri));
    }

    private static String getImageFileName(Uri uri) {
        int last = uri.toString().lastIndexOf("/");
        String fileName = uri.toString().substring(last + 1);
        if (!fileName.contains(".")) {
            fileName += ".jpg";
        }
        return fileName;
    }

    private void getPermission(String permission)
    {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, 0);
        }
    }

}
