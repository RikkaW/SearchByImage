package rikka.searchbyimage.ui;

import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Html;
import android.view.Menu;
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
import rikka.searchbyimage.utils.URLUtils;
import rikka.searchbyimage.utils.Utils;

public class EditSiteInfoActivity extends AppCompatActivity {
    public static final String EXTRA_EDIT_LOCATION =
            "rikka.searchbyimage.ui.EditSiteInfoActivity.EXTRA_EDIT_LOCATION";

    DatabaseHelper mDbHelper;

    Activity mActivity;
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
    MyLinearLayoutManager mLayoutManager;

    boolean mEnabled = true;

    int mLocation = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_engine);

        mActivity = this;

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
                    Snackbar.make(mCoordinatorLayout, R.string.check_your_data, Snackbar.LENGTH_LONG).show();
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
        mLayoutManager = new MyLinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(EditSitesActivity.getAdapter(this));
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setHasFixedSize(false);

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                if (viewHolder.getLayoutPosition() == mAdapter.getItemCount() - 1) {
                    return 0;
                }
                return super.getMovementFlags(recyclerView, viewHolder);
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                mAdapter.notifyItemRemoved(viewHolder.getLayoutPosition());
                mAdapter.setItemCount(mAdapter.getItemCount() - 1);
                mLayoutManager.setFakeItemCount(1);
                mCoordinatorLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mLayoutManager.setFakeItemCount(0);
                        mRecyclerView.requestLayout();
                    }
                }, 500);
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);

        itemTouchHelper.attachToRecyclerView(mRecyclerView);

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
            mRecyclerView.setAdapter(mAdapter);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.help:
                URLUtils.Open(
                        "https://github.com/RikkaW/SearchByImage/wiki/%E5%B8%AE%E5%8A%A9%EF%BC%9A%E8%87%AA%E5%AE%9A%E4%B9%89%E6%90%9C%E7%B4%A2%E5%BC%95%E6%93%8E",
                        this);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void modify() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        CustomEngineParcelable parcelable = new CustomEngineParcelable();
        parcelable.data = getData();

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
        mItem.post_text_key = parcelable.data.post_text_key;
        mItem.post_text_value = parcelable.data.post_text_value;
        mItem.post_text_type = parcelable.data.post_text_type;

        EditSitesActivity.getAdapter(this).notifyItemChanged(mLocation + 1);
    }

    private void add() {
        /*SQLiteDatabase db = mDbHelper.getWritableDatabase();
        CustomEngineParcelable parcelable = getParcelable();
        parcelable.data.id = CustomEngine.getAvailableId();
        mData.add(parcelable.data);

        ContentValues values = new ContentValues();
        values.put(CustomEngineTable.COLUMN_ID, parcelable.data.id);
        values.put(CustomEngineTable.COLUMN_DATA, ParcelableUtils.marshall(parcelable));

        db.insert(CustomEngineTable.TABLE_NAME, null, values);*/

        CustomEngineParcelable parcelable = new CustomEngineParcelable();
        parcelable.data = getData();
        parcelable.data.id = CustomEngine.getAvailableId();
        parcelable.data.enabled = 1;

        CustomEngine.addEngineToDb(this, parcelable, parcelable.data.id);
        CustomEngine.addEngineToList(parcelable.data);

        EditSitesActivity.getAdapter(this).notifyItemInserted(mData.size() - 1);
    }

    private CustomEngine getData() {
        CustomEngine data = new CustomEngine();
        data.name = mEditTextName.getText().toString();
        data.upload_url = mEditTextUrl.getText().toString();
        data.post_file_key = mEditTextFileKey.getText().toString();
        data.result_open_action = mSpinner.getSelectedItemPosition();

        data.post_text_key.clear();
        data.post_text_value.clear();
        data.post_text_type.clear();

        for (int i = 0; i < mRecyclerView.getChildCount(); i++) {
            View view = mRecyclerView.getChildAt(i);
            if (view == null)
                continue;
            EditText key = (EditText) view.findViewById(R.id.editText_key);
            EditText value = (EditText) view.findViewById(R.id.editText_value);
            if (key != null && value != null) {
                data.post_text_key.add(key.getEditableText().toString());
                data.post_text_value.add(value.getEditableText().toString());
                data.post_text_type.add(0);
            }
        }
        return data;
    }

    private boolean check() {
        return mEditTextName.getText().toString().length() != 0
                && (URLUtil.isHttpUrl(mEditTextUrl.getText().toString()) || URLUtil.isHttpsUrl(mEditTextUrl.getText().toString()))
                && mEditTextFileKey.getText().toString().length() != 0;
    }
}
