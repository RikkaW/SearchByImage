package rikka.searchbyimage.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.AppCompatSpinner;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import rikka.searchbyimage.R;
import rikka.searchbyimage.utils.Utils;

/**
 * Created by Rikka on 2016/1/29.
 */
public class DropDown extends FrameLayout {
    private Context mContext;
    //private ArrayAdapter<String> mAdapter;
    private SpinnerAdapter mAdapter;
    private AppCompatSpinner mSpinner;
    private TextView mTitleTextView;
    private TextView mSummaryTextView;

    private String mTitle;
    private String mSummary;


    public DropDown(Context context) {
        super(context, null);
    }

    public DropDown(Context context, AttributeSet attrs) {
        super(context, attrs);
        setPadding(0, Utils.dpToPx(8), 0, Utils.dpToPx(8));
        setMinimumHeight(Utils.dpToPx(48));

        mContext = context;

        //mAdapter = new ArrayAdapter<>(mContext,
        //        android.R.layout.simple_spinner_dropdown_item);
        mSpinner = new AppCompatSpinner(context);
        //mSpinner.setAdapter(mAdapter);
        mSpinner.setPadding(Utils.dpToPx(20), 0, 0, 0);
        mSpinner.setVisibility(INVISIBLE);
        mSpinner.setLayoutParams(new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT));
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                setSelection(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        addView(mSpinner);

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mSpinner.performClick();
            }
        });

        LayoutInflater.from(context).inflate(R.layout.drop_down, this);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DropDown);
        mTitle = a.getString(R.styleable.DropDown_myTitle);
        a.recycle();

        mTitleTextView = (TextView) findViewById(android.R.id.title);
        mTitleTextView.setSingleLine(true);
        mTitleTextView.setText(mTitle);

        mSummaryTextView = (TextView) findViewById(android.R.id.summary);
        mSummaryTextView.setSingleLine(true);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        mTitleTextView.setEnabled(enabled);
        mSummaryTextView.setEnabled(enabled);
    }

    public void setTitle(String title) {
        mTitle = title;
        mTitleTextView.setText(title);
    }

    public void setSummary(String summary) {
        mSummary = summary;
        mSummaryTextView.setText(summary);
    }

    public void setSelection(int position) {
        setSummary(mAdapter.getItem(position).toString());
    }

    public int getSelectedItemPosition() {
        return mSpinner.getSelectedItemPosition();
    }

    public void setAdapter(SpinnerAdapter adapter) {
        mAdapter = adapter;
        mSpinner.setAdapter(adapter);
    }
}
