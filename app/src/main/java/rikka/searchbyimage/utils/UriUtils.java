package rikka.searchbyimage.utils;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Yulan on 2016/5/28.
 */

public class UriUtils {
    /**
     * save the image shared to the app
     *
     * @param uri the file uri
     *
     * @return file
     * return null if file not exits or not have permission
     */
    @Nullable
    public static File storageImageShared(Context context, Uri uri) {
        InputStream is = null;
        OutputStream os = null;
        File file = null;
        if (uri.getAuthority() != null) {
            try {
                is = context.getContentResolver().openInputStream(uri);
                File folder = context.getExternalCacheDir();
                if (folder == null) {
                    folder = context.getCacheDir();
                }
                String RootPath = folder.toString();
                String FilePath = RootPath + "/image/" + "image"/*getFileName(uri)*/;
                file = new File(FilePath);
                if (!file.getParentFile().exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    file.getParentFile().mkdirs();
                }
                if (file.exists()) {
                    file.delete();
                }
                try {
                    //noinspection ResultOfMethodCallIgnored
                    file.createNewFile();
                } catch (IOException e) {
                    Log.e("在保存图片时出错：", e.toString());
                    throw e;
                }
                os = new FileOutputStream(file);
                byte[] buf = new byte[1024 * 8];
                int len;
                if (is != null) {
                    while ((len = is.read(buf)) != -1) {
                        os.write(buf, 0, len);
                    }
                }
                os.flush();

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    if (os != null) {
                        os.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return file;
    }

    public static String getFileName(Uri uri) {
        String fileName = getFileName(uri.toString());
        if (!fileName.contains(".")) {
            fileName += ".jpg";
        }
        fileName = fileName.replace("%", "_");
        return fileName;
    }

    public static String getFileName(final String filePath) {
        String file;
        if (filePath.contains("?")) {
            file = filePath.substring(0, filePath.indexOf("?"));
        } else {
            file = filePath;
        }
        int last = file.lastIndexOf("/");
        file = file.substring(last + 1).replace("%", "_");
        final String typeStart = "format/";
        if (filePath.contains(typeStart)) {
            int start = filePath.indexOf(typeStart) + typeStart.length();
            String type = filePath.substring(start);
            if (type.contains("/")) {
                type = type.substring(0, type.indexOf("/"));
            }
            file = file.substring(0, file.lastIndexOf(".") + 1) + type;
        }
        return file;
    }
}
