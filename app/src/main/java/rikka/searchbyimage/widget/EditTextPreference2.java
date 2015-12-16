package rikka.searchbyimage.widget;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by Rikka on 2015/12/1.
 */
public class EditTextPreference2 extends android.support.v7.preference.EditTextPreference {


    public EditTextPreference2(Context context) {
        super(context);
    }

    public EditTextPreference2(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditTextPreference2(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public EditTextPreference2(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void setText(String text) {
        super.setText(text);
        setSummary(text);
    }
}

