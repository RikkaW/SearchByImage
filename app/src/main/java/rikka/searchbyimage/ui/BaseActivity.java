package rikka.searchbyimage.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;

/**
 * Created by Rikka on 2016/3/3.
 */
public abstract class BaseActivity extends AppCompatActivity {
    //private Tracker mTracker;

    static {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //mTracker = ((Application) getApplication()).getDefaultTracker();
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*mTracker.setScreenName(this.getClass().getSimpleName());
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());*/
    }
}
