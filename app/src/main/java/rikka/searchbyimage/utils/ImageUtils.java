package rikka.searchbyimage.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Created by Rikka on 2016/1/6.
 */
public class ImageUtils {
    public static InputStream ResizeImage(InputStream inputStream) {
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scale = 1.0f;

        if (width > height && width > 1000) {
            scale = 1000.0f / width;
        } else if (width < height && height > 1000){
            scale = 1000.0f / height;
        }

        Bitmap resized = Bitmap.createScaledBitmap(bitmap,
                (int) (bitmap.getWidth() * scale),
                (int) (bitmap.getHeight() * scale),
                true);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        resized.compress(Bitmap.CompressFormat.PNG, 0, bos);
        byte[] bytes = bos.toByteArray();

        return new ByteArrayInputStream(bytes);
    }
}