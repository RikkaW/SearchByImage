package rikka.searchbyimage.apdater;

import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Rikka on 2016/12/11.
 */

abstract public class FilterAdapterHelper<T> {

    private RecyclerView.Adapter mAdapter;

    private List<T> mOriginalData;
    private List<T> mFilteredData;

    private String mKeyword;
    private boolean mIsSearching;
    private SparseIntArray mIntKeys;
    private SparseBooleanArray mBooleanKeys;

    public FilterAdapterHelper(RecyclerView.Adapter adapter) {
        mAdapter = adapter;
        mIntKeys = new SparseIntArray();
        mBooleanKeys = new SparseBooleanArray();

        mOriginalData = new ArrayList<>();
        mFilteredData = new ArrayList<>();
    }

    public List<T> getOriginalData() {
        return mOriginalData;
    }

    public void setOriginalData(Collection<T> originalData) {
        mOriginalData.clear();
        mOriginalData.addAll(originalData);
        mFilteredData = filter(mOriginalData);
    }

    public void setOriginalData(List<T> originalData) {
        mOriginalData = originalData;
        mFilteredData = filter(mOriginalData);
    }

    public List<T> getFilteredData() {
        return mFilteredData;
    }

    public T get(int index) {
        return mFilteredData.get(index);
    }

    public void setKeyword(String keyword) {
        if (mKeyword != null && mKeyword.equals(keyword)) {
            return;
        }

        mKeyword = keyword;
        mFilteredData = filter(mOriginalData);
        mAdapter.notifyDataSetChanged();
        //mAdapter.requestResetData();
    }

    public void setSearching(boolean searching) {
        mIsSearching = searching;
        mFilteredData = filter(mOriginalData);
        mAdapter.notifyDataSetChanged();
        //mAdapter.requestResetData();
    }

    public void putKey(int key, boolean value) {
        putKey(key, value, true);
    }

    public void putKey(int key, boolean value, boolean refreshData) {
        mBooleanKeys.put(key, value);
        if (refreshData) {
            mFilteredData = filter(mOriginalData);
        }
    }

    public void putKey(int key, int value) {
        putKey(key, value, true);
    }

    public void putKey(int key, int value, boolean refreshData) {
        mIntKeys.put(key, value);
        if (refreshData) {
            mFilteredData = filter(mOriginalData);
        }
    }

    private List<T> filter(List<T> list) {
        List<T> newList = new ArrayList<>();

        if (list == null) {
            return newList;
        }

        for (T obj : list) {
            boolean check = true;
            for (int i = 0; i < mIntKeys.size(); i++) {
                int key = mIntKeys.keyAt(i);
                if (!check(key, mIntKeys.get(key), obj)) {
                    check = false;
                    break;
                }
            }
            for (int i = 0; i < mBooleanKeys.size(); i++) {
                int key = mBooleanKeys.keyAt(i);
                if (!check(key, mBooleanKeys.get(key), obj)) {
                    check = false;
                    break;
                }
            }
            if (!check) {
                continue;
            }

            if (mIsSearching && !contains(mKeyword, obj)) {
                continue;
            }

            newList.add(obj);
        }
        return newList;
    }

    abstract public boolean contains(String key, T obj);

    abstract public boolean check(int key, int value, T obj);

    abstract public boolean check(int key, boolean value, T obj);
}
