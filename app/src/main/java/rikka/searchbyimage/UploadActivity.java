package rikka.searchbyimage;

import android.Manifest;
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
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import rikka.searchbyimage.utils.HttpRequestUtils;

public class UploadActivity extends AppCompatActivity {

    private class HttpUpload {
        public String url;
        public String html;
        public String uploadUrl;

        HttpUpload() {

        }

        HttpUpload(String uploadUrl, String url, String html) {
            this.url = url;
            this.uploadUrl = uploadUrl;
            this.html = html;
        }
    }

    private class UploadTask extends AsyncTask<Uri, Void, HttpUpload> {


        public final static int SITE_GOOGLE = 0;
        public final static int SITE_BAIDU = 1;
        public final static int SITE_IQDB = 2;

        private Context mContext;

        public UploadTask(Context context) {
            mContext = context;
        }

        protected HttpUpload doInBackground(Uri... imageUrl) {

            String uploadUri = null;
            String name = null;
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);

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
                }
            }


            HttpRequestUtils httpRequest = new HttpRequestUtils(uploadUri, "POST");
            String responseUri = "";

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
                httpRequest.addFormData(name, getImageFileName(imageUrl[0]), mContext.getContentResolver().openInputStream(imageUrl[0]));
                responseUri = httpRequest.getResponseUri(mContext);

                if (!responseUri.startsWith("http")) {
                    return new HttpUpload(uploadUri, responseUri, null);
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
                    int end =  responseUri.indexOf("/", start);

                    String googleUri =  "www.google.com";
                    
                    if (customRedirect) {
                        googleUri = sharedPref.getString("google_region", googleUri);
                    }

                    responseUri = responseUri.substring(0, start) + googleUri + responseUri.substring(end);
                }
            } catch (IOException e) {
                e.printStackTrace();

                for (StackTraceElement stackTraceElement:
                        e.getStackTrace()) {
                    if (stackTraceElement.getFileName().startsWith("HttpRequestUtil"))
                        responseUri = "Error: " + e.toString() +"\nFile: " + stackTraceElement.getFileName() + " (" + stackTraceElement.getLineNumber() + ")";
                }
            }

            return new HttpUpload(uploadUri, responseUri, httpRequest.getHtml());
        }

        protected void onPostExecute(HttpUpload result) {
            mProgressDialog.cancel();

            if (result.url.startsWith("http")) {

                if (result.url.equals(result.uploadUrl)) {
                    // for iqdb.org
                    Intent intent = new Intent(getBaseContext(), WebViewActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("EXTRA_INPUT", result.html);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(result.url));
                    startActivity(intent);
                }

                finish();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setMessage(result.url);
                builder.setTitle("出错了 OAO");
                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                    }
                });

                builder.show();
            }
        }
    }

    UploadTask mUploadTask;
    ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_upload);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                handleSendImage(intent); // Handle single image being sent
            }
        }
    }

    private ProgressDialog showDialog() {
        ProgressDialog progressDialog;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            progressDialog = new ProgressDialog(this, R.style.DialogStyle);
        else
            progressDialog = new ProgressDialog(this);

        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(true);
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

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/

    private String getImageUrlWithAuthority(Context context, Uri uri) {
        InputStream is = null;
        OutputStream os = null;
        File file = null;
        if (uri.getAuthority() != null) {
            try {
                is = context.getContentResolver().openInputStream(uri);
                String RootPath = context.getCacheDir().getAbsolutePath();
                String FilePath = RootPath + "/image/" + getImageFileName(uri);
                file = new File(FilePath);
                if (!file.getParentFile().exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    file.getParentFile().mkdirs();
                }
                try {
                    //noinspection ResultOfMethodCallIgnored
                    file.createNewFile();
                } catch (IOException e) {
                    Log.e("在保存图片时出错: ", e.toString());
                    Toast.makeText(context, "在保存图片时出错:\n" + e.toString(), Toast.LENGTH_LONG).show();
                }
                os = new FileOutputStream(file);
                byte[] buf = new byte[1024];
                int len;
                while ((len = is != null ? is.read(buf) : 0) > 0) {
                    os.write(buf, 0, len);
                }
                os.flush();

            } catch (IOException e) {
                e.printStackTrace();
                if (e instanceof FileNotFoundException) {
                    //ask for permission and do it again
                    getPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
                    Toast.makeText(context, "FileNotFoundException" + e.toString(), Toast.LENGTH_LONG).show();
                }
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    if (os != null) {
                        os.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return file.getAbsolutePath();
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
