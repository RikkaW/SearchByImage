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
import android.util.JsonReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

import rikka.searchbyimage.R;
import rikka.searchbyimage.SearchByImageApplication;
import rikka.searchbyimage.utils.HttpRequestUtils;
import rikka.searchbyimage.utils.ImageUtils;
import rikka.searchbyimage.utils.URLUtils;

public class UploadActivity extends AppCompatActivity {
    public final static int SITE_GOOGLE = 0;
    public final static int SITE_BAIDU = 1;
    public final static int SITE_IQDB = 2;
    public final static int SITE_TINEYE = 3;
    public final static int SITE_SAUCENAO = 4;
    public final static int SITE_ASCII2D = 5;

    private class Error {
        public String title;
        public String message;

        Error(String title, String message) {
            this.title = title;
            this.message = message;
        }
    }

    private class HttpUpload {
        public String url;
        public String html;
        public String uploadUrl;
        public int siteId;
        public Error error;

        HttpUpload(Error error) {
            this.error = error;
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
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mActivity);

            SearchByImageApplication application = (SearchByImageApplication) getApplication();
            InputStream inputStream = application.getImageInputStream();

            if (sharedPref.getBoolean("resize_image", false)) {
                inputStream = ImageUtils.ResizeImage(inputStream);
            }

            String uploadUri = null;
            String name = null;

            int siteId = Integer.parseInt(sharedPref.getString("search_engine_preference", "0"));
            switch (siteId) {
                case SITE_GOOGLE:
                    uploadUri = "http://www.google.com/searchbyimage/upload";
                    name = "encoded_image";
                    break;
                case SITE_BAIDU:
                    uploadUri = "http://image.baidu.com/pictureup/uploadwise";
                    name = "upload";
                    break;
                case SITE_IQDB:
                    uploadUri = "http://iqdb.org/";
                    name = "file";
                    break;
                case SITE_TINEYE:
                    uploadUri = "http://www.tineye.com/search";
                    name = "image";
                    break;
                case SITE_SAUCENAO:
                    uploadUri = "http://saucenao.com/search.php";
                    name = "file";
                    break;
                case SITE_ASCII2D:
                    uploadUri = "http://www.ascii2d.net/search/file";
                    name = "file";
                    break;
            }

            HttpRequestUtils httpRequest = new HttpRequestUtils(uploadUri, "POST");
            String responseUri;
            switch (siteId) {
                case SITE_IQDB:
                    Set<String> iqdb_service = sharedPref.getStringSet("iqdb_service", new HashSet<String>());
                    String[] selected = iqdb_service.toArray(new String[iqdb_service.size()]);

                    for (String aSelected : selected) {
                        httpRequest.addFormData("service[]", aSelected);
                    }

                    if (sharedPref.getBoolean("iqdb_forcegray", false)) {
                        httpRequest.addFormData("forcegray", "on");
                    }
                    break;
                case SITE_SAUCENAO:
                    httpRequest.addFormData("hide", sharedPref.getString("saucenao_hide", "0"));
                    httpRequest.addFormData("database", sharedPref.getString("saucenao_database", "999"));
                    break;
            }

            try {
                httpRequest.addFormData(name, getImageFileName(imageUrl[0]), inputStream);
                responseUri = httpRequest.getResponseUri(mActivity);

                if (!responseUri.startsWith("http")) {
                    return new HttpUpload(uploadUri, responseUri, null, siteId);
                }

                if (siteId == SITE_GOOGLE) {
                    boolean safeSearch = sharedPref.getBoolean("safe_search_preference", true);
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
            } catch (UnknownHostException e) {
                e.printStackTrace();
                return new HttpUpload(new Error(getString(R.string.unknown_host_exception), getExceptionText(e)));
            } catch (SocketTimeoutException e) {
                e.printStackTrace();
                return new HttpUpload(new Error(getString(R.string.timeout_exception), getExceptionText(e)));
            } catch (IOException e) {
                e.printStackTrace();
                return new HttpUpload(new Error(getString(R.string.socket_exception), getExceptionText(e)));
            }

            return new HttpUpload(uploadUri, responseUri, httpRequest.getHtml(), siteId);
        }

        private String getExceptionText(Exception e) {
            String result = "Error: " + e.toString() + "\nFile: ";

            for (StackTraceElement stackTraceElement :
                    e.getStackTrace()) {
                if (stackTraceElement.getFileName().startsWith("HttpRequestUtil") || stackTraceElement.getFileName().startsWith("UploadActivity"))
                    result += "\n" + stackTraceElement.getFileName() + " (" + stackTraceElement.getLineNumber() + ")";
            }

            return result;
        }

        protected void onPostExecute(HttpUpload result) {
            if (result.url == null) {

                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                builder.setMessage(result.error.message);
                builder.setTitle(result.error.title);
                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                    }
                });

                builder.show();

                mProgressDialog.dismiss();
                return;
            }

            if (result.url.startsWith("http")) {
                Intent intent;

                switch (result.siteId) {
                    case SITE_BAIDU:
                        int err_no = 0;
                        String contsign = "";
                        String obj_url = "";
                        String simid = "";
                        String error_msg = "";

                        JsonReader reader = null;
                        try {
                            reader = new JsonReader(new InputStreamReader(new FileInputStream(new File(result.html))));
                            reader.beginObject();

                            while (reader.hasNext()) {
                                String keyName = reader.nextName();
                                switch (keyName) {
                                    case "errno":
                                        err_no = reader.nextInt();
                                        break;
                                    case "msg":
                                        error_msg = reader.nextString();
                                        break;
                                    case "json_data":
                                        reader.beginObject();
                                        break;
                                    case "contsign":
                                        contsign = reader.nextString();
                                        break;
                                    case "obj_url":
                                        obj_url = reader.nextString();
                                        break;
                                    case "simid":
                                        simid = reader.nextString();
                                        break;
                                    default:
                                        reader.skipValue();
                                        break;
                                }
                            }
                            reader.endObject();
                        } catch (IllegalStateException | IOException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                if (reader != null) {
                                    reader.close();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        if (err_no != 0) {
                            new AlertDialog.Builder(mActivity)
                                    .setTitle("baidu.com")
                                    .setMessage(error_msg)
                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            finish();
                                        }
                                    })
                                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                        @Override
                                        public void onDismiss(DialogInterface dialog) {
                                            finish();
                                        }
                                    })
                                    .show();

                            mProgressDialog.dismiss();

                            return;
                        }

                        StringBuilder sb = new StringBuilder();
                        sb.append("http://image.baidu.com/n/mo_search?guess=1&rn=30&appid=0&tag=1&isMobile=0");
                        sb.append("&queryImageUrl=");
                        sb.append(obj_url);
                        sb.append("&querySign=");
                        sb.append(contsign);
                        sb.append("&simid=");
                        sb.append(simid);
                        result.url = sb.toString();
                    case SITE_GOOGLE:
                    case SITE_TINEYE:
                    case SITE_ASCII2D:
                        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mActivity);
                        switch (sharedPref.getString("show_result_in", URLUtils.SHOW_IN_WEBVIEW)) {
                            case URLUtils.SHOW_IN_WEBVIEW:
                            case URLUtils.SHOW_IN_CHROME:
                                intent = new Intent(mActivity, ChromeCustomTabsActivity.class);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                                } else {
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                }
                                intent.putExtra(ChromeCustomTabsActivity.EXTRA_URL, result.url);
                                intent.putExtra(ChromeCustomTabsActivity.EXTRA_SITE_ID, result.siteId);

                                startActivity(intent);
                                break;
                            case URLUtils.SHOW_IN_BROWSER:
                                URLUtils.OpenBrowser(mActivity, Uri.parse(result.url));
                                break;
                        }

                        break;
                    case SITE_IQDB:
                        intent = new Intent(getApplicationContext(), ResultActivity.class);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                        } else {
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        }
                        intent.putExtra(ResultActivity.EXTRA_FILE, result.html);
                        intent.putExtra(ResultActivity.EXTRA_SITE_ID, result.siteId);

                        startActivity(intent);
                        break;
                    case SITE_SAUCENAO:
                        intent = new Intent(getApplicationContext(), WebViewActivity.class);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                        } else {
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        }
                        intent.putExtra(WebViewActivity.EXTRA_FILE, result.html);
                        intent.putExtra(WebViewActivity.EXTRA_SITE_ID, result.siteId);

                        startActivity(intent);
                        break;
                }

                mProgressDialog.dismiss();
                finish();

            }
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
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
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
