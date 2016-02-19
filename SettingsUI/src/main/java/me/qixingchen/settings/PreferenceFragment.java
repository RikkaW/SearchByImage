package me.qixingchen.settings;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Rikka on 2016/2/19.
 */
public abstract class PreferenceFragment extends android.support.v14.preference.PreferenceFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        RecyclerView listView = getListView();
        listView.addItemDecoration(new BaseRecyclerViewItemDecoration(container.getContext()) {
            @Override
            public boolean canDraw(RecyclerView parent, View child, int childCount, int position) {
                return ((position < childCount - 1)
                        && parent.getChildAt(position + 1).findViewById(android.R.id.summary) != null
                        && child.findViewById(android.R.id.summary) != null);
            }
        });

        return view;
    }
}
