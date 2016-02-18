package me.qixingchen.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.support.v7.widget.AppCompatSpinner;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

/**
 * Created by Rikka on 2015/12/15.
 */
public class DropDownPreference extends Preference {

    private Context mContext;
    private ArrayAdapter<String> mAdapter;
    private AppCompatSpinner mSpinner;
    private ArrayList<Object> mValues = new ArrayList<>();

    private CharSequence[] mEntries;
    private CharSequence[] mEntryValues;

    private Callback mCallback;
    private int mSelectedPosition = -1;

    public DropDownPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public DropDownPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public DropDownPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
        mAdapter = new ArrayAdapter<String>(mContext,
                android.R.layout.simple_spinner_dropdown_item);

        mSpinner = new AppCompatSpinner(context);
        mSpinner.setVisibility(View.INVISIBLE);
        mSpinner.setAdapter(mAdapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                setSelectedItem(position, true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // noop
            }
        });

        setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                mSpinner.performClick();
                return true;
            }
        });

        // Support XML specification like ListPreferences do.
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DropDownPreference);
        mEntries = a.getTextArray(R.styleable.DropDownPreference_myEntries);
        mEntryValues = a.getTextArray(R.styleable.DropDownPreference_myEntryValues);
        if (mEntries != null && mEntryValues != null) {
            for (int i = 0; i < mEntries.length; i++) {
                addItem(mEntries[i].toString(), mEntryValues[i]);
            }
        }
        a.recycle();
    }

    public DropDownPreference(Context context) {
        super(context, null);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        if (restoreValue) {
            int index = findIndexByEntryValues(getPersistedString(""));
            if (index != -1 && index < mEntries.length) {
                setSelectedItem(index);
            }
        }
    }

    public int findIndexByEntryValues(String value) {
        if (mEntryValues == null) {
            return -1;
        }

        for (int i = 0; i < mEntryValues.length; i++) {
            if (value.equals(mEntryValues[i])) {
                return i;
            }
        }

        return -1;
    }

    public void setDropDownWidth(int dimenResId) {
        mSpinner.setDropDownWidth(mContext.getResources().getDimensionPixelSize(dimenResId));
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public void setSelectedItem(int position) {
        setSelectedItem(position, false);
    }

    public void setSelectedItem(int position, boolean fromSpinner) {
        boolean changed = mSelectedPosition != position;

        if (fromSpinner && position == mSelectedPosition) {
            return;
        }
        final Object value = mValues.get(position);
        if (mCallback != null && !mCallback.onItemSelected(position, value)) {
            return;
        }
        mSpinner.setSelection(position);
        mSelectedPosition = mSpinner.getSelectedItemPosition();
        setSummary(mAdapter.getItem(position));
        final boolean disableDependents = value == null;
        notifyDependencyChange(disableDependents);
        notifyChanged();

        if (changed && value != null) {
            persistString(value.toString());
        }
    }

    public int getSelectedPosition() {
        return mSelectedPosition;
    }

    public void setSelectedValue(Object value) {
        final int i = mValues.indexOf(value);
        if (i > -1) {
            setSelectedItem(i);
        }
    }

    public void addItem(int captionResid, Object value) {
        addItem(mContext.getResources().getString(captionResid), value);
    }

    public void addItem(String caption, Object value) {
        mAdapter.add(caption);
        mValues.add(value);
    }

    public void removeItem(int index) {
        mAdapter.remove(mAdapter.getItem(index));
        mValues.remove(index);
    }

    public Object getValue(int index) {
        return mValues.get(index);
    }

    public ArrayAdapter<String> getAdapter() {
        return mAdapter;
    }

    public ArrayList<Object> getValues() {
        return mValues;
    }

    public void setAdapter(ArrayAdapter<String> adapter) {
        mAdapter = adapter;
    }

    public void setValues(ArrayList<Object> values) {
        mValues = values;
    }

    public int getItemCount() {
        return mAdapter.getCount();
    }

    public void clearItems() {
        mAdapter.clear();
        mValues.clear();
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        if (holder.equals(mSpinner.getParent())) return;
        if (mSpinner.getParent() != null) {
            ((ViewGroup) mSpinner.getParent()).removeView(mSpinner);
        }
        final ViewGroup vg = (ViewGroup) holder.itemView;
        vg.addView(mSpinner, 0);
        final ViewGroup.LayoutParams lp = mSpinner.getLayoutParams();
        lp.width = 0;
        mSpinner.setLayoutParams(lp);
    }
    //    @Override
    //    public void onBindView(View view) {
    //        super.onBindView(view);
    //        if (view.equals(mSpinner.getParent())) return;
    //        if (mSpinner.getParent() != null) {
    //            ((ViewGroup)mSpinner.getParent()).removeView(mSpinner);
    //        }
    //        final ViewGroup vg = (ViewGroup)view;
    //        vg.addView(mSpinner, 0);
    //        final ViewGroup.LayoutParams lp = mSpinner.getLayoutParams();
    //        lp.width = 0;
    //        mSpinner.setLayoutParams(lp);
    //    }

    public interface Callback {
        boolean onItemSelected(int pos, Object value);
    }
}
