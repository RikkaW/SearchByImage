package rikka.searchbyimage.utils;

import android.content.res.Resources;
import android.util.DisplayMetrics;

/**
 * Created by Rikka on 2016/1/10.
 */
public class Utils {
    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }
}
