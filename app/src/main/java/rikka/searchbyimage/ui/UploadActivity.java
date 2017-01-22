package rikka.searchbyimage.ui;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.util.Pair;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.content.res.AppCompatResources;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import rikka.searchbyimage.R;
import rikka.searchbyimage.service.UploadParam;
import rikka.searchbyimage.service.UploadService;
import rikka.searchbyimage.staticdata.SearchEngine;
import rikka.searchbyimage.support.Settings;
import rikka.searchbyimage.utils.FilenameResolver;
import rikka.searchbyimage.utils.UploadResultUtils;
import rikka.searchbyimage.utils.Utils;
import rikka.searchbyimage.widget.ListBottomSheetDialog;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static rikka.searchbyimage.staticdata.EngineId.SITE_IQDB;
import static rikka.searchbyimage.staticdata.EngineId.SITE_SAUCENAO;

public class UploadActivity extends BaseActivity {

    public static final String EXTRA_URI =
            "rikka.searchbyimage.ui.UploadActivity.EXTRA_URI";

    private final class UploadServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("Service", "onServiceConnected");
            sUploadBinder = (UploadService.UploadBinder) service;

            if (mUploadParam != null) {
                sUploadBinder.addTask(mUploadParam, mFileToUpload.getName());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("Service", "onServiceDisconnected");

            sUploadBinder = null;
        }
    }

    private UploadServiceConnection mServiceConnection = new UploadServiceConnection();
    private static UploadService.UploadBinder sUploadBinder;

    private Uri mUri;

    private BottomSheetBehavior mBottomSheetBehavior;

    private ImageView mImageView;
    private View mProgressContainer;
    private View mProgress;
    private ImageView mProgressIcon;
    private TextView mProgressText;

    private TextView mButton1;
    private TextView mButton2;
    private TextView mButton3;

    private boolean mOpenSettings;

    private File mFileToUpload;
    private String mFilename;
    private UploadParam mUploadParam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        Log.d("UploadActivity", "onCreate");

        mBottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottom_sheet));
        mBottomSheetBehavior.setHideable(true);
        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    finish();
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        ((View) findViewById(R.id.bottom_sheet).getParent()).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        });

        mButton1 = (TextView) findViewById(android.R.id.button1);
        mButton2 = (TextView) findViewById(android.R.id.button2);
        mButton3 = (TextView) findViewById(android.R.id.button3);

        mOpenSettings = Settings.instance(this).getBoolean(Settings.SETTINGS_EVERY_TIME, false);
        setButtons(false);

        mProgressContainer = findViewById(android.R.id.progress);
        mProgress = findViewById(android.R.id.icon1);
        mProgressIcon = (ImageView) findViewById(android.R.id.icon2);
        mProgressText = (TextView) findViewById(android.R.id.text1);

        mImageView = (ImageView) findViewById(android.R.id.icon);
        mImageView.post(new Runnable() {
            @Override
            public void run() {
                mImageView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (mImageView.getWidth() * 0.5625)));
                setBottomSheetPeekHeight();
            }
        });

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.startsWith("image/") && intent.getParcelableExtra(Intent.EXTRA_STREAM) != null) {
                mUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
            }
        } else if (intent.hasExtra(EXTRA_URI)) {
            mUri = intent.getParcelableExtra(EXTRA_URI);
        }

        if (mUri == null) {
            finish();
        }

        saveImage(mUri);
    }

    private void setBottomSheetPeekHeight() {
        getWindow().getDecorView().post(new Runnable() {
            @Override
            public void run() {
                mBottomSheetBehavior.setPeekHeight(findViewById(R.id.bottom_sheet).getHeight());
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
    }

    @Nullable
    private Drawable getDrawableAndSetTint(@DrawableRes int resId) {
        Drawable icon = AppCompatResources.getDrawable(this, resId);
        if (icon != null) {
            DrawableCompat.setTintList(icon, AppCompatResources.getColorStateList(this, R.color.primary_text));
        }

        return icon;
    }

    private void setSearchEngineButton(int id) {
        SearchEngine item = SearchEngine.getItemById(id);
        Drawable icon = null;
        if (item == null) {
            mButton2.setText("选择搜索引擎");
        } else {
            mButton2.setText(item.getName());
            if (id < SearchEngine.SITE_CUSTOM_START) {
                icon = getDrawableAndSetTint(SearchEngine.BUILD_IN_ENGINE_ICONS[id]);
            } else {
                icon = getDrawableAndSetTint(SearchEngine.DEFAULT_ENGINE_ICON);
            }
        }

        TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(mButton2,
                icon, null, null, null);
    }

    private void setButtons(boolean uploading) {

        if (!uploading) {
            if (mOpenSettings) {
                mButton1.setEnabled(false);
            } else {
                mButton1.setVisibility(View.GONE);
                mButton2.setVisibility(View.GONE);
                mButton3.setVisibility(View.GONE);
                return;
            }

            TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(mButton1,
                    getDrawableAndSetTint(R.drawable.ic_file_upload_24dp), null, null, null);
            mButton1.setText("上传");
            mButton1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startUpload();
                }
            });

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            int id = Integer.parseInt(preferences.getString("search_engine_preference", "0"));
            setSearchEngineButton(id);
            mButton2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final List<Integer> id = new ArrayList<>();
                    List<Integer> icons = new ArrayList<>();
                    List<CharSequence> texts = new ArrayList<>();
                    for (SearchEngine item : SearchEngine.getList(v.getContext())) {
                        texts.add(item.getName());
                        id.add(item.getId());
                        if (item.getId() < SearchEngine.SITE_CUSTOM_START) {
                            icons.add(SearchEngine.BUILD_IN_ENGINE_ICONS[item.getId()]);
                        } else {
                            icons.add(SearchEngine.DEFAULT_ENGINE_ICON);
                        }
                    }

                    new ListBottomSheetDialog.Builder(v.getContext())
                            .setItems(texts)
                            .setIcons(icons)
                            .setOnClickListener(new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    setSearchEngineButton(id.get(which));

                                    dialog.dismiss();
                                }
                            })
                            .show();
                }
            });

            TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(mButton3,
                    getDrawableAndSetTint(R.drawable.ic_settings_24dp), null, null, null);
            mButton3.setText("其他设置");
            mButton3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), MainActivity.class);
                    intent.putExtra(MainActivity.EXTRA_MINI, true);
                    startActivity(intent);
                }
            });
        } else {
            setBottomSheetPeekHeight();

            mButton1.setVisibility(View.VISIBLE);
            mButton2.setVisibility(View.GONE);
            mButton3.setVisibility(View.VISIBLE);

            TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(mButton1,
                    getDrawableAndSetTint(R.drawable.ic_all_out_24dp), null, null, null);
            mButton1.setText("后台运行");
            mButton1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });

            TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(mButton3,
                    getDrawableAndSetTint(R.drawable.ic_clear_24dp), null, null, null);
            mButton3.setText("取消上传");
            mButton3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (sUploadBinder != null) {
                        sUploadBinder.cancelTask(mFileToUpload.getName());
                    }
                    finish();
                }
            });
        }
    }

    private void setProgress(@StringRes int text, @DrawableRes int icon) {
        setProgress(getString(text), icon);
    }

    private void setProgress(String text, @DrawableRes int icon) {
        if (text == null) {
            mProgressContainer.setVisibility(View.GONE);
            return;
        }

        mProgressContainer.setVisibility(View.VISIBLE);
        mProgressText.setText(text);

        if (icon != 0) {
            mProgress.setVisibility(View.GONE);
            mProgressIcon.setVisibility(View.VISIBLE);
            mProgressIcon.setImageDrawable(AppCompatResources.getDrawable(this, icon));
        } else {
            mProgress.setVisibility(View.VISIBLE);
            mProgressIcon.setVisibility(View.GONE);
        }
    }

    private void saveImage(Uri uri) {
        Observable.just(uri)
                .map(new Func1<Uri, File>() {
                    @Override
                    public File call(Uri uri) {
                        File path = getExternalCacheDir();
                        if (path == null) {
                            path = getFilesDir();
                        }

                        try {
                            String time = Long.toString(System.currentTimeMillis());
                            String filename = FilenameResolver.query(getContentResolver(), uri);
                            mFilename = TextUtils.isEmpty(filename) ? time : filename;

                            return Utils.streamToFile(
                                    getContentResolver().openInputStream(uri),
                                    path + "/images/" + time);
                        } catch (Exception e) {
                            return null;
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        setProgress("获取图片中", 0);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<File>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (isFinishing()) {
                            return;
                        }

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            setProgress("需要权限", R.drawable.ic_security_24dp);

                            new RxPermissions(UploadActivity.this)
                                    .request(Manifest.permission.READ_EXTERNAL_STORAGE)
                                    .subscribe(new Action1<Boolean>() {
                                        @Override
                                        public void call(Boolean granted) {
                                            if (granted) {
                                                saveImage(mUri);
                                            } else {
                                                setProgress("被拒绝", R.drawable.ic_error_24dp);
                                            }
                                        }
                                    });
                        } else {
                            setProgress("获取图片出错", R.drawable.ic_error_24dp);
                        }
                    }

                    @Override
                    public void onNext(final File file) {
                        if (file == null) {
                            onError(null);
                            return;
                        }

                        if (isFinishing()) {
                            return;
                        }

                        onFileReady(file);

                        Glide.with(UploadActivity.this)
                                .load(file)
                                .crossFade()
                                .listener(new RequestListener<File, GlideDrawable>() {
                                    @Override
                                    public boolean onException(Exception e, File model, Target<GlideDrawable> target, boolean isFirstResource) {
                                        setProgress("获取图片出错", R.drawable.ic_error_24dp);

                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(GlideDrawable resource, File model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                        if (!mOpenSettings) {
                                            mProgressContainer.setAlpha(0);
                                            mProgressContainer.setBackgroundColor(0x88000000);
                                            ObjectAnimator animator = ObjectAnimator.ofFloat(mProgressContainer, View.ALPHA, 0f, 1f);
                                            animator.setDuration(300);
                                            animator.setInterpolator(new AccelerateInterpolator());
                                            animator.start();
                                        }

                                        return false;
                                    }
                                })
                                .into(mImageView);
                    }
                });
    }

    public void onFileReady(File file) {
        mFileToUpload = file;

        setProgress(null, 0);

        if (!mOpenSettings) {
            startUpload();
        } else {
            mButton1.setEnabled(true);
        }
    }

    public void startUpload() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int id = Integer.parseInt(preferences.getString("search_engine_preference", "0"));
        SearchEngine item = SearchEngine.getItemById(id);
        if (item == null) {
            setProgress("不存在的引擎", R.drawable.ic_error_24dp);
            return;
        }

        if (mOpenSettings) {
            mProgressContainer.setAlpha(0);
            mProgressContainer.setBackgroundColor(ContextCompat.getColor(this, R.color.upload_image_mask));
            ObjectAnimator animator = ObjectAnimator.ofFloat(mProgressContainer, View.ALPHA, 0f, 1f);
            animator.setDuration(300);
            animator.setInterpolator(new AccelerateInterpolator());
            animator.start();
        }

        mUploadParam = getUploadParam(item);
        setProgress("上传中..", 0);
        startService();
    }

    private UploadParam getUploadParam(SearchEngine item) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        List<Pair<String, String>> body = new ArrayList<>();
        switch (item.getId()) {
            case SITE_IQDB:
                Set<String> iqdb_service = preferences.getStringSet("iqdb_service", new HashSet<String>());
                String[] selected = iqdb_service.toArray(new String[iqdb_service.size()]);

                for (String aSelected : selected) {
                    body.add(new Pair<>("service[]", aSelected));
                }

                if (preferences.getBoolean("iqdb_forcegray", false)) {
                    body.add(new Pair<>("forcegray", "on"));
                }
                break;
            case SITE_SAUCENAO:
                body.add(new Pair<>("hide", preferences.getString("saucenao_hide", "0")));
                body.add(new Pair<>("database", preferences.getString("saucenao_database", "999")));
                break;
            default:
                if (item.post_text_key.size() > 0) {
                    for (int i = 0; i < item.post_text_key.size(); i++) {
                        body.add(new Pair<>(item.post_text_key.get(i), item.post_text_value.get(i)));
                    }
                }
                break;
        }

        return new UploadParam(
                item.getId(),
                0,
                mFileToUpload.getAbsolutePath(),
                mFilename,
                item.getUploadUrl(),
                item.getPostFileKey(),
                body,
                new ArrayList<Pair<String, String>>(),
                item.getResultOpenAction());
    }

    private void startService() {
        setButtons(true);

        Log.d("UploadActivity", "startService");

        Intent upload = new Intent(this, UploadService.class);
        Log.d("Service", "bindService");
        getApplicationContext().bindService(upload, mServiceConnection, Service.BIND_AUTO_CREATE);

        if (sUploadBinder != null) {
            sUploadBinder.addTask(mUploadParam, mFileToUpload.getName());
        }
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(getClass().getSimpleName(), "onReceive");

            if (mFileToUpload == null
                    || !intent.getStringExtra(UploadService.EXTRA_KEY).equals(mFileToUpload.getName())) {
                return;
            }

            UploadResultUtils.handleResult(context, intent, false);

            if (!isFinishing()) {
                finish();
            }
        }
    };

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UploadService.INTENT_ACTION_RESULT);
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, intentFilter);

        Log.d("UploadActivity", "registerReceiver");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);

        Log.d("UploadActivity", "unregisterReceiver");
    }
}