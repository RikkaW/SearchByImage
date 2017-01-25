package rikka.searchbyimage.ui;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import rikka.searchbyimage.BuildConfig;
import rikka.searchbyimage.R;
import rikka.searchbyimage.support.Settings;
import rikka.searchbyimage.utils.IntentUtils;

import static rikka.searchbyimage.support.GetDeviceInfo.getAppInfo;


public class MainActivity extends BaseActivity {

    public static final String EXTRA_MINI =
            "rikka.searchbyimage.ui.MainActivity.EXTRA_MINI";

    public static final String ACTION_UPLOAD =
            BuildConfig.APPLICATION_ID + ".intent.action.NEW_UPLOAD";

    private boolean mPaused;
    private String mAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAction = getIntent().getAction();
        if (mAction != null && mAction.equals(ACTION_UPLOAD)) {
            selectImage();
            return;
        }

        setContentView(R.layout.activity_main);

        getWindow().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.background)));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            SettingsFragment fragment = new SettingsFragment();
            Bundle bundle = new Bundle();
            bundle.putBoolean(SettingsFragment.ARG_POPUP, getIntent().getBooleanExtra(EXTRA_MINI, false));
            fragment.setArguments(bundle);

            getSupportFragmentManager().beginTransaction().replace(R.id.settings_container,
                    fragment).commit();
        }

        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        if (getIntent().getBooleanExtra(EXTRA_MINI, false)) {
            findViewById(R.id.fab).setVisibility(View.GONE);

            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle(R.string.settings);
            }
        }

        if ((Settings.instance(this).getInt(Settings.SUCCESSFULLY_UPLOAD_COUNT, 0) >= 3
                && Settings.instance(this).getBoolean(Settings.HIDE_DONATE_REQUEST, false))
                || BuildConfig.DEBUG) {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.donate_request)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new AlertDialog.Builder(MainActivity.this)
                                    .setMessage(R.string.donate_request_1_message)
                                    .setPositiveButton(R.string.donate_request_1_yes, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            startActivity(new Intent(MainActivity.this, DonateActivity.class));
                                        }
                                    })
                                    .setNegativeButton(R.string.donate_request_1_no, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Settings.instance(getApplicationContext()).putBoolean(Settings.HIDE_DONATE_REQUEST, true);
                                        }
                                    })
                                    .setCancelable(false)
                                    .show();
                        }
                    })
                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle(R.string.donate_request_2_message)
                                    .setItems(new CharSequence[]{
                                            getString(R.string.not_like_1),
                                            getString(R.string.not_like_2),
                                            getString(R.string.not_like_3),
                                    }, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case 0:
                                                    new AlertDialog.Builder(MainActivity.this)
                                                            .setMessage(R.string.not_like_1_message)
                                                            .setPositiveButton(android.R.string.ok, null)
                                                            .show();
                                                    break;
                                                case 1:
                                                    new AlertDialog.Builder(MainActivity.this)
                                                            .setMessage(R.string.not_like_2_message)
                                                            .setPositiveButton(R.string.send_feedback, new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    sendFeedback();                                                        }
                                                            })
                                                            .setNegativeButton(android.R.string.cancel, null)
                                                            .show();
                                                    break;
                                                case 2:
                                                    sendFeedback();
                                                    break;
                                            }
                                        }
                                    })
                                    .show();
                        }
                    })
                    .setCancelable(false)
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            Settings.instance(getApplicationContext()).putBoolean(Settings.HIDE_DONATE_REQUEST, true);
                        }
                    })
                    .show();
        }
    }

    private void sendFeedback() {
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "rikkanyaaa+imageSearchFeedback@gmail.moe", null));
        intent.putExtra(Intent.EXTRA_CC, new String[]{"xmu.miffy+imageSearchFeedback@gmail.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "SearchByImage Feedback");
        intent.putExtra(Intent.EXTRA_TEXT, getAppInfo(this).toString());
        IntentUtils.startOtherActivity(this, intent);
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        } else {
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
        }
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, 1);
        } else {
            Toast.makeText(MainActivity.this, R.string.target_app_not_found, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        mPaused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mPaused &&
                mAction != null && mAction.equals(ACTION_UPLOAD)) {
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                Uri uri = data.getData();
                Intent intent = new Intent(this, UploadActivity.class);
                intent.putExtra(UploadActivity.EXTRA_URI, uri);
                startActivity(intent);
            }
        }
    }
}