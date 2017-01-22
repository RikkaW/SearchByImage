package rikka.searchbyimage.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Rikka on 2016/1/6.
 */
public class ImageUtils {



    @Nullable
    public static byte[] resizeImage(@Nullable InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return null;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = calculateInSampleSize(inputStream.available(), 1024 * 1024); // 1MB

        Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
        if (bitmap == null) {
            return null;
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
        byte[] bytes = bos.toByteArray();

        Log.d("ResizeImage", "inSampleSize = " + options.inSampleSize);

        return bytes/*new ByteArrayInputStream(bytes)*/;
    }

    private static int calculateInSampleSize(int size, int maxSize) {
        // 糟糕的大概计算...
        int curSize = size;
        int inSampleSize = 1;

        while (curSize > maxSize) {
            inSampleSize *= 2;
            curSize /= 4;
        }

        return inSampleSize;
    }

}