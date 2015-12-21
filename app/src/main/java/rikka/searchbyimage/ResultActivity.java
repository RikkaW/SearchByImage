package rikka.searchbyimage;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;

import rikka.searchbyimage.apdater.RecyclerViewAdapter;
import rikka.searchbyimage.utils.IqdbResultCollecter;
import rikka.searchbyimage.widget.SimpleDividerItemDecoration;

public class ResultActivity extends AppCompatActivity {
    RecyclerView mRecyclerView;
    RecyclerViewAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        ArrayList<IqdbResultCollecter.IqdbItem> list;

        Intent intent = getIntent();
        if (intent.hasExtra("EXTRA_INPUT")) {
            //mWebView.loadData(intent.getStringExtra("EXTRA_INPUT"), "text/html", "UTF-8");
            //mWebView.loadUrl("file://" + intent.getStringExtra("EXTRA_INPUT"));
            list = loadSearchResult(intent.getStringExtra("EXTRA_INPUT"));

            mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
            //mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
            mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getApplicationContext()));

            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            mRecyclerView.setHasFixedSize(true);

            mAdapter = new RecyclerViewAdapter(list);
            mAdapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {

                @Override
                public void onItemClick(View view, int position, IqdbResultCollecter.IqdbItem item) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(item.imageURL));
                    startActivity(intent);
                }

                @Override
                public void onItemLongClick(View view, int position, IqdbResultCollecter.IqdbItem item) {
                }
            });

            mRecyclerView.setAdapter(mAdapter);
        }


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
