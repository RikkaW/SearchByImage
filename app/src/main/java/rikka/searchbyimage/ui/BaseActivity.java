package rikka.searchbyimage.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import moe.xing.daynightmode.BaseDayNightModeActivity;
import rikka.searchbyimage.SearchByImageApplication;

/**
 * Created by Rikka on 2016/3/3.
 */
public abstract class BaseActivity extends BaseDayNightModeActivity {
    private Tracker mTracker;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTracker = ((SearchByImageApplication) getApplication()).getDefaultTracker();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTracker.setScreenName(this.getClass().getSimpleName());
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }
}
