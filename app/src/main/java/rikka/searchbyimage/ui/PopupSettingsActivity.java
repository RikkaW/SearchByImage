package rikka.searchbyimage.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import rikka.searchbyimage.R;
import rikka.searchbyimage.ui.fragment.SettingsFragment;

public class PopupSettingsActivity extends BaseActivity {

    public static final String EXTRA_URI =
            "rikka.searchbyimage.ui.ResultActivity.EXTRA_URI";

    private Uri mUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.close);

        if (savedInstanceState == null) {
            SettingsFragment fragment = new SettingsFragment();
            Bundle bundle = new Bundle();
            bundle.putBoolean("popup", true);
            fragment.setArguments(bundle);

            getSupportFragmentManager().beginTransaction().replace(R.id.settings_container,
                    fragment).commit();
        }

        mUri = getIntent().getParcelableExtra(EXTRA_URI);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.ok:
                Intent intent = new Intent(this, UploadActivity.class);
                intent.putExtra(UploadActivity.EXTRA_URI, mUri);
                intent.putExtra(UploadActivity.EXTRA_SAVE_FILE, false);
                startActivity(intent);

                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
