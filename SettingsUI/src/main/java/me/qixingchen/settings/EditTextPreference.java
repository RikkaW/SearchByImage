package me.qixingchen.settings;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

/**
 * Created by Rikka on 2015/12/1.
 */
public class EditTextPreference extends android.support.v7.preference.EditTextPreference {
    private boolean showValueSummary;

    public EditTextPreference(Context context) {
        super(context);
    }

    public EditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MyPreference);
        showValueSummary = a.getBoolean(R.styleable.MyPreference_showValueSummary, true);
        a.recycle();
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
        if (showValueSummary) {
            setSummary(text);
        }
    }

    @Override
    protected void onClick() {
        //super.onClick();
        View view = LayoutInflater.from(getContext()).inflate(R.layout.preference_dialog_edittext, null);
        final AppCompatEditText editText = (AppCompatEditText) view.findViewById(android.R.id.edit);
        editText.setText(getText());

        new AlertDialog.Builder(getContext(), R.style.PreferenceTheme_Dialog)
                .setTitle(getDialogTitle())
                .setMessage(getDialogMessage())
                .setIcon(getDialogIcon())
                .setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditTextPreference.this.setText(editText.getText().toString());
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }
}

