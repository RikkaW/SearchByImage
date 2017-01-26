package rikka.searchbyimage.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.net.ConnectivityManagerCompat;
import android.support.v4.util.Pair;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import rikka.searchbyimage.BuildConfig;
import rikka.searchbyimage.R;
import rikka.searchbyimage.staticdata.SearchEngine;
import rikka.searchbyimage.support.OkHttpClientProvider;
import rikka.searchbyimage.support.Settings;
import rikka.searchbyimage.utils.HttpUtils;
import rikka.searchbyimage.utils.ImageUtils;
import rikka.searchbyimage.utils.UploadResultUtils;
import rikka.searchbyimage.utils.Utils;

import static rikka.searchbyimage.staticdata.EngineId.SITE_BAIDU;
import static rikka.searchbyimage.staticdata.EngineId.SITE_GOOGLE;
import static rikka.searchbyimage.staticdata.EngineId.SITE_IQDB;
import static rikka.searchbyimage.staticdata.EngineId.SITE_SAUCENAO;

/**
 * Created by Yulan on 2016/5/28.
 * upload file service
 */

public class UploadService extends Service {

    private static final int NOTIFICATION_ID = 0x10;

    public static final String INTENT_ACTION_RESULT = BuildConfig.APPLICATION_ID + ".intent.action.upload_result";
    public static final String INTENT_ACTION_CANCEL = BuildConfig.APPLICATION_ID + ".intent.action.upload_cancel";
    public static final String EXTRA_RESULT = "EXTRA_RESULT";
    public static final String EXTRA_KEY = "EXTRA_KEY";

    private UploadBinder mUploadBinder = new UploadBinder();

    private Map<String, UploadTask> mTasks = new HashMap<>();

    private class UploadTask extends AsyncTask<UploadParam, Integer, UploadResult> {

        private Context mContext;
        private String mKey;
        private String mFileUri;
        private boolean mCanceled;

        public UploadTask(Context context, String key) {
            mContext = context;
            mKey = key;
        }

        protected UploadResult doInBackground(UploadParam... params) {
            Log.d("UploadService", "doInBackground");

            SearchEngine.getList(mContext);

            UploadParam param = params[0];
            mFileUri = param.getFileUri();
            File image = new File(param.getFileUri());
            if (!image.exists()) {
                return new UploadResult(UploadResult.ERROR_FILE_NOT_FOUND, mContext.getString(R.string.file_not_found), param);
            }

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);

            InputStream inputStream;
            try {
                inputStream = new FileInputStream(image);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return new UploadResult(UploadResult.ERROR_FILE_NOT_FOUND, getString(R.string.file_not_found), param);
            }

            boolean resize = preferences.getBoolean("resize_image", true);

            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (ConnectivityManagerCompat.isActiveNetworkMetered(cm)
                    && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                    && ConnectivityManagerCompat.getRestrictBackgroundStatus(cm) != ConnectivityManagerCompat.RESTRICT_BACKGROUND_STATUS_DISABLED) {
                resize = true;
            }

            byte[] content = null;
            if (resize) {
                try {
                    content = ImageUtils.resizeImage(inputStream, 1024 * 1024); // 1MB
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (content == null) {
                    return new UploadResult(UploadResult.ERROR_FILE_NOT_FOUND, "error when try to resize image", param);
                }
            } else {
                try {
                    byte[] b = new byte[1024];
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    int c;
                    while ((c = inputStream.read(b)) != -1) {
                        os.write(b, 0, c);
                    }
                    content = os.toByteArray();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (content == null) {
                    return new UploadResult(UploadResult.ERROR_FILE_NOT_FOUND, "error when try to resize image", param);
                }
            }

            return upload(param, content);
        }

        private synchronized UploadResult upload(UploadParam param, byte[] content) {
            OkHttpClient okHttpClient = OkHttpClientProvider.get();

            MultipartBody.Builder bodyBuilder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(param.getPostFileKey(), HttpUtils.guessMimeType(param.getFilename()),
                            RequestBody.create(MediaType.parse(HttpUtils.guessMimeType(param.getFilename())), content));

            for (Pair<String, String> pair: param.getBodies()) {
                bodyBuilder.addFormDataPart(pair.first, pair.second);
            }

            Request.Builder builder = new Request.Builder()
                    .url(param.getUrl())
                    .addHeader("accept", "*/*")
                    .addHeader("accept", "*/*")
                    .addHeader("accept-encoding", "deflate")
                    .addHeader("cache-control", "no-cache")
                    .addHeader("connection", "close")
                    .addHeader("host", Uri.parse(param.getUrl()).getHost())
                    .addHeader("user-agent",
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36")
                    .post(bodyBuilder.build());

            Request request = builder.build();
            Response response;
            try {
                response = okHttpClient.newCall(request).execute();
            } catch (UnknownHostException e) {
                return new UploadResult(UploadResult.ERROR_UNKNOWN_HOST, mContext.getString(R.string.unknown_host_exception), param);
            } catch (SocketTimeoutException e) {
                return new UploadResult(UploadResult.ERROR_TIMEOUT, mContext.getString(R.string.timeout_exception), param);
            } catch (IOException e) {
                return new UploadResult(UploadResult.ERROR_IO, mContext.getString(R.string.socket_exception) + "\n" + e.getMessage(), param);
            } catch (Exception e) {
                return new UploadResult(UploadResult.ERROR_UNKNOWN, e.getMessage(), param);
            }

            if (!response.isSuccessful()) {
                return new UploadResult(UploadResult.ERROR_IO, mContext.getString(R.string.socket_exception), param);
            }

            int count = Settings.instance(mContext).getInt(Settings.SUCCESSFULLY_UPLOAD_COUNT, 0);
            Settings.instance(mContext).putInt(Settings.SUCCESSFULLY_UPLOAD_COUNT, count + 1);

            String url = response.request().url().toString();
            File html = null;
            String htmlFilename = new File(param.getFileUri()).getName();

            switch (param.getEngineId()) {
                case SITE_GOOGLE:
                    url = UploadResultUtils.getModifiedGoogleUrl(mContext, url);
                    break;
                case SITE_BAIDU:
                    try {
                        url = UploadResultUtils.getUrlFromBaiduJSON(mContext, response.body().byteStream());
                    } catch (Exception e) {
                        return new UploadResult(UploadResult.ERROR_IO, "message from image.baidu.com:" + e.getMessage(), param);
                    }
                    break;
                case SITE_IQDB:
                case SITE_SAUCENAO:
                    html = Utils.streamToCacheFile(mContext, response.body().byteStream(), "html", htmlFilename);
                    break;
                default:
                    html = Utils.streamToCacheFile(mContext, response.body().byteStream(), "html", htmlFilename);
                    break;
            }

            return new UploadResult(
                    param.getEngineId(),
                    param.getFileUri(),
                    param.getFilename(),
                    url,
                    html == null ? null : html.getAbsolutePath(),
                    param.getResultOpenAction()
            );
        }

        protected void onProgressUpdate(Integer... values) {
        }

        protected void onPostExecute(UploadResult result) {
            // TODO copy from cache to file if history enabled (然而还没做)

            Log.d("UploadService", "onPostExecute");

            onTaskFinished(mCanceled, mKey, result);

            removeTask(mKey);
        }

        private void onTaskFinished(boolean canceled, String key, UploadResult result) {
            if (mFileUri != null) {
                File file = new File(mFileUri);
                if (!file.delete()) {
                    Log.w("UploadService", "cache file not deleted");
                }
            }

            Intent intent = new Intent(INTENT_ACTION_RESULT);
            intent.putExtra(EXTRA_RESULT, result);
            intent.putExtra(EXTRA_KEY, key);

            if (!LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent)
                    && result.getErrorCode() != UploadResult.CANCELED
                    && !canceled) {
                sendBroadcast(intent);
            }
        }
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            removeTask();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("Service", "onCreate");

        registerReceiver(mBroadcastReceiver, new IntentFilter(INTENT_ACTION_CANCEL));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("Service", "onDestroy");

        unregisterReceiver(mBroadcastReceiver);
    }

    /*@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Service", "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("Service", "onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        Log.d("Service", "onRebind");
    }*/

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("Service", "onBind");

        startForeground(NOTIFICATION_ID, getNotification(mTasks.size()));
        return mUploadBinder;
    }

    private void removeTask() {
        for (String key : mTasks.keySet()) {
            UploadTask task = mTasks.get(key);
            if (task != null) {
                if (!task.isCancelled()) {
                    task.cancel(true);
                }
                task.mCanceled = true;
                mTasks.remove(key);

                task.onTaskFinished(true, task.mKey, new UploadResult(UploadResult.CANCELED, "canceled", null));
            }
        }

        checkStopAndUpdateNotification();
    }

    private void removeTask(String key) {
        UploadTask task = mTasks.get(key);
        if (task != null) {
            if (!task.isCancelled()) {
                task.cancel(true);
            }
            task.mCanceled = true;
            mTasks.remove(key);
        }

        checkStopAndUpdateNotification();
    }

    private void startTask(UploadParam param, String key) {
        UploadTask task = new UploadTask(getApplicationContext(), key);
        task.execute(param);
        mTasks.put(key, task);

        checkStopAndUpdateNotification();
    }

    private void checkStopAndUpdateNotification() {
        Log.d("UploadService", "checkStopAndUpdateNotification task count " + mTasks.size());

        if (!isServiceUploading()) {
            stopForeground(true);
            return;
        }

        updateNotification();
    }

    private Notification getNotification(int taskCount) {
        return new NotificationCompat.Builder(this)
                .setContentTitle(taskCount == 0 ?
                        getString(R.string.uploading) : getResources().getQuantityString(R.plurals.uploading_notification, taskCount, taskCount))
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setSmallIcon(R.drawable.ic_stat)
                .setProgress(100, 0, true)
                .setColor(0xFF3F51B5)
                .addAction(R.drawable.ic_stat_cancel, getString(android.R.string.cancel),
                        PendingIntent.getBroadcast(this, 0, new Intent(INTENT_ACTION_CANCEL), PendingIntent.FLAG_ONE_SHOT))
                .build();
    }

    private void updateNotification() {
        startForeground(NOTIFICATION_ID, getNotification(mTasks.size()));
    }

    private boolean isServiceUploading() {
        return !mTasks.isEmpty();
    }

    public class UploadBinder extends Binder {
        public void cancelTask(String key) {
            removeTask(key);
        }

        public void addTask(UploadParam param, String key) {
            startTask(param, key);
        }

        public boolean isUploading() {
            return isServiceUploading();
        }
    }
}
