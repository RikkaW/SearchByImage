package rikka.searchbyimage;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;


/**
 * Created by Rikka on 2015/12/12.
 */
public class HttpUploadFile {

    String boundary = "----WebKitFormBoundaryAAGZldGncBiDdsTP";

    public String Upload(String uri, String fileFromName, String filePath) {


        byte[] postHeaderBytes = getHeadBytes(fileFromName);
        byte[] boundaryBytes = getBoundaryBytes();

        HttpURLConnection connection = null;
        BufferedInputStream fileStream = null;
        String responseUri = null;
        File file = new File(filePath);
        InputStream inputStream;

        try {
            /*Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1",
                    27000));*/
            connection = (HttpURLConnection) new URL(uri).openConnection();


            connection.setRequestMethod("POST");
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("content-type", "multipart/form-data; boundary=" + boundary);
            connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
            connection.setRequestProperty("Cache-Control", "no-cache");
            connection.setUseCaches(false);
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent",
                    "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.152 Safari/535.19");
            connection.setConnectTimeout(60 * 1000);
            connection.setDoOutput(true);
            OutputStream os = connection.getOutputStream();
            os.write(postHeaderBytes);

            byte[] buffer = new byte[4096];

            inputStream = new FileInputStream(file);
            fileStream = new BufferedInputStream(inputStream);
            while ((fileStream.read(buffer)) != -1) {
                os.write(buffer);
            }

            os.write(boundaryBytes);
            os.flush();
            os.close();

            connection.connect();
            connection.getInputStream();

            responseUri = connection.getURL().toString();

        } catch (IOException e) {
            e.printStackTrace();
            StackTraceElement stackTraceElement= e.getStackTrace()[0];// 得到异常棧的首个元素
            responseUri = e.toString() + "\n" + stackTraceElement.getLineNumber() + "\n" + stackTraceElement.getMethodName();

        } finally {
            if (fileStream != null) {
                try {
                    fileStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                connection.disconnect();
            }
            if (fileStream!=null){
                try {
                    fileStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return responseUri;
    }

    private byte[] getHeadBytes(String fileFromName) {
        // 前面
        StringBuilder sb = new StringBuilder();
        sb.append("--" + boundary);
        sb.append("\r\n");
        sb.append("Content-Disposition: form-data; name=\"encoded_image\"; filename=\"");
        sb.append(fileFromName);
        sb.append("\"");
        sb.append("\r\n");
        sb.append("Content-Type: application/octet-stream");
        sb.append("\r\n\r\n");

        ByteBuffer byteBuffer = Charset.forName("UTF-8").encode(sb.toString());
        byte[] b = new byte[byteBuffer.remaining()];
        byteBuffer.get(b);
        return b;
    }

    private byte[] getBoundaryBytes() {
        // 后面
        StringBuilder sb = new StringBuilder();
        sb.append("\r\n");
        sb.append("--" + boundary + "--");
        sb.append("\r\n");

        ByteBuffer byteBuffer = Charset.forName("UTF-8").encode(sb.toString());
        byte[] b = new byte[byteBuffer.remaining()];
        byteBuffer.get(b);
        return b;
    }
}