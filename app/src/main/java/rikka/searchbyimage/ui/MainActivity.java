package rikka.searchbyimage.ui;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import java.io.FileNotFoundException;

import rikka.searchbyimage.R;
import rikka.searchbyimage.SearchByImageApplication;
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

        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT <= 19) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(intent, 1);
                    }
                } else if (Build.VERSION.SDK_INT > 19) {
                    /*Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(intent, 1);
                    }*/

                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/*");
                    startActivityForResult(intent, 1);
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {

            if (requestCode == 1) {
                Uri uri = data.getData();

                SearchByImageApplication application = (SearchByImageApplication) getApplication();
                try {
                    application.setImageInputStream(getContentResolver().openInputStream(uri));

                    Intent intent = new Intent(this, UploadActivity.class);
                    intent.putExtra(UploadActivity.EXTRA_URI2, uri);
                    startActivity(intent);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}