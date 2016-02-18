package me.qixingchen.settings;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by Rikka on 2015/12/1.
 */
public class EditTextPreference extends android.support.v7.preference.EditTextPreference {


    public EditTextPreference(Context context) {
        super(context);
    }

    public EditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditTextPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public EditTextPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void setText(String text) {
        super.setText(text);
        setSummary(text);
    }
}

