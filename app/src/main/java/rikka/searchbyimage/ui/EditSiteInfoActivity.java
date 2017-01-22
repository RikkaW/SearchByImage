package rikka.searchbyimage.ui;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;

import rikka.searchbyimage.staticdata.SearchEngine;
import rikka.searchbyimage.staticdata.SearchEngineParcelable;
import rikka.searchbyimage.widget.MyLinearLayoutManager;
import rikka.searchbyimage.widget.DropDown;
import rikka.searchbyimage.BuildConfig;
import rikka.searchbyimage.R;
import rikka.searchbyimage.database.DatabaseHelper;
import rikka.searchbyimage.database.table.CustomEngineTable;
import rikka.searchbyimage.apdater.PostFormAdapter;
import rikka.searchbyimage.utils.ParcelableUtils;
import rikka.searchbyimage.utils.BrowsersUtils;

public class EditSiteInfoActivity extends BaseActivity {
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
    TextInputLayout mTextInputName;
    TextInputLayout mTextInputUrl;
    TextView mFormTitle;
    DropDown mSpinner;
    RecyclerView mRecyclerView;

    List<SearchEngine> mData;
    SearchEngine mItem;
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

        /*if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            final Drawable upArrow = ContextCompat.getDrawable(this, R.drawable.abc_ic_ab_back_material);
            upArrow.setColorFilter(ContextCompat.getColor(this, android.R.color.white), PorterDuff.Mode.SRC_ATOP);
            mToolbar.setNavigationIcon(upArrow);
        }*/

        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);

        mEditTextName = (EditText) findViewById(R.id.edit_name);
        mEditTextUrl = (EditText) findViewById(R.id.edit_url);
        mEditTextFileKey = (EditText) findViewById(R.id.edit_file_key);

        mFormTitle = (TextView) findViewById(R.id.post_form_title);

        mTextInputName = (TextInputLayout) findViewById(R.id.textInupt_name);
        mTextInputUrl = (TextInputLayout) findViewById(R.id.textInput_url);

        mSpinner = (DropDown) findViewById(R.id.dropDown);
        mSpinner.setAdapter(ArrayAdapter.createFromResource(this,
                R.array.custom_open_with, android.R.layout.simple_spinner_dropdown_item));

        mDbHelper = DatabaseHelper.instance(this);
        mData = SearchEngine.getList(this);

        mFAB = (FloatingActionButton) findViewById(R.id.fab);
        mFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mEnabled) {
                    onBackPressed();
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

                onBackPressed();
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
                mEditTextUrl.setText(mItem.getUploadUrl());
                mEditTextName.setText(mItem.getName());
                mEditTextFileKey.setText(mItem.getPostFileKey());
                if (mItem.getResultOpenAction() <= SearchEngine.RESULT_OPEN_ACTION.OPEN_HTML_FILE) {
                    mSpinner.setSelection(mItem.getResultOpenAction());
                }

                mEnabled = (mItem.getId() > 5);
                mAdapter = new PostFormAdapter(mItem, mEnabled);
                mRecyclerView.setAdapter(mAdapter);


                if (!mEnabled) {
                    mTextInputName.setEnabled(false);
                    mTextInputUrl.setEnabled(false);
                    mEditTextUrl.setEnabled(false);
                    mEditTextName.setEnabled(false);
                    mEditTextFileKey.setEnabled(false);
                    mSpinner.setEnabled(false);
                    mSpinner.setAdapter(ArrayAdapter.createFromResource(this,
                            R.array.custom_open_with_in_app, android.R.layout.simple_spinner_dropdown_item));
                    mSpinner.setSelection(mItem.getResultOpenAction());
                }

            } else {
                mLocation = -1;
            }
        } else {
            mAdapter = new PostFormAdapter();
            mRecyclerView.setAdapter(mAdapter);
        }

        mEditTextFileKey.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                setFormTitleColor(hasFocus);
            }
        });

        mAdapter.setOnFocusChangeListener(new PostFormAdapter.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                setFormTitleColor(hasFocus);
            }
        });

        mEditTextName.addTextChangedListener(new TextChangeRemoveErrorTextWatcher(mTextInputName));
        mEditTextUrl.addTextChangedListener(new TextChangeRemoveErrorTextWatcher(mTextInputUrl));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!BuildConfig.hideOtherEngine) {
            getMenuInflater().inflate(R.menu.edit_info, menu);
            return true;
        }

        return true;
    }

    /**
     * set FormTitle Color
     * if control view get focus ,set color to colorPrimary
     * otherwise,set color to default
     *
     * @param hasFocus if control view has focus
     */
    private void setFormTitleColor(boolean hasFocus) {
        if (hasFocus) {
            //mFormTitle.setTextColor(ContextCompat.getColor(mActivity, R.color.colorPrimary));
            mFormTitle.setTextColor(ContextCompat.getColor(mActivity, R.color.colorAccent));
        } else {
            mFormTitle.setTextColor(ContextCompat.getColor(mActivity, R.color.primary_text  ));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.help:
                BrowsersUtils.open(
                        this,
                        "https://github.com/RikkaW/SearchByImage/wiki/%E5%B8%AE%E5%8A%A9%EF%BC%9A%E8%87%AA%E5%AE%9A%E4%B9%89%E6%90%9C%E7%B4%A2%E5%BC%95%E6%93%8E",
                        false);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void modify() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        SearchEngineParcelable parcelable = new SearchEngineParcelable();
        parcelable.data = getData();

        ContentValues values = new ContentValues();
        values.put(CustomEngineTable.COLUMN_DATA, ParcelableUtils.marshall(parcelable));

        String selection = CustomEngineTable.COLUMN_ID + " LIKE ?";
        String[] selectionArgs = {String.valueOf(mItem.getId())};

        db.update(
                CustomEngineTable.TABLE_NAME,
                values,
                selection,
                selectionArgs);

        mItem.setName(parcelable.data.getName());
        mItem.setUploadUrl(parcelable.data.getUploadUrl());
        mItem.setPostFileKey(parcelable.data.getPostFileKey());
        mItem.setResultOpenAction(parcelable.data.getResultOpenAction());
        mItem.post_text_key = parcelable.data.post_text_key;
        mItem.post_text_value = parcelable.data.post_text_value;
        mItem.post_text_type = parcelable.data.post_text_type;

        EditSitesActivity.getAdapter(this).notifyItemChanged(mLocation);
    }

    private void add() {
        /*SQLiteDatabase db = mDbHelper.getWritableDatabase();
        SearchEngineParcelable parcelable = getParcelable();
        parcelable.data.id = SearchEngine.getAvailableId();
        mData.add(parcelable.data);

        ContentValues values = new ContentValues();
        values.put(CustomEngineTable.COLUMN_ID, parcelable.data.id);
        values.put(CustomEngineTable.COLUMN_DATA, ParcelableUtils.marshall(parcelable));

        db.insert(CustomEngineTable.TABLE_NAME, null, values);*/

        SearchEngineParcelable parcelable = new SearchEngineParcelable();
        parcelable.data = getData();
        parcelable.data.setId(SearchEngine.getAvailableId());
        parcelable.data.setEnabled(1);

        SearchEngine.addEngineToDb(this, parcelable, parcelable.data.getId());
        SearchEngine.addEngineToList(parcelable.data);

        EditSitesActivity.getAdapter(this).notifyItemInserted(mData.size() - 1);
        EditSitesActivity.getAdapter(this).notifyItemChanged(mData.size() - 2);
    }

    private SearchEngine getData() {
        SearchEngine data = new SearchEngine();
        data.setName(mEditTextName.getText().toString());
        data.setUploadUrl(mEditTextUrl.getText().toString());
        data.setPostFileKey(mEditTextFileKey.getText().toString());
        data.setResultOpenAction(mSpinner.getSelectedItemPosition());

        if (!URLUtil.isNetworkUrl(data.getUploadUrl())) {
            data.setUploadUrl("http://" + data.getUploadUrl());
        }

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
        if (mEditTextName.getText().toString().length() == 0) {
            mTextInputName.setError(getString(R.string.not_empty));
        }

        if (!Patterns.WEB_URL.matcher(mEditTextUrl.getText().toString()).matches()) {
            mTextInputUrl.setError(getString(R.string.invalid_web_url));
        }

        if (mEditTextUrl.getText().toString().length() == 0) {
            mTextInputUrl.setError(getString(R.string.not_empty));
        }

        return mEditTextName.getText().toString().length() != 0
                && Patterns.WEB_URL.matcher(mEditTextUrl.getText().toString()).matches()
                && mEditTextFileKey.getText().toString().length() != 0;
    }

    private class TextChangeRemoveErrorTextWatcher implements TextWatcher {
        TextInputLayout mTextInputLayout;

        public TextChangeRemoveErrorTextWatcher(TextInputLayout textInputLayout) {
            mTextInputLayout = textInputLayout;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            mTextInputLayout.setErrorEnabled(false);
        }
    }
}
