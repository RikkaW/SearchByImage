package rikka.searchbyimage;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v14.preference.PreferenceFragment;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().replace(R.id.settings_container,
                    new SettingsFragment()).commit();
        }

    }

    public static class SettingsFragment extends PreferenceFragment implements
            OnSharedPreferenceChangeListener,
            Preference.OnPreferenceClickListener {

        Context mContext;

        PreferenceCategory mCategoryGoogle;
        PreferenceCategory mCategoryIqdb;
        SwitchPreference mSafeSearch;
        PreferenceScreen mScreen;
        EditTextPreference mCustomGoogleUri;

        //        @Override
        //        public void onCreate(Bundle savedInstanceState) {
        //            super.onCreate(savedInstanceState);
        //            addPreferencesFromResource(R.xml.preferences);
        //
        //            mCategoryGoogle = (PreferenceCategory) findPreference("category_google");
        //            mCategoryIqdb = (PreferenceCategory) findPreference("category_iqdb");
        //            mSafeSearch = (SwitchPreference) findPreference("safe_search_preference");
        //            mScreen = (PreferenceScreen) findPreference("screen");
        //            mCustomGoogleUri = (EditTextPreference) findPreference("google_region");
        //
        //            setSafeSearchHide();
        //            setCustomGoogleUriHide();
        //
        //            mContext = getActivity();
        //
        //            Preference versionPref = findPreference("version");
        //            versionPref.setOnPreferenceClickListener(this);
        //
        //            try {
        //                versionPref.setSummary(mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName);
        //            } catch (PackageManager.NameNotFoundException e) {
        //                e.printStackTrace();
        //            }
        //        }
        private int click = 0;
        private Runnable clearClickCount = new Runnable() {
            @Override
            public void run() {
                click = 0;
            }
        };

        @Override
        public void onCreatePreferences(Bundle bundle, String s) {
            setPreferencesFromResource(R.xml.preferences, s);
            mCategoryGoogle = (PreferenceCategory) findPreference("category_google");
            mCategoryIqdb = (PreferenceCategory) findPreference("category_iqdb");
            mSafeSearch = (SwitchPreference) findPreference("safe_search_preference");
            mScreen = (PreferenceScreen) findPreference("screen");
            mCustomGoogleUri = (EditTextPreference) findPreference("google_region");

            setSafeSearchHide();
            setCustomGoogleUriHide();

            mContext = getActivity();

            Preference versionPref = findPreference("version");
            versionPref.setOnPreferenceClickListener(this);

            try {
                versionPref.setSummary(mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals("search_engine_preference")) {
                setSafeSearchHide();
            }

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

        private void setSafeSearchHide() {
            SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();

            int siteId = Integer.parseInt(sharedPreferences.getString("search_engine_preference", "0"));

            switch (siteId) {
                case 0: {
                    mScreen.addPreference(mCategoryGoogle);
                    mScreen.removePreference(mCategoryIqdb);
                    break;
                }
                case 1: {
                    mScreen.removePreference(mCategoryGoogle);
                    mScreen.removePreference(mCategoryIqdb);
                    break;
                }
                case 2: {
                    mScreen.removePreference(mCategoryGoogle);
                    mScreen.addPreference(mCategoryIqdb);
                    break;
                }
            }
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            getActivity().getWindow().getDecorView().removeCallbacks(clearClickCount);
            getActivity().getWindow().getDecorView().postDelayed(clearClickCount, 3000);

            click++;

            if (click == 5)
                Toast.makeText(mContext, "OAO", Toast.LENGTH_SHORT).show();
            else if (click == 10)
                Toast.makeText(mContext, "><", Toast.LENGTH_SHORT).show();
            else if (click == 15)
                Toast.makeText(mContext, "www", Toast.LENGTH_SHORT).show();
            else if (click == 25) {
                Toast.makeText(mContext, "QAQ", Toast.LENGTH_SHORT).show();

                click = -10;
            }

            return false;
        }
    }
}