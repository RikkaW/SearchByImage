package rikka.searchbyimage.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;

import rikka.searchbyimage.R;
import rikka.searchbyimage.apdater.ResultAdapter;
import rikka.searchbyimage.utils.ClipBoardUtils;
import rikka.searchbyimage.utils.IntentUtils;
import rikka.searchbyimage.utils.IqdbResultCollecter;
import rikka.searchbyimage.utils.BrowsersUtils;

public class IqdbResultActivity extends BaseResultActivity {

    public static final String EXTRA_FILE =
            "rikka.searchbyimage.ui.IqdbResultActivity.EXTRA_FILE";

    RecyclerView mRecyclerView;
    ResultAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        ArrayList<IqdbResultCollecter.IqdbItem> list;

        if (!getIntent().hasExtra(EXTRA_FILE)) {
            finish();
        }

        list = loadSearchResult(getIntent().getStringExtra(EXTRA_FILE));

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        //mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
            /*mRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            });*/

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(true);

        mAdapter = new ResultAdapter(list);
        mAdapter.setOnItemClickListener(new ResultAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(View view, int position, IqdbResultCollecter.IqdbItem item) {
                BrowsersUtils.open(IqdbResultActivity.this, item.imageURL, false);
            }

            @Override
            public void onItemLongClick(View view, int position, final IqdbResultCollecter.IqdbItem item) {
                new AlertDialog.Builder(IqdbResultActivity.this)
                        .setItems(
                                new CharSequence[]{getString(R.string.open_with), getString(R.string.copy_link)},
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which) {
                                            case 0:
                                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.imageURL));
                                                IntentUtils.startOtherActivity(IqdbResultActivity.this, intent);
                                                break;
                                            case 1:
                                                ClipBoardUtils.putTextIntoClipboard(IqdbResultActivity.this, item.imageURL);
                                                Toast.makeText(IqdbResultActivity.this, String.format(getString(R.string.copy_to_clipboard), item.imageURL), Toast.LENGTH_SHORT).show();
                                                break;
                                        }
                                    }
                                })
                        .show();
            }
        });

        mRecyclerView.setAdapter(mAdapter);
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
