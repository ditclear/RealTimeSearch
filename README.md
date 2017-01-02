
#RealTimeSearch

android实现延迟搜索（简书地址：http://www.jianshu.com/p/13774ed60f22）

#### 场景

在进行搜索功能开发(特别是需要从网络或者本地加载)的时候，为了给用户以更好体验：

比如用户想要搜索“abc”,如果每次输入的文字变化都执行一次请求(确实很垃圾)，那么就会陆续搜索“a”，“ab”,“abc”。这还是在搜索比较少的情况下，如果搜索字数较多，又或者网络状况不好，那么用户的体验一定会很差，所以节流就很有必要性。通常的做法便是：

**设置一个延迟时间，过滤掉变化过快的字符**

![screenshot.gif](https://github.com/ditclear/RealTimeSearch/blob/master/screenshot.gif?raw=true)

而实现的方式，我总结了有以下三种：
* Handler+Thread
* Executor+Future
* RxJava

apk地址：[Demo](https://github.com/ditclear/RealTimeSearch/tree/master/apk)

#### 实现（延迟500ms搜索）

**1.Handler+Thread**

第一种方式使用我们都很熟悉的Handler,配合Hanlder的`removeCallbacks`方法，或者`removeMessages`方法移除Callback/Messages。延迟的话使用`postDelayed`方法就可以实现。

```java
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

    private class DelayQueryRunnable implements Runnable {
        String mText;
        private boolean canceled = false;

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
```



**2.Executor+Future**

Future模式可以这样来描述：我有一个任务，提交给了Future，Future替我完成这个任务。期间我自己可以去做任何想做的事情。一段时间之后，我就便可以从Future那儿取出结果

在onQueryTextChange函数即输入框内容每次变化时将一个数据获取线程`SearchThread`放到`scheduledExecutor`中，如果当前正在执行搜索，那么取消这个任务重新搜索，避免不必要的数据获取和多个搜索提示同时出现。

```java
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
          	//取消任务，true代表立即取消
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
```



**3.RxJava**

使用rxjava最简单，使用[PublishSubject](http://reactivex.io/documentation/subject.html)和[Debounce](http://reactivex.io/documentation/operators/debounce.html)操作符很容易实现延迟搜索。`PublishSubject`既是发送者也是接收者，可以用于接收变化的字符串，而`Debounce`操作符会过滤掉发射速率过快的数据项，更多优化可以看这一篇[使用RxJava 提升用户体验](http://www.jianshu.com/p/33c548bce571)。

```java

    private PublishSubject<String > mSubject=PublishSubject.create();

	private void initData() {
       //其他操作
      
        mSubject.debounce(500, TimeUnit.MILLISECONDS)//延迟500ms
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Action1<String>() {
                    @Override
                    public void call(String s) {
                      	//从网络或者本地搜索
                        queryMatch(s);
                    }
                }).subscribe();    
    }

	@Override
    public boolean onQueryTextChange(String newText) {
        if (!TextUtils.isEmpty(newText)) {
            queryWithRxJava(newText);
        }
        return true;
    }

    private void queryWithRxJava(String newText) {
      	//发送数据源
        mSubject.onNext(newText);
    }
```

#### 结语

三种方法介绍完毕，也许可以做些优化，使用`HandlerThread`什么的，或者还有其他更好的方法，欢迎交流。
