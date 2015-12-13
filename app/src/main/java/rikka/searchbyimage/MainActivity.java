package rikka.searchbyimage;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    private class UploadTask extends AsyncTask<Uri, Void, String> {
        private Context mContext;

        public UploadTask(Context context) {
            mContext = context;
        }

        protected String doInBackground(Uri... imageUrl) {
            HttpUploadFile httpUploadFile = new HttpUploadFile();

            return httpUploadFile.Upload("http://www.google.com/searchbyimage/upload", "QAQQQQ", getImageUrlWithAuthority(mContext, imageUrl[0]));

        }


        protected void onPostExecute(String result) {
            mProgressDialog.cancel();

            if (!result.equals("")) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(result));
                startActivity(intent);
            } else {
                Toast.makeText(MainActivity.this, "failed?", Toast.LENGTH_LONG).show();
            }

            finish();
        }
    }

    View mView;
    UploadTask mUploadTask;
    ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mView = findViewById(R.id.view);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "什么也没有 OAQ", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

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
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(true);
        progressDialog.setMessage("Uploading...");

        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                mUploadTask.cancel(true);
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

    @Override
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
    }

    private static String getImageUrlWithAuthority(Context context, Uri uri) {
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
                    Log.e("在保存图片时出错：", e.toString());
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
}
