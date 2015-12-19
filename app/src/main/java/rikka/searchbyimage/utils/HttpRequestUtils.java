package rikka.searchbyimage.utils;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;


/**
 * Created by Rikka on 2015/12/12.
 */
public class HttpRequestUtils {

    public static class HttpFormData {
        public final static int FORM_DATA_TEXT = 0x1;
        public final static int FORM_DATA_FILE = 0x2;

        private int type;
        private String name;
        private String filename;
        private String string;
        private InputStream inputStream;

        public HttpFormData(String name, String string) {
            this.type = FORM_DATA_TEXT;
            this.name = name;
            this.string = string;
        }

        public HttpFormData(String name, String filename, InputStream inputStream) {
            this.type = FORM_DATA_FILE;
            this.name = name;
            this.filename = filename;
            this.inputStream = inputStream;
        }

        public void writeForm(OutputStream os, String boundary) throws IOException {
            os.write(getFormByteHead(boundary));

            switch (type) {
                case FORM_DATA_TEXT: {
                    ByteBuffer byteBuffer = Charset.forName("UTF-8").encode(string);
                    byte[] b = new byte[byteBuffer.remaining()];
                    byteBuffer.get(b);
                    os.write(b);

                    break;
                }
                case FORM_DATA_FILE: {
                    BufferedInputStream fileStream = null;

                    try {
                        byte[] buffer = new byte[4096];

                        fileStream = new BufferedInputStream(inputStream);
                        while ((fileStream.read(buffer)) != -1) {
                            os.write(buffer);
                        }
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                    finally {
                        if (fileStream != null)
                            fileStream.close();
                    }

                    break;
                }
            }
        }

        private byte[] getFormByteHead(String boundary) {
            StringBuilder sb = new StringBuilder();
            sb.append("--").append(boundary);
            sb.append("\r\n");

            switch (type) {
                case FORM_DATA_TEXT: {
                    sb.append("Content-Disposition: form-data; name=\"").append(name).append("\"");
                    sb.append("\r\n\r\n");
                    break;
                }
                case FORM_DATA_FILE: {
                    sb.append("Content-Disposition: form-data; name=\"").append(name).append("\"; filename=\"").append(filename).append("\"");
                    sb.append("\r\n");
                    sb.append("Content-Type: application/octet-stream");
                    sb.append("\r\n\r\n");
                    break;
                }
            }

            ByteBuffer byteBuffer = Charset.forName("UTF-8").encode(sb.toString());
            byte[] b = new byte[byteBuffer.remaining()];
            byteBuffer.get(b);
            return b;
        }

        private static byte[] getFormByteNewLine() {
            StringBuilder sb = new StringBuilder();
            sb.append("\r\n");

            ByteBuffer byteBuffer = Charset.forName("UTF-8").encode(sb.toString());
            byte[] b = new byte[byteBuffer.remaining()];
            byteBuffer.get(b);
            return b;
        }

        private static byte[] getFormByteEnd(String boundary) {
            StringBuilder sb = new StringBuilder();
            //sb.append("\r\n");
            sb.append("--").append(boundary).append("--");
            sb.append("\r\n");

            ByteBuffer byteBuffer = Charset.forName("UTF-8").encode(sb.toString());
            byte[] b = new byte[byteBuffer.remaining()];
            byteBuffer.get(b);
            return b;
        }
    }

    private String boundary;
    private String uri;
    private String method;
    private int timeout;
    private HttpURLConnection connection;
    private String html;

    private String responseUri;

    private ArrayList<HttpFormData> formDataList = new ArrayList<>();

    public HttpRequestUtils(String uri, String method) {
        this.uri = uri;
        this.method = method;

        this.boundary = generateBoundary();
    }

    private final static char[] MULTIPART_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    private String generateBoundary() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("----WebKitFormBoundary");

        Random rand = new Random();
        for (int i = 0; i < 16; i++) {
            buffer.append(MULTIPART_CHARS[rand.nextInt(MULTIPART_CHARS.length)]);
        }
        return buffer.toString();
    }

    public void addFormData(String name, String str) {
        formDataList.add(new HttpFormData(name, str));
    }

    public void addFormData(String name, String filename, InputStream inputStream) {
        formDataList.add(new HttpFormData(name, filename, inputStream));
    }

    public HttpURLConnection openConnection() throws IOException {
        connection = (HttpURLConnection) new URL(uri).openConnection();

        connection.setRequestMethod(method);
        connection.setRequestProperty("accept", "*/*");
        connection.setRequestProperty("content-type", "multipart/form-data; boundary=" + boundary);
        connection.setRequestProperty("accept-encoding", "deflate");
        connection.setRequestProperty("cache-control", "no-cache");
        connection.setUseCaches(false);
        connection.setRequestProperty("connection", "Keep-Alive");
        connection.setRequestProperty("user-agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.80 Safari/537.36");
                //"Mozilla / 5.0 (Linux; Android 5.1 .1; Nexus 6 Build/LYZ28E) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.20 Mobile Safari/537.36");
        connection.setConnectTimeout(2 * 1000);

        //connection.setRequestProperty("connection", "close");


        return connection;
    }

    private void writeForm() throws IOException {
        connection.setDoOutput(true);

        OutputStream os = connection.getOutputStream();

        for (int i = 0; i < formDataList.size(); i++) {
            formDataList.get(i).writeForm(os, boundary);
            os.write(HttpFormData.getFormByteNewLine());
        }

        os.write(HttpFormData.getFormByteEnd(boundary));

        os.flush();
        os.close();
    }

    public void connect(Context context) throws IOException {
        if (formDataList.size() > 0)
            writeForm();

        connection.connect();

        InputStream inputStream = connection.getInputStream();
        String RootPath = context.getCacheDir().getAbsolutePath();
        String FilePath = RootPath + "/html/result.html";

        File file = new File(FilePath);
        if (!file.getParentFile().exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.getParentFile().mkdirs();
        }
        try {
            //noinspection ResultOfMethodCallIgnored
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileOutputStream outputStream = new FileOutputStream(FilePath);

        int bytesRead;
        byte[] buffer = new byte[1024];
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        html = FilePath;

        responseUri = connection.getURL().toString();

        connection.disconnect();
    }

    public String getResponseUri(Context context) throws IOException {
        openConnection();
        connect(context);
        return responseUri;
    }

    public String getHtml() {
        return html;
    }
}