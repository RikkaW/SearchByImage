package rikka.searchbyimage.ui.fragment;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import java.util.List;

import me.qixingchen.settings.DropDownPreference;
import me.qixingchen.settings.PreferenceFragment;
import me.qixingchen.settings.SwitchPreference;
import rikka.searchbyimage.BuildConfig;
import rikka.searchbyimage.R;
import rikka.searchbyimage.staticdata.CustomEngine;
import rikka.searchbyimage.ui.EditSitesActivity;
import rikka.searchbyimage.ui.UploadActivity;
import rikka.searchbyimage.utils.ClipBoardUtils;
import rikka.searchbyimage.utils.CustomTabsHelper;
import rikka.searchbyimage.utils.URLUtils;

/**
 * Created by Rikka on 2015/12/23.
 */
public class SettingsFragment extends PreferenceFragment implements
        SharedPreferences.OnSharedPreferenceChangeListener,
        Preference.OnPreferenceClickListener {

    Activity mActivity;

    PreferenceCategory mCategoryGoogle;
    PreferenceCategory mCategoryIqdb;
    PreferenceCategory mCategoryBaidu;
    PreferenceCategory mCategoryNotice;
    PreferenceCategory mCategoryAdvance;

    SwitchPreference mSafeSearch;
    PreferenceScreen mScreen;
    EditTextPreference mCustomGoogleUri;
    PreferenceCategory mCategorySauceNAO;
    DropDownPreference mSearchEngine;

    Preference mNotice;

    List<CustomEngine> mData;

    private int click = 0;
    private Runnable clearClickCount = new Runnable() {
        @Override
        public void run() {
            click = 0;
        }
    };

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        mActivity = getActivity();

        boolean popup = getArguments().getBoolean("popup");

        if (popup) {
            //if (!BuildConfig.hideOtherEngine)
            addPreferencesFromResource(R.xml.preferences_general_mini);

            addPreferencesFromResource(R.xml.preferences_search_settings);
        } else {
            addPreferencesFromResource(R.xml.preferences_usage);
            addPreferencesFromResource(/*BuildConfig.hideOtherEngine ? R.xml.preferences_general_gp : */R.xml.preferences_general);
            addPreferencesFromResource(R.xml.preferences_search_settings);
            addPreferencesFromResource(R.xml.preferences_about);
        }


        mCategoryGoogle = (PreferenceCategory) findPreference("category_google");
        mCategoryIqdb = (PreferenceCategory) findPreference("category_iqdb");
        mCategorySauceNAO = (PreferenceCategory) findPreference("category_saucenao");
        mCategoryBaidu = (PreferenceCategory) findPreference("category_baidu");
        mCategoryAdvance = (PreferenceCategory) findPreference("category_advance");

        mData = CustomEngine.getList(mActivity);
        mSearchEngine = (DropDownPreference) findPreference("search_engine_preference");

        mSafeSearch = (SwitchPreference) findPreference("safe_search_preference");
        mScreen = (PreferenceScreen) findPreference("screen");
        mCustomGoogleUri = (EditTextPreference) findPreference("google_region");
        mNotice = (Preference) findPreference("preference_notice");

        setCustomGoogleUriHide();
        //setSearchEngineHide();

        /*if (BuildConfig.hideOtherEngine) {
            mSafeSearch.setEnabled(false);
            mSafeSearch.setChecked(true);
        }*/

        if (!CustomTabsHelper.getIsChromeInstalled(mActivity)) {
            DropDownPreference showResultInPreference = (DropDownPreference) findPreference("show_result_in");
            showResultInPreference.removeItem(1);
        }

        if (!popup) {
            /*SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
            if (sharedPreferences.getBoolean("developer", false)) {
                mScreen.addPreference(mCategoryAdvance);
            } else {
                mScreen.removePreference(mCategoryAdvance);
            }*/

            Preference versionPref = findPreference("version");
            versionPref.setOnPreferenceClickListener(this);

            Preference githubPref = findPreference("open_source");
            githubPref.setOnPreferenceClickListener(this);

            Preference advancePref = findPreference("advance");
            if (advancePref != null) {
                advancePref.setOnPreferenceClickListener(this);
            }

            Preference donatePref = findPreference("donate");
            if (BuildConfig.hideOtherEngine) {
                ((PreferenceCategory) findPreference("about")).removePreference(donatePref);
            } else {
                donatePref.setOnPreferenceClickListener(this);
            }

            try {
                versionPref.setSummary(mActivity.getPackageManager().getPackageInfo(mActivity.getPackageName(), 0).versionName);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);

        addCustomEngines();

        mSearchEngine.setCallback(new DropDownPreference.Callback() {
            @Override
            public boolean onItemSelected(int pos, Object value) {
                SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();

                sharedPreferences.edit()
                        .putString("search_engine_id", (String) value)
                        .apply();

                setSearchEngineHide(Integer.parseInt((String) value));

                return true;
            }
        });
    }

    private void addCustomEngines() {
        /*int count = mSearchEngine.getItemCount();
        for (int i = 6; i < count; i++) {
            mSearchEngine.removeItem(6);
        }*/

        mSearchEngine.clearItems();

        for (CustomEngine item : mData) {
            if (item.getEnabled() == 1) {
                mSearchEngine.addItem(item.getName(), Integer.toString(item.getId()));
            }
        }

        SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();

        mSearchEngine.setSelectedItem(0);
        mSearchEngine.setSelectedValue(sharedPreferences.getString("search_engine_id", "0"));

        setSearchEngineHide(Integer.parseInt((String) mSearchEngine.getSelectedValue()));
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);

        mSearchEngine.setCallback(null);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        /*if (key.equals("search_engine_preference")) {
            setSearchEngineHide();
        }*/

        if (key.equals("google_region_preference")) {
            setCustomGoogleUriHide();
        }
    }

    private void setCustomGoogleUriHide() {
        SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();

        boolean customRedirect = sharedPreferences.getString("google_region_preference", "0").equals("2");

        if (customRedirect) {
            mCategoryGoogle.addPreference(mCustomGoogleUri);
        } else {
            mCategoryGoogle.removePreference(mCustomGoogleUri);
        }
    }

    private void setSearchEngineHide(int siteId) {
        switch (siteId) {
            case UploadActivity.SITE_GOOGLE:
                mScreen.addPreference(mCategoryGoogle);
                mScreen.removePreference(mCategoryIqdb);
                mScreen.removePreference(mCategorySauceNAO);
                mScreen.removePreference(mCategoryBaidu);
                break;
            case UploadActivity.SITE_IQDB:
                mScreen.removePreference(mCategoryGoogle);
                mScreen.addPreference(mCategoryIqdb);
                mScreen.removePreference(mCategorySauceNAO);
                mScreen.removePreference(mCategoryBaidu);
                break;
            case UploadActivity.SITE_SAUCENAO:
                mScreen.removePreference(mCategoryGoogle);
                mScreen.removePreference(mCategoryIqdb);
                mScreen.addPreference(mCategorySauceNAO);
                mScreen.removePreference(mCategoryBaidu);
                break;
            case UploadActivity.SITE_BAIDU:
                mScreen.removePreference(mCategoryGoogle);
                mScreen.removePreference(mCategoryIqdb);
                mScreen.removePreference(mCategorySauceNAO);
                mScreen.addPreference(mCategoryBaidu);
                break;
            case UploadActivity.SITE_ASCII2D:
            case UploadActivity.SITE_TINEYE:
            default:
                mScreen.removePreference(mCategoryGoogle);
                mScreen.removePreference(mCategoryIqdb);
                mScreen.removePreference(mCategorySauceNAO);
                mScreen.removePreference(mCategoryBaidu);
                break;
        }

        /*switch (siteId) {
            case UploadActivity.SITE_BAIDU:
                mScreen.addPreference(mCategoryNotice);
                mNotice.setSummary(R.string.notice_baidu);
                break;
            case UploadActivity.SITE_GOOGLE:
            case UploadActivity.SITE_IQDB:
            case UploadActivity.SITE_SAUCENAO:
            case UploadActivity.SITE_ASCII2D:
            case UploadActivity.SITE_TINEYE:
                mScreen.removePreference(mCategoryNotice);
                break;
        }*/
    }

    int mIsRed = 0;

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();

        switch (key) {
            case "advance":
                startActivity(new Intent(mActivity, EditSitesActivity.class));
                break;
            case "version":
                getActivity().getWindow().getDecorView().removeCallbacks(clearClickCount);
                getActivity().getWindow().getDecorView().postDelayed(clearClickCount, 3000);

                click++;


                //startActivity(new Intent(mActivity, EditSitesActivity.class));

                if (click == 5)
                    Toast.makeText(mActivity, "OAO", Toast.LENGTH_SHORT).show();
                else if (click == 10)
                    Toast.makeText(mActivity, "><", Toast.LENGTH_SHORT).show();
                else if (click == 15)
                    Toast.makeText(mActivity, "www", Toast.LENGTH_SHORT).show();
                else if (click == 25)
                    Toast.makeText(mActivity, "QAQ", Toast.LENGTH_SHORT).show();
                else if (click == 40) {
                    Toast.makeText(mActivity, "2333", Toast.LENGTH_SHORT).show();
/*
                    int color[][] = {
                            {
                                    ContextCompat.getColor(getActivity(), R.color.colorPrimary),
                                    Color.parseColor("#F44336")
                            },
                            {
                                    ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark),
                                    Color.parseColor("#D32F2F")
                            }
                    };


                    final Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);

                    colorAnimation(
                            color[0][mIsRed],
                            color[0][1 - mIsRed],
                            250,
                            new ValueAnimator.AnimatorUpdateListener() {

                                @Override
                                public void onAnimationUpdate(ValueAnimator animator) {
                                    toolbar.setBackgroundColor((int) animator.getAnimatedValue());
                                }
                            });

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        colorAnimation(
                                color[1][mIsRed],
                                color[1][1 - mIsRed],
                                250,
                                new ValueAnimator.AnimatorUpdateListener() {
                                    @Override
                                    public void onAnimationUpdate(ValueAnimator animator) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                            getActivity().getWindow().setStatusBarColor((int) animator.getAnimatedValue());
                                        }
                                    }
                                });

                        getActivity().setTaskDescription(new ActivityManager.TaskDescription(
                                getActivity().getTitle().toString(),
                                null,
                                color[1][1 - mIsRed]));


                    }
                    mIsRed = 1 - mIsRed;*/
                    click = -10;

                    /*SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
                    sharedPreferences.edit().putBoolean("developer", true).apply();

                    mScreen.addPreference(mCategoryAdvance);
                    Preference advancePref = findPreference("advance");
                    if (advancePref != null) {
                        advancePref.setOnPreferenceClickListener(this);
                    }*/
                    /*View view = mActivity.findViewById(R.id.settings_container);
                    view.animate()
                            .rotation(view.getRotation() + 180 + 360)
                            .setDuration(3000)
                            .setInterpolator(new FastOutSlowInInterpolator())
                            .start();*/
                }

                break;

            case "open_source":
                URLUtils.Open("https://github.com/RikkaW/SearchByImage", mActivity);
                break;
            case "donate":
                ClipBoardUtils.putTextIntoClipboard(mActivity, "rikka@xing.moe");
                Toast.makeText(mActivity, String.format(getString(R.string.copy_to_clipboard), "rikka@xing.moe"), Toast.LENGTH_SHORT).show();

                break;
        }

        return false;
    }

    void colorAnimation(int colorFrom, int colorTo, int duration, ValueAnimator.AnimatorUpdateListener listener) {
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.setDuration(duration);
        colorAnimation.addUpdateListener(listener);
        colorAnimation.start();
    }
}