package rikka.searchbyimage.service;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import okhttp3.Call;
import rikka.searchbyimage.R;
import rikka.searchbyimage.staticdata.CustomEngine;
import rikka.searchbyimage.support.Settings;
import rikka.searchbyimage.utils.HttpUtils;
import rikka.searchbyimage.utils.ImageUtils;
import rikka.searchbyimage.utils.ResponseUtils;
import rikka.searchbyimage.utils.Utils;

import static rikka.searchbyimage.staticdata.EngineId.*;

/**
 * Created by Yulan on 2016/5/28.
 * upload file service
 */

public class UploadService extends Service {

    private static final int NOTIFICATION_ID = 0x10;

    public static final String INTENT_ACTION_RETRY = "rikka.searchbyimage2.intent_action_retry";
    public static final String INTENT_ACTION_SUCCESS = "rikka.searchbyimage2.intent_action_success";
    public static final String INTENT_ACTION_ERROR = "rikka.searchbyimage2.intent_action_error";
    public static final String INTENT_RETRY_TIMES = "intent_retry_times";
    public static final String INTENT_ERROR_TITLE = "intent_error_title";
    public static final String INTENT_ERROR_MESSAGE = "intent_error_message";

    private UploadBinder uploadBinder = new UploadBinder();

    private UploadTask uploadTask = new UploadTask(this);
    private boolean isUploading = false;

    private class UploadTask extends AsyncTask<String, Integer, ResponseUtils.HttpUpload> {

        private Context mContext;

        private ResponseUtils.HttpUpload mHttpUpload;

        public UploadTask(Context context) {
            mContext = context;
        }

        protected ResponseUtils.HttpUpload doInBackground(String... filePath) {
            Log.d("UploadService", "doInBackground");

            isUploading = true;

            CustomEngine.getList(mContext);

            mHttpUpload = new ResponseUtils.HttpUpload();

            File image = new File(filePath[0]);
            if (!image.exists()) {
                mHttpUpload.error = new ResponseUtils.ErrorMessage(new FileNotFoundException(filePath[0]),
                        mContext.getString(R.string.something_wrong));
                return mHttpUpload;
            }

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);

            mHttpUpload.siteId = Integer.parseInt(preferences.getString("search_engine_preference", "0"));

            InputStream inputStream = null;
            try {
                inputStream = new FileInputStream(image);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                mHttpUpload.error = new ResponseUtils.ErrorMessage(e,
                        getString(R.string.file_not_found));
            }

            byte[] content = null;
            if (preferences.getBoolean("resize_image", false)) {
                try {
                    content = ImageUtils.ResizeImage(inputStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (content == null) {
                    mHttpUpload.error = new ResponseUtils.ErrorMessage("Error", "stream is null");
                    return mHttpUpload;
                }
            }


            String uploadUri;
            String key;

            HttpUtils.Body body = new HttpUtils.Body();

            CustomEngine item = CustomEngine.getItemById(mHttpUpload.siteId);
            uploadUri = item.getUpload_url();
            key = item.getPost_file_key();

            switch (mHttpUpload.siteId) {
                case SITE_IQDB:
                    Set<String> iqdb_service = preferences.getStringSet("iqdb_service", new HashSet<String>());
                    String[] selected = iqdb_service.toArray(new String[iqdb_service.size()]);

                    for (String aSelected : selected) {
                        body.add("service[]", aSelected);
                    }

                    if (preferences.getBoolean("iqdb_forcegray", false)) {
                        body.add("forcegray", "on");
                    }
                    break;
                case SITE_SAUCENAO:
                    body.add("hide", preferences.getString("saucenao_hide", "0"));
                    body.add("database", preferences.getString("saucenao_database", "999"));
                    break;
                default:
                    if (item.post_text_key.size() > 0) {
                        for (int i = 0; i < item.post_text_key.size(); i++) {
                            body.add(item.post_text_key.get(i), item.post_text_value.get(i));
                        }
                    }
                    break;
            }
            String fileName = Settings.instance(mContext).getString(Settings.STORAGE_IMAGE_NAME, "image.jpg");
            if (content == null) {
                body.add(key, fileName, image);
            } else {
                body.add(key, fileName, content);
            }

            upload(uploadUri, body);
            return mHttpUpload;
        }

        private void upload(String uploadUri, HttpUtils.Body body) {
            try {
                HttpUtils.postForm(uploadUri,
                        new HttpUtils.Header()
                                .add("accept", "*/*")
                                .add("accept-encoding", "deflate")
                                .add("cache-control", "no-cache")
                                .add("connection", "close")
                                .add("user-agent",
                                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.80 Safari/537.36")
                        ,
                        body,
                        new HttpUtils.Callback() {
                            @Override
                            public void onSuccess(String url, int code, InputStream stream) {
                                switch (mHttpUpload.siteId) {
                                    case SITE_GOOGLE:
                                        url = ResponseUtils.getModifiedGoogleUrl(mContext, url);
                                        break;
                                    case SITE_BAIDU:
                                        try {
                                            url = ResponseUtils.getUrlFromBaiduJSON(mContext, stream);
                                        } catch (Exception e) {
                                            mHttpUpload.error = new ResponseUtils.ErrorMessage("Message from image.baidu.com:", e.getMessage());
                                        }
                                        break;
                                    case SITE_IQDB:
                                    case SITE_SAUCENAO:
                                        Utils.streamToCacheFile(mContext, stream, "html", "result.html");
                                        mHttpUpload.html = mContext.getCacheDir().getAbsolutePath() + "/" + "html" + "/" + "result.html";
                                        break;
                                    default:
                                        Utils.streamToCacheFile(mContext, stream, "html", "result.html");
                                        mHttpUpload.html = mContext.getCacheDir().getAbsolutePath() + "/" + "html" + "/" + "result.html";
                                        break;
                                }

                                mHttpUpload.url = url;
                            }

                            @Override
                            public void onFail(int code) {
                                mHttpUpload.error = new ResponseUtils.ErrorMessage(getString(R.string.socket_exception), Integer.toString(code));
                            }

                            @Override
                            public void onRetry(int retry) {
                                publishProgress(retry);
                            }
                        }
                );
            } catch (UnknownHostException e) {
                e.printStackTrace();
                mHttpUpload.error = new ResponseUtils.ErrorMessage(e, mContext.getString(R.string.unknown_host_exception));
            } catch (SocketTimeoutException e) {
                e.printStackTrace();
                mHttpUpload.error = new ResponseUtils.ErrorMessage(e, mContext.getString(R.string.timeout_exception));
            } catch (IOException e) {
                e.printStackTrace();
                mHttpUpload.error = new ResponseUtils.ErrorMessage(e, mContext.getString(R.string.socket_exception));
            }
        }

        protected void onProgressUpdate(Integer... values) {
            Intent intent = new Intent(INTENT_ACTION_RETRY);
            intent.putExtra(INTENT_RETRY_TIMES, values[0]);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        }

        protected void onPostExecute(ResponseUtils.HttpUpload result) {
            isUploading = false;
            stopForeground(true);

            if (result.error != null) {
                Intent intent = new Intent(INTENT_ACTION_ERROR);
                intent.putExtra(INTENT_ERROR_MESSAGE, result.error.message);
                intent.putExtra(INTENT_ERROR_TITLE, result.error.title);
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);

                Log.d(getClass().getSimpleName(), "Broadcast error");
                return;
            }

            Intent intent = new Intent(INTENT_ACTION_SUCCESS);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);

            CustomEngine item = CustomEngine.getItemById(mHttpUpload.siteId);
            if (item != null) {
                switch (item.getResult_open_action()) {
                    case CustomEngine.RESULT_OPEN_ACTION.BUILD_IN_IQDB:
                        ResponseUtils.openIqdbResult(mContext, result);
                        break;
                    case CustomEngine.RESULT_OPEN_ACTION.OPEN_HTML_FILE:
                        ResponseUtils.openHTMLinWebView(mContext, result);
                        break;
                    case CustomEngine.RESULT_OPEN_ACTION.DEFAULT:
                        ResponseUtils.openURL(mContext, result);
                        break;
                }
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(getClass().getSimpleName(), "onBind");

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.uploading))
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setSmallIcon(R.drawable.ic_stat)
                .setProgress(100, 0, true)
                .setColor(0xFF3F51B5)
                .build();

        startForeground(NOTIFICATION_ID, notification);
        return uploadBinder;
    }

    private void cancelTask() {
        HttpUtils.cancel();
        uploadTask.cancel(false);
        isUploading = false;
    }

    private void startTask() {
        File folder = getExternalCacheDir();
        if (folder == null) {
            folder = getCacheDir();
        }

        uploadTask.execute(folder.toString() + "/image/image");
    }

    private boolean isServiceUploading() {
        return isUploading;
    }

    public class UploadBinder extends Binder {
        public void cancel() {
            cancelTask();
        }

        public void startUpload() {
            startTask();
        }

        public boolean isUploading() {
            return isServiceUploading();
        }
    }
}
