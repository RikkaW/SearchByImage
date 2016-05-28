package rikka.searchbyimage.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.io.FileNotFoundException;

import rikka.searchbyimage.R;
import rikka.searchbyimage.SearchByImageApplication;
import rikka.searchbyimage.ui.fragment.SettingsFragment;
import rikka.searchbyimage.utils.IntentUtils;


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
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(intent, 1);
                    }
                } else {
                    /*Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(intent, 1);
                    }*/

                    /*Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/*");
                    startActivityForResult(intent, 1);*/

                    Intent intentFromGallery = new Intent(Intent.ACTION_GET_CONTENT, null);
                    intentFromGallery.setType("image/*");
                    intentFromGallery.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
                    IntentUtils.startOtherActivity(activity, intentFromGallery);

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