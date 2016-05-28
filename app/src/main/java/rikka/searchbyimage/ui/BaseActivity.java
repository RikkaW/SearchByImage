package rikka.searchbyimage.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

import moe.xing.daynightmode.BaseDayNightModeActivity;

/**
 * Created by Rikka on 2016/3/3.
 */
public class BaseActivity extends BaseDayNightModeActivity {
    protected Activity mActivity;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = this;
    }
}
