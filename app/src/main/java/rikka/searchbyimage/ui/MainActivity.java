package rikka.searchbyimage.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import rikka.searchbyimage.R;


public class MainActivity extends BaseActivity {

    public static final String EXTRA_MINI =
            "rikka.searchbyimage.ui.MainActivity.EXTRA_MINI";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            SettingsFragment fragment = new SettingsFragment();
            Bundle bundle = new Bundle();
            bundle.putBoolean(SettingsFragment.ARG_POPUP, getIntent().getBooleanExtra(EXTRA_MINI, false));
            fragment.setArguments(bundle);

            getSupportFragmentManager().beginTransaction().replace(R.id.settings_container,
                    fragment).commit();
        }

        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                } else {
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
                }
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, 1);
                } else {
                    Toast.makeText(MainActivity.this, R.string.target_app_not_found, Toast.LENGTH_LONG).show();
                }
            }
        });

        if (getIntent().getBooleanExtra(EXTRA_MINI, false)) {
            findViewById(R.id.fab).setVisibility(View.GONE);

            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle(R.string.settings);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                Uri uri = data.getData();
                Intent intent = new Intent(this, UploadActivity.class);
                intent.putExtra(UploadActivity.EXTRA_URI, uri);
                startActivity(intent);
            }
        }
    }
}