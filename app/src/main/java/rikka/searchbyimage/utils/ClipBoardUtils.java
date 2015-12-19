package rikka.searchbyimage.utils;

import android.content.ClipData;
import android.content.Context;
import android.content.ClipboardManager;

/**
 * Created by Rikka on 2015/12/18.
 */
public class ClipBoardUtils {
    public static void putTextIntoClipboard(Context context, String text) {
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("copy text", text);
        clipboardManager.setPrimaryClip(clipData);
    }
}
