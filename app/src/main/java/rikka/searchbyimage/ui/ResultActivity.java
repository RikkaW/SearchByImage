package rikka.searchbyimage.ui;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;

import rikka.searchbyimage.R;
import rikka.searchbyimage.ui.apdater.RecyclerViewAdapter;
import rikka.searchbyimage.utils.ClipBoardUtils;
import rikka.searchbyimage.utils.IqdbResultCollecter;
import rikka.searchbyimage.utils.URLUtils;
import rikka.searchbyimage.widget.SimpleDividerItemDecoration;

public class ResultActivity extends AppCompatActivity {

    public static final String EXTRA_FILE =
            "rikka.searchbyimage.ui.ResultActivity.EXTRA_FILE";

    public static final String EXTRA_SITE_ID =
            "rikka.searchbyimage.ui.ResultActivity.EXTRA_SITE_ID";

    RecyclerView mRecyclerView;
    RecyclerViewAdapter mAdapter;
    Activity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        mActivity = this;

        ArrayList<IqdbResultCollecter.IqdbItem> list;

        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_FILE)) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                int siteId = intent.getIntExtra(EXTRA_SITE_ID, 2);
                String siteName = getResources().getStringArray(R.array.search_engines)[siteId];

                setTaskDescription(new ActivityManager.TaskDescription(
                        String.format(getString(R.string.search_result), siteName),
                        null,
                        getResources().getColor(R.color.colorPrimary)));
            }

            list = loadSearchResult(intent.getStringExtra(EXTRA_FILE));

            mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
            //mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
            mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getApplicationContext()));

            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            mRecyclerView.setHasFixedSize(true);

            mAdapter = new RecyclerViewAdapter(list);
            mAdapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {

                @Override
                public void onItemClick(View view, int position, IqdbResultCollecter.IqdbItem item) {
                    /*Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(item.imageURL));
                    startActivity(intent);*/

                    URLUtils.Open(item.imageURL, mActivity);
                }

                @Override
                public void onItemLongClick(View view, int position, final IqdbResultCollecter.IqdbItem item) {
                    new AlertDialog.Builder(mActivity)
                            .setItems(
                                    new CharSequence[] {getString(R.string.open_with), getString(R.string.copy_link)},
                                    new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case 0:
                                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.imageURL));
                                                    mActivity.startActivity(intent);
                                                    break;
                                                case 1:
                                                    ClipBoardUtils.putTextIntoClipboard(mActivity, item.imageURL);
                                                    Toast.makeText(mActivity, String.format(getString(R.string.copy_to_clipboard), item.imageURL), Toast.LENGTH_SHORT).show();
                                                    break;
                                            }
                                        }
                                    })
                            .show();
                }
            });

            mRecyclerView.setAdapter(mAdapter);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    private ArrayList<IqdbResultCollecter.IqdbItem> loadSearchResult(String htmlFilePath) {
        File file = new File(htmlFilePath);

        BufferedInputStream fileStream = null;
        StringBuilder sb = new StringBuilder();

        try {
            byte[] buffer = new byte[4096];

            fileStream = new BufferedInputStream(new FileInputStream(file));
            while ((fileStream.read(buffer)) != -1) {
                sb.append(new String(buffer, Charset.forName("UTF-8")));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileStream != null)
                try {
                    fileStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }

       return IqdbResultCollecter.getItemList(sb.toString());
    }
}
