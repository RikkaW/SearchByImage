package rikka.searchbyimage.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Rikka on 2016/1/22.
 */
public class HttpUtils {
    public interface Callback {
        void onSuccess(String url, int code, InputStream stream);
        void onFail(int code);
        void onRetry(int retry);
    }

    private static OkHttpClient init() {
        return new OkHttpClient.Builder()
                .writeTimeout(600, TimeUnit.SECONDS)
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    public static void postForm(String url, Header header, Body body, Callback callback) throws IOException {
        OkHttpClient okHttpClient = init();

        Request.Builder builder = new Request.Builder();
        builder.url(url);

        for (String key : header.getMap().keySet()) {
            builder.addHeader(key, header.getMap().get(key));
        }

        MultipartBody.Builder bodyBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);

        for (Body.FormData data : body.getList()) {
            if (data.value != null) {
                bodyBuilder.addFormDataPart(data.key, data.value);
            } else {
                bodyBuilder.addFormDataPart(data.key, data.filename,
                        RequestBody.create(MediaType.parse("image/png"), data.file));
            }
        }

        builder.post(bodyBuilder.build());

        int retry = 0;
        Response response = null;
        while (response == null) {
            try {
                response = okHttpClient.newCall(builder.build()).execute();
            } catch (IOException e) {
                retry ++;
                callback.onRetry(retry);
            } catch (Throwable e) {
                callback.onFail(-1);
                return;
            }
        }

        if (response.isSuccessful()) {
            callback.onSuccess(
                    response.request().url().toString(),
                    response.code(),
                    response.body().byteStream());
        } else {
            callback.onFail(response.code());
        }
    }

    public static class Header {
        private Map<String, String> headers = new HashMap<>();

        public Header() {
        }

        public Header(Map<String, String> map) {
            this.headers = map;
        }

        public Header add(String key, String value) {
            headers.put(key, value);
            return this;
        }

        private Map<String, String> getMap() {
            return headers;
        }
    }

    public static class Body {
        private class FormData {
            public String key;
            public String value;
            public String filename;
            public File file;

            public FormData(String key, String value) {
                this.key = key;
                this.value = value;
            }

            public FormData(String key, String filename, File file) {
                this.key = key;
                this.filename = filename;
                this.file = file;
            }
        }
        private List<FormData> body = new ArrayList<>();

        public Body() {
        }

        public Body(List<FormData> body) {
            this.body = body;
        }

        public Body add(String key, String value) {
            body.add(new FormData(key, value));
            return this;
        }

        public Body add(String key, String fileName, File file) {
            body.add(new FormData(key, fileName, file));
            return this;
        }

        private List<FormData> getList() {
            return body;
        }
    }
}
