package rikka.searchbyimage.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.preference.ListPreference;
import android.util.AttributeSet;

/**
 * Created by Rikka on 2015/12/1.
 */
public class ListPreferenceShowValue extends ListPreference {

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ListPreferenceShowValue(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ListPreferenceShowValue(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ListPreferenceShowValue(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ListPreferenceShowValue(Context context) {
        super(context, null);
    }

    @Override
    public void setValue(String index) {
        super.setValue(index);
        setSummary(getEntries()[findIndexOfValue(index)]);
    }
}

