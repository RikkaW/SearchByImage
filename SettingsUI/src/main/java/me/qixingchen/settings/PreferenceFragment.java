package me.qixingchen.settings;


import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v14.preference.EditTextPreferenceDialogFragment;
import android.support.v14.preference.ListPreferenceDialogFragment;
import android.support.v14.preference.MultiSelectListPreference;
import android.support.v14.preference.MultiSelectListPreferenceDialogFragment;
import android.support.v7.preference.Preference;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Rikka on 2016/2/19.
 */
public abstract class PreferenceFragment extends android.support.v14.preference.PreferenceFragment {
    private static final String DIALOG_FRAGMENT_TAG =
            "android.support.v14.preference.PreferenceFragment.DIALOG";

    public void addDefaultListDivider() {

        // hide Google's divider added in 23.2.0
        setDivider(null);

        RecyclerView listView = getListView();
        listView.addItemDecoration(new BaseRecyclerViewItemDecoration(getActivity()) {
            @Override
            public boolean canDraw(RecyclerView parent, View child, int childCount, int position) {
                return (position < childCount - 1
                        && parent.getChildAt(position + 1).findViewById(android.R.id.summary) != null
                        && child.findViewById(android.R.id.summary) != null);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        addDefaultListDivider();
        return view;
    }


    @Override
    public void onStart() {
        super.onStart();
        getPreferenceManager().setOnPreferenceTreeClickListener(this);
        getPreferenceManager().setOnDisplayPreferenceDialogListener(this);
    }
    @Override
    public void onStop() {
        super.onStop();
        getPreferenceManager().setOnPreferenceTreeClickListener(null);
        getPreferenceManager().setOnDisplayPreferenceDialogListener(null);
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        boolean handled = false;
        if (getCallbackFragment() instanceof OnPreferenceDisplayDialogCallback) {
            handled = ((OnPreferenceDisplayDialogCallback) getCallbackFragment())
                    .onPreferenceDisplayDialog(this, preference);
        }
        if (!handled && getActivity() instanceof OnPreferenceDisplayDialogCallback) {
            handled = ((OnPreferenceDisplayDialogCallback) getActivity())
                    .onPreferenceDisplayDialog(this, preference);
        }
        if (handled) {
            return;
        }
        // check if dialog is already showing
        if (getFragmentManager().findFragmentByTag(DIALOG_FRAGMENT_TAG) != null) {
            return;
        }
        final DialogFragment f;
        if (preference instanceof EditTextPreference) {
            f = EditTextPreferenceDialogFragment.newInstance(preference.getKey());
        } else if (preference instanceof ListPreference) {
            f = ListPreferenceDialogFragment.newInstance(preference.getKey());
        } else if (preference instanceof MultiSelectListPreference) {
            f = MultiSelectListPreferenceDialogFragment.newInstance(preference.getKey());
        } else {
            throw new IllegalArgumentException("Tried to display dialog for unknown " +
                    "preference type. Did you forget to override onDisplayPreferenceDialog()?");
        }
        f.setTargetFragment(this, 0);
        f.show(getFragmentManager(), DIALOG_FRAGMENT_TAG);
    }
}
