package rikka.searchbyimage.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import rikka.searchbyimage.R;
import rikka.searchbyimage.ui.fragment.SettingsFragment;


public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            SettingsFragment fragment = new SettingsFragment();
            Bundle bundle = new Bundle();
            bundle.putBoolean("popup", false);
            fragment.setArguments(bundle);

            getFragmentManager().beginTransaction().replace(R.id.settings_container,
                    fragment).commit();
        }
    }
}