package rikka.searchbyimage.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Rikka on 2016/1/6.
 */
public class ImageUtils {
    public static byte[] ResizeImage(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return null;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = calculateInSampleSize(inputStream.available(), 1024 * 1024); // 1MB

        Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
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

    /*private static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }*/
}