package rikka.searchbyimage.ui;

import android.Manifest;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;

import com.tbruyelle.rxpermissions.RxPermissions;

import java.io.File;
import java.util.Locale;

import rikka.searchbyimage.R;
import rikka.searchbyimage.service.UploadService;
import rikka.searchbyimage.support.Settings;
import rikka.searchbyimage.utils.UriUtils;
import rx.functions.Action1;

public class UploadActivity extends BaseActivity {
    public final static int SITE_GOOGLE = 0;
    public final static int SITE_BAIDU = 1;
    public final static int SITE_IQDB = 2;
    public final static int SITE_TINEYE = 3;
    public final static int SITE_SAUCENAO = 4;
    public final static int SITE_ASCII2D = 5;


    ProgressDialog mProgressDialog;

    public static final String EXTRA_URI =
            "rikka.searchbyimage.ui.UploadActivity.EXTRA_URI";

    public static final String EXTRA_URI2 =
            "rikka.searchbyimage.ui.UploadActivity.EXTRA_URI2";

    private Intent mIntent;

    private UploadService.UploadBinder uploadBinder;

    //if activity has paused,and not uploading image when resume or not show error,finish it
    private boolean paused = false;
    private boolean isError = false;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            uploadBinder = (UploadService.UploadBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_upload);

        Settings.instance(this)
                .putBoolean(Settings.DOWNLOAD_FILE_CRASH, false);

        mIntent = getIntent();
        String action = mIntent.getAction();
        String type = mIntent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.startsWith("image/") && mIntent.getParcelableExtra(Intent.EXTRA_STREAM) != null) {
                handleSendImage(mIntent);
            }
        }

        if (mIntent.hasExtra(EXTRA_URI)) {
            handleSendImageUri((Uri) mIntent.getParcelableExtra(EXTRA_URI));
        }

        if (mIntent.hasExtra(EXTRA_URI2)) {
            handleSendImageUri((Uri) mIntent.getParcelableExtra(EXTRA_URI2));
        }
    }

    private void handleSendImage(Intent intent) {

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

    private void handleSendImageUri(final Uri uri) {
        mProgressDialog = showDialog();
        File file = UriUtils.storageImageShared(this, uri);
        if (file == null) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.permission_require)
                    .setMessage(R.string.permission_require_detail)
                    .setPositiveButton(R.string.get_permission, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            RxPermissions.getInstance(mActivity).request(Manifest.permission.READ_EXTERNAL_STORAGE)
                                    .subscribe(new Action1<Boolean>() {
                                        @Override
                                        public void call(Boolean granted) {
                                            if (granted) {
                                                handleSendImageUri(uri);
                                            }
                                        }
                                    });
                        }
                    })
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            finish();
                        }
                    })
                    .show();
            return;
        }
        Intent upload = new Intent(this, UploadService.class);
        upload.putExtra(UploadService.INTENT_FILE_PATH, file.getPath());
        bindService(upload, serviceConnection, Service.BIND_AUTO_CREATE);
    }

    private ProgressDialog showDialog() {
        ProgressDialog progressDialog;

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP || Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP_MR1)
            progressDialog = new ProgressDialog(this, R.style.ProgressDialog);
        else
            progressDialog = new ProgressDialog(this);

        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage(getString(R.string.uploading));

        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (uploadBinder != null) {
                    uploadBinder.cancel();
                }
                finish();
            }
        });

        progressDialog.show();

        return progressDialog;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceConnection != null) {
            unbindService(serviceConnection);
        }
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mProgressDialog == null) {
                return;
            }
            switch (intent.getAction()) {
                case UploadService.INTENT_ACTION_RETRY:
                    int times = intent.getIntExtra(UploadService.INTENT_RETRY_TIMES, 0);
                    mProgressDialog.setMessage(String.format(Locale.getDefault(), "Retrying:%d times", times));
                    break;
                case UploadService.INTENT_ACTION_SUCCESS:
                    mProgressDialog.dismiss();
                    finish();
                    break;
                case UploadService.INTENT_ACTION_ERROR:
                    mProgressDialog.dismiss();
                    isError = true;
                    String title = intent.getStringExtra(UploadService.INTENT_ERROR_TITLE);
                    String message = intent.getStringExtra(UploadService.INTENT_ERROR_MESSAGE);
                    new AlertDialog.Builder(mActivity)
                            .setMessage(message)
                            .setTitle(title)
                            .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    finish();
                                }
                            }).show();
                default:
                    break;
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UploadService.INTENT_ACTION_ERROR);
        intentFilter.addAction(UploadService.INTENT_ACTION_SUCCESS);
        intentFilter.addAction(UploadService.INTENT_ACTION_RETRY);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);
        if (uploadBinder != null && uploadBinder.isUploading() && mProgressDialog != null) {
            mProgressDialog.show();
        } else if (paused && !isError) {
            finish();
        }
    }

    /**
     * Dispatch onPause() to fragments.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        paused = true;
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }
}
