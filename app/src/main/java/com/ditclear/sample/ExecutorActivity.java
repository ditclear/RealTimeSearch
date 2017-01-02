package com.ditclear.sample;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ExecutorActivity extends AppCompatActivity implements SearchView.OnQueryTextListener{
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
        mHandler=new MyHandler();

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
                return false;
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
                ,"Executor+Future",query), Toast.LENGTH_SHORT).show();
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
            showSearchTip(newText);
        }
        return true;
    }

    private MyHandler mHandler ;
    private Future<?> mFuture;
    // 创建 SingleThreadExecutor
    ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

    public ScheduledFuture<?> schedule(Runnable command, long delayTimeMills) {
        return scheduledExecutor.schedule(command, delayTimeMills, TimeUnit.MILLISECONDS);
    }

    public void showSearchTip(String newText) {
        if (mFuture!=null){
            mFuture.cancel(true);
        }
        // 延迟500毫秒
        mFuture=schedule(new SearchThread(newText), 500);
    }

    class SearchThread implements Runnable {

        String newText;

        public SearchThread(String newText) {
            this.newText = newText;
        }

        public void run() {
            if (!TextUtils.isEmpty(newText)) {
                mHandler.sendMessage(mHandler.obtainMessage(1, newText ));
            }
        }
    }

    private class MyHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            mHandler.removeMessages(1);
            switch (msg.what) {
                case 1:
                    queryMatch((String) msg.obj);
                    break;
            }
        }
    }
}
