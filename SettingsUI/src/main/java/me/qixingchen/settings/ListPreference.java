package me.qixingchen.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

/**
 * Created by Rikka on 2015/12/1.
 */
public class ListPreference extends android.support.v7.preference.ListPreference {
    private boolean showValueSummary;

    public ListPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public ListPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MyPreference);
        showValueSummary = a.getBoolean(R.styleable.MyPreference_showValueSummary, true);
        a.recycle();
    }

    public ListPreference(Context context) {
        super(context, null);
    }

    @Override
    public void setValue(String index) {
        super.setValue(index);
        if (showValueSummary) {
            setSummary(getEntries()[findIndexOfValue(index)]);
        }
    }
}

