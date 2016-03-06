package me.qixingchen.settings;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Rikka on 2016/3/6.
 */
public class MultiSelectListPreference extends android.support.v14.preference.MultiSelectListPreference {
    public MultiSelectListPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public MultiSelectListPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MultiSelectListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MultiSelectListPreference(Context context) {
        super(context);
    }

    @Override
    protected void onClick() {
        //super.onClick();

        final Set<String> values = new HashSet<>();
        values.addAll(getValues());

        boolean[] checked = new boolean[getEntryValues().length];
        for (String s : values) {
            checked [findIndexOfValue(s)] = findIndexOfValue(s) >= 0;
        }

        new AlertDialog.Builder(getContext(), R.style.PreferenceTheme_Dialog)
                .setTitle(getDialogTitle())
                .setMessage(getDialogMessage())
                .setIcon(getDialogIcon())
                .setNegativeButton(getNegativeButtonText(), null)
                .setPositiveButton(getPositiveButtonText(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setValues(values);
                    }
                })
                .setMultiChoiceItems(getEntries(), checked, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which, boolean isChecked) {
                        if (isChecked) {
                            values.add(getEntryValues()[which].toString());
                        } else {
                            values.remove(getEntryValues()[which].toString());
                        }
                    }
                })
                .show();
    }
}
