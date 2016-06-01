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
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.io.FileNotFoundException;
import java.util.Locale;

import rikka.searchbyimage.R;
import rikka.searchbyimage.service.UploadService;
import rikka.searchbyimage.staticdata.StaticData;
import rikka.searchbyimage.support.Settings;
import rikka.searchbyimage.utils.UriUtils;

public class UploadActivity extends BaseActivity implements UriUtils.StoreImageFileListener {

    public static final String EXTRA_URI =
            "rikka.searchbyimage.ui.UploadActivity.EXTRA_URI";

    public static final String EXTRA_SAVE_FILE =
            "rikka.searchbyimage.ui.UploadActivity.EXTRA_SAVE_FILE";

    private UploadService.UploadBinder uploadBinder;

    // if activity has paused, and not uploading image when resume or not show error, finish it
    private boolean paused = false;
    private boolean isError = false;
    private boolean requestingPermission = false;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            uploadBinder = (UploadService.UploadBinder) service;
            Log.d("UploadActivity", "onServiceConnected");

            if (StaticData.instance().fileIsReady) {
                uploadBinder.startUpload();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceConnection = null;
        }
    };

    private ProgressDialog mProgressDialog;

    private Uri mUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("UploadActivity", "onCreate");

        Settings.instance(this)
                .putBoolean(Settings.DOWNLOAD_FILE_CRASH, false);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        boolean shouldSaveFile = false;
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.startsWith("image/") && intent.getParcelableExtra(Intent.EXTRA_STREAM) != null) {
                mUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                shouldSaveFile = true;
            }
        } else if (intent.hasExtra(EXTRA_URI)) {
            mUri = intent.getParcelableExtra(EXTRA_URI);
        }

        if (mUri == null) {
            finish();
            return;
        }

        if (shouldSaveFile || intent.getBooleanExtra(EXTRA_SAVE_FILE, false)) {
            if (checkPermission(mUri)) {
                StaticData.instance().fileIsReady = false;
                UriUtils.storageImageFileAsync(this, mUri, this);
                requestingPermission = false;
                Log.d("UploadActivity", "storageImageFileAsync");
            } else {
                return;
            }
        }

        postStartStorageFile();
    }

    private void postStartStorageFile() {
        Intent intent = getIntent();

        boolean shouldCheckOpenSetting = true;
        if (intent.hasExtra(EXTRA_SAVE_FILE)) {
            shouldCheckOpenSetting = false;
        }

        if (shouldCheckOpenSetting && Settings.instance(this).getBoolean("setting_each_time", true)) {
            Intent newIntent = new Intent(this, PopupSettingsActivity.class);
            newIntent.putExtra(PopupSettingsActivity.EXTRA_URI, mUri);

            startActivity(newIntent);
            finish();
        } else {
            startService();
        }
    }

    @Override
    public void onFinish(Uri uri) {
        Log.d("UploadActivity", "storageImageFile onFinish");
        requestingPermission = false;
        StaticData.instance().fileIsReady = true;

        if (uploadBinder != null && !uploadBinder.isUploading()) {
            uploadBinder.startUpload();
        }
    }

    private boolean checkPermission(final Uri uri) {
        try {
            getContentResolver().openInputStream(uri);
            return true;
        } catch (FileNotFoundException | SecurityException ignored) {
            requestingPermission = true;

            new AlertDialog.Builder(this)
                    .setTitle(R.string.permission_require)
                    .setMessage(R.string.permission_require_detail)
                    .setPositiveButton(R.string.get_permission, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (ContextCompat.checkSelfPermission(UploadActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(UploadActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
                            }
                        }
                    })
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            finish();
                        }
                    })
                    .show();

            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 0:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    StaticData.instance().fileIsReady = false;
                    UriUtils.storageImageFileAsync(this, mUri, this);
                    postStartStorageFile();
                } else {
                    finish();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void startService() {
        Log.d("UploadActivity", "startService");
        getWindow().getDecorView().postDelayed(new Runnable() {
            @Override
            public void run() {
                mProgressDialog = showDialog();
            }
        }, 10);

        Intent upload = new Intent(this, UploadService.class);
        bindService(upload, serviceConnection, Service.BIND_AUTO_CREATE);
    }

    private ProgressDialog showDialog() {
        Log.d("UploadActivity", "showDialog");

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
            try {
                unbindService(serviceConnection);
            } catch (IllegalArgumentException ignore) {
            }
            serviceConnection = null;
        }
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(getClass().getSimpleName(), "onReceive");

            if (mProgressDialog == null) {
                return;
            }

            switch (intent.getAction()) {
                case UploadService.INTENT_ACTION_RETRY:
                    int times = intent.getIntExtra(UploadService.INTENT_RETRY_TIMES, 0);
                    mProgressDialog.setMessage(String.format(Locale.getDefault(), "Retrying: %d times", times));
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
                    new AlertDialog.Builder(UploadActivity.this)
                            .setMessage(message)
                            .setTitle(title)
                            .setPositiveButton(android.R.string.ok, null)
                            .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
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

        Log.d("UploadActivity", "registerReceiver");
        if (uploadBinder != null && uploadBinder.isUploading() && mProgressDialog != null) {
            mProgressDialog.show();
        } else if (paused && !isError && !requestingPermission) {
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

        paused = true;
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void finish() {
        super.finish();
        Log.d("UploadActivity", "finish");
    }
}


/*RxPermissions
                                    .getInstance(UploadActivity.this)
                                    .request(Manifest.permission.READ_EXTERNAL_STORAGE)
                                    .subscribe(new Action1<Boolean>() {
                                        @Override
                                        public void call(Boolean granted) {
                                            if (granted) {
                                                storageImageFile(uri);
                                            }
                                        }
                                    });*/