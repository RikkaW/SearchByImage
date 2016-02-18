package me.qixingchen.settings;

import android.content.Context;
import android.content.res.Resources;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Rikka on 2016/1/10.
 */
public class Utils {
    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static File streamToCacheFile(Context context, InputStream inputStream, String name) {
        return streamToCacheFile(context, inputStream, "", name);
    }

    public static File streamToCacheFile(Context context, InputStream inputStream, String path, String name) {
        String FilePath = context.getCacheDir().getAbsolutePath() + "/" + path + "/" + name;

        File file = new File(FilePath);
        if (!file.getParentFile().exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.getParentFile().mkdirs();
        }
        try {
            //noinspection ResultOfMethodCallIgnored
            file.createNewFile();

            FileOutputStream outputStream = new FileOutputStream(FilePath);

            int bytesRead;
            byte[] buffer = new byte[1024];
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }
}
