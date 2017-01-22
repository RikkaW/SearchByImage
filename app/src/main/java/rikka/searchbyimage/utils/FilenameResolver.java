package rikka.searchbyimage.utils;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

/**
 * Created by Rikka on 2017/1/22.
 */

public final class FilenameResolver {

    public static String query(ContentResolver contentResolver, Uri uri) {
        if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
            return uri.getLastPathSegment();
        } else if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            String filename = null;
            Cursor cursor = contentResolver.query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                filename = cursor.getString(nameIndex);
                cursor.close();
            }
            return filename;
        }
        return null;
    }
}
