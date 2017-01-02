package com.ditclear.sample;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HandlerActivity extends AppCompatActivity implements SearchView.OnQueryTextListener{
    Random r = new Random();
    private Toolbar mToolbar;
    private SearchView mSearchView;
    private RecyclerView mRecyclerView;
    private RecyclerAdapter mAdapter,mRecentAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initView();

        initData();
    }

    private void initData() {
        mAdapter = new RecyclerAdapter(this);
        mRecentAdapter = new RecyclerAdapter(this);
        mRecentAdapter.setList(fakeData("item"));
    }

    private void initView() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mSearchView = (SearchView) findViewById(R.id.searchView);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        setSupportActionBar(mToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                // 避免输入框隐藏
                return true;
            }
        });
        mSearchView.onActionViewExpanded();
        mSearchView.setIconified(false);
        mSearchView.setQueryHint("请输入关键词");
        mSearchView.setOnQueryTextListener(this);

        mRecyclerView.setAdapter(mRecentAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));

    }

    private List<String> fakeData(String query) {
        List<String> data = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            data.add(query +  i);

        }
        return data;
    }

    private void queryMatch(String query) {
        Toast.makeText(this, String.format(getString(R.string.toast_format)
                ,"handler+thread",query), Toast.LENGTH_SHORT).show();
        //模拟网络请求
        mAdapter.setList(fakeData(query));
        mRecyclerView.setAdapter(mAdapter);

    }
    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }
    @Override
    public boolean onQueryTextChange(String newText) {
        if (!TextUtils.isEmpty(newText)) {
            queryWithHandler(newText);
        }
        return true;
    }

    private void queryWithHandler(String newText) {
        // 延迟
        if (delayQueryTask != null) {
            delayQueryTask.cancel();
            handler.removeCallbacksAndMessages(null);
        }
        delayQueryTask = new DelayQueryRunnable(newText);
        handler.postDelayed(delayQueryTask, 500);
    }

    private Handler handler = new Handler();
    private DelayQueryRunnable delayQueryTask;
    private class DelayQueryRunnable implements Runnable {
        private boolean canceled = false;
        String mText;
        public DelayQueryRunnable(String text) {
            this.mText = text;
        }

        @Override
        public void run() {
            if (canceled) {
                return;
            }
            queryMatch(mText);
        }

        public void cancel() {
            canceled = true;
        }
    }
}
