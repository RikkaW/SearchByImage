package rikka.searchbyimage.service;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
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
import rikka.searchbyimage.R;
import rikka.searchbyimage.staticdata.SearchEngine;
import rikka.searchbyimage.support.OkHttpClientProvider;
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

    public static final String INTENT_ACTION_RESULT = "rikka.searchbyimage.intent.action.upload_result";
    public static final String EXTRA_RESULT = "EXTRA_RESULT";
    public static final String EXTRA_KEY = "EXTRA_KEY";

    private UploadBinder mUploadBinder = new UploadBinder();

    private Map<String, UploadTask> mTasks = new HashMap<>();

    private class UploadTask extends AsyncTask<UploadParam, Integer, UploadResult> {

        private Context mContext;
        private String mKey;

        public UploadTask(Context context, String key) {
            mContext = context;
            mKey = key;
        }

        protected UploadResult doInBackground(UploadParam... params) {
            Log.d("UploadService", "doInBackground");

            SearchEngine.getList(mContext);

            UploadParam param = params[0];
            File image = new File(param.getFileUri());
            if (!image.exists()) {
                return new UploadResult(1, mContext.getString(R.string.something_wrong));
            }

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);

            InputStream inputStream;
            try {
                inputStream = new FileInputStream(image);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return new UploadResult(1, getString(R.string.file_not_found));
            }

            byte[] content = null;
            if (preferences.getBoolean("resize_image", true)) {
                try {
                    content = ImageUtils.resizeImage(inputStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (content == null) {
                    return new UploadResult(1, "error when try to resize image");
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
                    return new UploadResult(1, "error when try to resize image");
                }
            }

            return upload(param, content);
        }

        private UploadResult upload(UploadParam param, byte[] content) {
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
                    .addHeader("user-agent",
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36")
                    .post(bodyBuilder.build());

            Request request = builder.build();
            Response response;
            try {
                response = okHttpClient.newCall(request).execute();
            } catch (UnknownHostException e) {
                return new UploadResult(2, mContext.getString(R.string.unknown_host_exception));
            } catch (SocketTimeoutException e) {
                return new UploadResult(3, mContext.getString(R.string.timeout_exception));
            } catch (IOException e) {
                return new UploadResult(4, mContext.getString(R.string.socket_exception));
            } catch (Exception e) {
                return new UploadResult(5, e.getMessage());
            }

            if (!response.isSuccessful()) {
                return new UploadResult(4, mContext.getString(R.string.socket_exception));
            }

            String url = response.request().url().toString();
            File html = null;

            switch (param.getEngineId()) {
                case SITE_GOOGLE:
                    url = UploadResultUtils.getModifiedGoogleUrl(mContext, url);
                    break;
                case SITE_BAIDU:
                    try {
                        url = UploadResultUtils.getUrlFromBaiduJSON(mContext, response.body().byteStream());
                    } catch (Exception e) {
                        return new UploadResult(4, "message from image.baidu.com:" + e.getMessage());
                    }
                    break;
                case SITE_IQDB:
                case SITE_SAUCENAO:
                    html = Utils.streamToCacheFile(mContext, response.body().byteStream(), "html", param.getFilename());
                    break;
                default:
                    html = Utils.streamToCacheFile(mContext, response.body().byteStream(), "html", param.getFilename());
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
            /*Intent intent = new Intent(INTENT_ACTION_RESULT);
            intent.putExtra(INTENT_RETRY_TIMES, values[0]);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);*/
        }

        protected void onPostExecute(UploadResult result) {
            // TODO copy from cache to file if history enabled (然而还没做)

            Log.d("UploadService", "onPostExecute");

            if (result.getErrorCode() == 0) {
                File file = new File(result.getFileUri());
                if (!file.delete()) {
                    Log.w("UploadService", "cache file not deleted");
                }
            }

            removeTask(mKey);

            Intent intent = new Intent(INTENT_ACTION_RESULT);
            intent.putExtra(EXTRA_RESULT, result);
            intent.putExtra(EXTRA_KEY, mKey);

            if (!LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent)) {
                sendBroadcast(intent);
            }

            /*if (item != null) {
                switch (item.getResultOpenAction()) {*/
                    /*case SearchEngine.RESULT_OPEN_ACTION.BUILD_IN_IQDB:
                        UploadResultUtils.openIqdbResult(mContext, result);
                        break;
                    case SearchEngine.RESULT_OPEN_ACTION.OPEN_HTML_FILE:
                        UploadResultUtils.openHTMLinWebView(mContext, result);
                        break;*/
                    /*case SearchEngine.RESULT_OPEN_ACTION.DEFAULT:
                        UploadResultUtils.openURL(mContext, result);
                        break;
                }
            }*/
        }
    }

    /*@Override
    public void onCreate() {
        super.onCreate();
        Log.d("Service", "onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("Service", "onDestroy");
    }

    @Override
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

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.uploading))
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setSmallIcon(R.drawable.ic_stat)
                .setProgress(100, 0, true)
                .setColor(0xFF3F51B5)
                .build();

        startForeground(NOTIFICATION_ID, notification);
        return mUploadBinder;
    }

    private void removeTask(String key) {
        UploadTask task = mTasks.get(key);
        if (task != null) {
            if (!task.isCancelled()) {
                task.cancel(true);
            }
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

        updateNotification("正在上传 " + mTasks.size() + " 个图片");
    }

    private void updateNotification(String title) {
        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle(title)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setSmallIcon(R.drawable.ic_stat)
                .setProgress(100, 0, true)
                .setColor(0xFF3F51B5)
                //.setOngoing(true)
                .build();

        startForeground(NOTIFICATION_ID, notification);
        /*((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
                .notify(NOTIFICATION_ID, notification);*/
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
