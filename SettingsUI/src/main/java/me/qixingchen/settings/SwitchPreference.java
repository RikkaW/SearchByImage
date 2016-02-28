package me.qixingchen.settings;

import android.content.Context;
import android.support.v4.content.SharedPreferencesCompat;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.util.AttributeSet;

/**
 * Created by Rikka on 2016/2/25.
 */
public class SwitchPreference extends SwitchPreferenceCompat {
    public SwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SwitchPreference(Context context) {
        super(context);
    }
}
