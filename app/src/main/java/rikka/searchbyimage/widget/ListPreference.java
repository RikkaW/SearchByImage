package rikka.searchbyimage.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;

/**
 * Created by Rikka on 2015/12/1.
 */
public class ListPreference extends android.support.v7.preference.ListPreference {

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ListPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ListPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ListPreference(Context context) {
        super(context, null);
    }

    @Override
    public void setValue(String index) {
        super.setValue(index);
        setSummary(getEntries()[findIndexOfValue(index)]);
    }
}

