package rikka.searchbyimage.ui;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.List;

import rikka.searchbyimage.R;
import rikka.searchbyimage.database.DatabaseHelper;
import rikka.searchbyimage.database.table.CustomEngineTable;
import rikka.searchbyimage.staticdata.CustomEngine;
import rikka.searchbyimage.staticdata.CustomEngineParcelable;
import rikka.searchbyimage.ui.apdater.PostFormAdapter;
import rikka.searchbyimage.utils.ParcelableUtils;

public class EditSiteInfoActivity extends AppCompatActivity {
    public static final String EXTRA_EDIT_LOCATION =
            "rikka.searchbyimage.ui.EditSiteInfoActivity.EXTRA_EDIT_LOCATION";

    DatabaseHelper mDbHelper;

    CoordinatorLayout mCoordinatorLayout;
    Toolbar mToolbar;
    FloatingActionButton mFAB;
    EditText mEditTextName;
    EditText mEditTextUrl;
    EditText mEditTextFileKey;
    Spinner mSpinner;
    RecyclerView mRecyclerView;

    List<CustomEngine> mData;
    CustomEngine mItem;
    PostFormAdapter mAdapter;

    boolean mEnabled = true;

    int mLocation = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_engine);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);

        mEditTextName = (EditText) findViewById(R.id.edit_name);
        mEditTextUrl = (EditText) findViewById(R.id.edit_url);
        mEditTextFileKey = (EditText) findViewById(R.id.edit_file_key);
        mSpinner = (Spinner) findViewById(R.id.spinner);

        mDbHelper = DatabaseHelper.instance(this);
        mData = CustomEngine.getList(this);

        mFAB = (FloatingActionButton) findViewById(R.id.fab);
        mFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mEnabled) {
                    finish();
                }

                if (!check()) {
                    Snackbar.make(mCoordinatorLayout, "Check your data", Snackbar.LENGTH_LONG).show();
                    return;
                }

                if (mLocation == -1) {
                    add();
                } else {
                    modify();
                }

                finish();
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }

            @Override
            public void onMeasure(RecyclerView.Recycler recycler, RecyclerView.State state,
                                  int widthSpec, int heightSpec) {
                final int width = View.MeasureSpec.getSize(widthSpec);
                int height = 0;
                int childHeight = 0;
                for (int i = 0; i < getItemCount(); i++) {
                    try {
                        childHeight = measureScrapChildHeight(recycler, i,
                                View.MeasureSpec.makeMeasureSpec(i, View.MeasureSpec.UNSPECIFIED),
                                View.MeasureSpec.makeMeasureSpec(i, View.MeasureSpec.UNSPECIFIED));
                        height = height + childHeight;

                    } catch (IndexOutOfBoundsException ignore) {
                        height = height + childHeight;
                    }
                }
                setMeasuredDimension(width, height);
            }
        });
        mRecyclerView.setAdapter(EditSitesActivity.getAdapter(this));
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setHasFixedSize(false);

        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_EDIT_LOCATION)) {
            mLocation = intent.getIntExtra(EXTRA_EDIT_LOCATION, -1);
            mItem = mData.get(mLocation);
            if (mItem != null) {
                mEditTextUrl.setText(mItem.upload_url);
                mEditTextName.setText(mItem.name);
                mEditTextFileKey.setText(mItem.post_file_key);
                if (mItem.result_open_action <= CustomEngine.RESULT_OPEN_ACTION.OPEN_HTML_FILE) {
                    mSpinner.setSelection(mItem.result_open_action);
                }

                mEnabled = (mItem.id > 5);
                mAdapter = new PostFormAdapter(mItem, mEnabled);
                mRecyclerView.setAdapter(mAdapter);


                if (!mEnabled) {
                    mEditTextUrl.setEnabled(false);
                    mEditTextName.setEnabled(false);
                    mEditTextFileKey.setEnabled(false);
                    mSpinner.setEnabled(false);
                    mSpinner.setAdapter(ArrayAdapter.createFromResource(this,
                            R.array.custom_open_with_in_app, android.R.layout.simple_spinner_item));
                    mSpinner.setSelection(mItem.result_open_action);
                }

            } else {
                mLocation = -1;
            }
        } else {
            mAdapter = new PostFormAdapter();
            mItem = mAdapter.getData();
            mRecyclerView.setAdapter(mAdapter);
        }
    }

    private void modify() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        CustomEngineParcelable parcelable = getParcelable();

        ContentValues values = new ContentValues();
        values.put(CustomEngineTable.COLUMN_DATA, ParcelableUtils.marshall(parcelable));

        String selection = CustomEngineTable.COLUMN_ID + " LIKE ?";
        String[] selectionArgs = {String.valueOf(mItem.id)};

        db.update(
                CustomEngineTable.TABLE_NAME,
                values,
                selection,
                selectionArgs);

        mItem.name = parcelable.data.name;
        mItem.upload_url = parcelable.data.upload_url;
        mItem.post_file_key = parcelable.data.post_file_key;
        mItem.result_open_action = parcelable.data.result_open_action;

        EditSitesActivity.getAdapter(this).notifyItemChanged(mLocation + 2);
    }

    private void add() {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        CustomEngineParcelable parcelable = getParcelable();
        parcelable.data.id = CustomEngine.getAvailableId();
        mData.add(parcelable.data);

        ContentValues values = new ContentValues();
        values.put(CustomEngineTable.COLUMN_ID, parcelable.data.id);
        values.put(CustomEngineTable.COLUMN_DATA, ParcelableUtils.marshall(parcelable));

        db.insert(CustomEngineTable.TABLE_NAME, null, values);

        EditSitesActivity.getAdapter(this).notifyItemInserted(mData.size() - 1);
    }

    private CustomEngineParcelable getParcelable() {
        CustomEngineParcelable parcelable = new CustomEngineParcelable();
        parcelable.data.name = mEditTextName.getText().toString();
        parcelable.data.upload_url = mEditTextUrl.getText().toString();
        parcelable.data.post_file_key = mEditTextFileKey.getText().toString();
        parcelable.data.result_open_action = mSpinner.getSelectedItemPosition();
        parcelable.data.post_text_type = mItem.post_text_type;
        parcelable.data.post_text_value = mItem.post_text_value;
        parcelable.data.post_text_key = mItem.post_text_key;
        return parcelable;
    }

    private boolean check() {
        return mEditTextName.getText().toString().length() != 0
                && (URLUtil.isHttpUrl(mEditTextUrl.getText().toString()) || URLUtil.isHttpsUrl(mEditTextUrl.getText().toString()))
                && mEditTextFileKey.getText().toString().length() != 0;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private int measureScrapChildHeight(RecyclerView.Recycler recycler, int position, int widthSpec,
                                        int heightSpec) throws IndexOutOfBoundsException {
        View view = recycler.getViewForPosition(position);
        int height = 0;
        if (view != null) {

            RecyclerView.LayoutParams p = (RecyclerView.LayoutParams) view.getLayoutParams();
            int childWidthSpec = ViewGroup.getChildMeasureSpec(widthSpec,
                    view.getPaddingLeft() + view.getPaddingRight(), p.width);
            int childHeightSpec = ViewGroup.getChildMeasureSpec(heightSpec,
                    view.getPaddingTop() + view.getPaddingBottom(), p.height);
            view.measure(childWidthSpec, childHeightSpec);
            height = view.getMeasuredHeight() + p.bottomMargin + p.topMargin;
            recycler.recycleView(view);
        }
        return height;
    }
}
