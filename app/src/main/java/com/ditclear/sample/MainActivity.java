package com.ditclear.sample;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    Random r = new Random();
    private Toolbar mToolbar;
    private SearchView mSearchView;
    private RecyclerView mRecyclerView;
    private RecyclerAdapter mAdapter;

    private int mode = 0;

    Map<Integer,String > modeMap=new ArrayMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        initData();
    }

    private void initData() {
        mAdapter=new RecyclerAdapter(this);
        modeMap.put(0,"Handler");
        modeMap.put(1,"Executor");
        modeMap.put(2,"RxJava");

    }

    /**
     * 初始化Executor 数据
     */
    private void initExecutorData() {
        mHandler=new MyHandler();
    }

    private void initView() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mSearchView = (SearchView) findViewById(R.id.searchView);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        setSupportActionBar(mToolbar);

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

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
    }

    private List<String> fakeData() {
        int n = r.nextInt(10);
        List<String> data = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            data.add("item" + i);

        }
        return data;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_task:
                mode=0;
                break;
            case R.id.menu_executor:
                mode=1;
                initExecutorData();
                break;
            case R.id.menu_rxjava:
                mode=2;
                break;

        }
        return super.onOptionsItemSelected(item);
    }


    private void queryMatch(String query) {
        Toast.makeText(this, String.format(getString(R.string.toast_format)
                ,modeMap.get(mode),query), Toast.LENGTH_SHORT).show();
        //模拟网络请求
        mAdapter.setList(fakeData());
        mRecyclerView.setAdapter(mAdapter);

    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (!TextUtils.isEmpty(newText)) {
            switch (mode){
                case 0:
                    queryWithHandler(newText);
                    break;
                case 1:
                    queryWithExecutor(newText);
                    break;
                case 2:
                    break;
                default:
                    queryWithHandler(newText);
                    break;
            }
        }else {
            mAdapter.clear();
            mRecyclerView.setAdapter(mAdapter);
        }
        return true;
    }

    private void queryWithExecutor(String newText) {
        showSearchTip(newText);
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

    /**********************************Handler**********************************/

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

    /**********************************Executor**********************************/

    private MyHandler mHandler ;
    // 创建 SingleThreadExecutor
    ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

    public ScheduledFuture<?> schedule(Runnable command, long delayTimeMills) {
        return scheduledExecutor.schedule(command, delayTimeMills, TimeUnit.MILLISECONDS);
    }

    public void showSearchTip(String newText) {
        // 延迟500毫秒
        schedule(new SearchThread(newText), 500);
    }

    class SearchThread implements Runnable {

        String newText;

        public SearchThread(String newText) {
            this.newText = newText;
        }

        public void run() {
            // keep only one thread to load current search tip, u can get data from network here
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
