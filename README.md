---
title: Jeson Android MVP
tags: [java, MVP]
date: 2017/6/2
categories: 技术类
---

# MVP简介
MVP演化自MVC, 全称为 Model View Presenter.

## 特点

1. 将业务逻辑与界面解耦
2. 提升可扩展性, 可测试性, 整洁性和灵活性
3. M V P三层之间使用接口进行通信, 可以独立变化, 理想化情况是依赖接口实现不同(相同)逻辑适配相同(不同)的界面
4. 使类的职责更加单一, 更易于维护

## MVP架构
以技术公司的客户(View), 销售(Presenter), 研发(Model)为模型

![MVP架构示意图.svg-10.8kB][1]

1. Presenter - 交互中间人
Presenter作为View层和Model层的交互中间人, 同时**拥有View和Model成员变量**, 其犹如一位尽职的销售人员(Presenter), 接受客户的请求(View)并根据请求(View)寻找相应的研发人员(Model)进行开发, 研发人员(Model)开发完成通过销售人员(Presenter)将成果交付给客户(View).

2. View - 用户界面
View通常指Activity, Fragment或者某个View控件, 其**拥有**一个为其服务的**Presenter成员变量**. 其犹如一位公司的老客户(View), 通过为其服务的销售人员(Presenter)获取自己想要的技术需求, 但自己会获取什么质量的需求实现完全依赖于销售(Presenter), 被动.

3. Model - 业务逻辑
对于一个结构化的APP来说, Model的功能主要是运行逻辑, 提供数据, 有些时候Model封装了DAO或者网络访问的功能, 它的成员变量里面**没有View和Presenter**. 其犹如一位加班至深夜的程序员(Model), 不停的接收着来自销售(Presenter)的需求, 不知道这些需求有什么用, 也不知道自己的劳动成果(数据结果)会去往何方.

## MVP与MVC和MVVM的区别

### MVC架构

1. View层可以直接与Controller和Model层直接通信
2. Controller起到了事件路由的功能
3. 耦合度较高, View, Controller和Model构成了回路

![MVC架构示意图][2]

### MVVM架构

1. 与MVP非常类似, 只不过View层和ViewModel层是双向绑定, 任何一方发生变化则会反应反应到另外一方
2. MVP中View的更新则需要通过Presenter
3. ViewModel角色需要做的只是部分业务逻辑的处理, 以及修改View或者Model的状态
4. MVVM类似于ListView与Adapter, 数据集之间的关系, ListVew就是View, Adapter就是ViewModel, 数据集就是Model.

![MVVM架构示意图][3]



# 接口和类

## 接口

### IBasicModel

```java
public interface IBasicModel extends IBasicHandler, ILifeRecycle {

    /**
     * 设置工作回调
     */
    void setWorkCallback(IBasicHandler.Callback callback);
}
```

### IBasicPresenter

```java
public interface IBasicPresenter extends IBasicHandler, ILifeRecycle {

}
```

### IBasicView

```java
public interface IBasicView{

    /**
     * 当有新的数据更新时, presentr会调用这个方法
     *
     * @param data 数据更新时的数据载体
     */
    void onDataUpdate(Bundle data);

}
```

### IBasicHandler 
```java
public interface IBasicHandler {

    /**
     * 处理派发下来的任务
     *
     * @param taskType 任务执行时所需的数据类型
     * @param data     任务执行时所需的数据
     * @param callback 任务执行时执行的回调
     */
    void handleTask(Callback callback, int taskType, Bundle data);

    /**
     * 获取数据
     *
     * @param dataType 任务类型, 可根据这个类型获取相应的数据
     * @param data     获取数据时需要传入的参数
     * @return 要获取的数据
     */
    Object getData(int dataType, Bundle data);

    /**
     * 回调接口
     */
    interface Callback{
        void call(int dataType, Bundle data);

        void onError(int dataType, Bundle data);

        void onSuccess(int dataType, Bundle data);

        void onFailed(int dataType, Bundle data);

    }
} 
```

### ILifeRecycle

```java
/**
 * 生命周期接口, 与Activity类似
 */
public interface ILifeRecycle {

    /**
     * 作用与Activity的onCreate(Bundle bundle)类似, 由presenter或者view层调用
     * @param bundle
     */
    void onCreate(Bundle bundle);

    /**
     * 作用与Activity的onStart类似, 由presenter或者view层调用
     */
    void onStart();

    /**
     * 作用与Activity的onResume类似, 由presenter或者view层调用
     */
    void onResume();

    /**
     * 作用与Activity的onPause类似, 由presenter或者view层调用
     */
    void onPause();

    /**
     * 作用与Activity的onStop类似, 由presenter或者view层调用
     */
    void onStop();

    /**
     * 作用与Activity的onDestory类似, 由presenter或者view层调用
     */
    void onDestroy();

}
```

## 类

### BasicPresenter

```java
public abstract class BasicPresenter<T extends IBasicView, K extends IBasicModel> implements IBasicPresenter {

    /**
     * 子线程回调call()时, 使接下来的代码运行在主线程
     */
    private static final int MSG_ON_WORKING_CALLED_ON_UI = 0xF0000001;
    /**
     * 子线程回调onSuccess()时, 使接下来的代码运行在主线程
     */
    private static final int MSG_ON_SUCCESS_CALLED_ON_UI = 0xF0000002;
    /**
     * 子线程回调onFailed()时, 使接下来的代码运行在主线程
     */
    private static final int MSG_ON_FAILED_CALLED_ON_UI = 0xF0000003;
    /**
     * 子线程回调onError()时, 使接下来的代码运行在主线程
     */
    private static final int MSG_ON_ERROR_CALLED_ON_UI = 0xF0000004;
    /**
     * 根据线程id保存一个状态, 决定回调是在子线程或者是在主线程运行
     */
    private Map<Long, Boolean> mCalledStatus;
    private T mBasicView;
    private K mBasicModel;
    private android.os.Handler mUIHandler;

    public BasicPresenter(T view, K model) {
        mBasicView = view;
        mBasicModel = model;
        mUIHandler = new Handler();
        mCalledStatus = new HashMap<>();
        mBasicModel.setWorkCallback(mWorkCallback);
    }

    /**
     * 获取view层
     *
     * @return view层
     */
    protected T getBasicView() {
        return mBasicView;
    }

    /**
     * 获取model层
     *
     * @return model层
     */
    protected K getBasicModel() {
        return mBasicModel;
    }

    /**
     * 获取运行的handler
     *
     * @return
     */
    protected android.os.Handler getHandler() {
        return mUIHandler;
    }

    /**
     * 运行在主线程<br/>
     * 处理来自model层的消息, 运行在主线程,如果onWorkingCalledOnWorkThread()被重写了, 这个方法便不会运行<br/>
     * 如果重写了onWorkingCalledOnWorkThread()这个方法, 如果还想使onWorkingCalledOnUIThread()这个方法运行, 则需要调用super.onWorkingCalledOnWorkThread()<br/>
     * onWorkingCalledOnWorkThread()总是运行在oonWorkingCalledOnUIThread()之前
     *
     * @param dataType 任务类型, 可根据这个类型获取相应的数据
     * @param data     获取数据时需要传入的参数
     */
    protected void onWorkingCalledOnUIThread(int dataType, Bundle data) {
    }

    /**
     * 运行在子线程<br/>
     * 处理来自model层的消息, 运行在子线程, 当这个方法被重写时onWorkingCalledOnUIThread()方法不再运行<br/>
     * 不需要调用super.onWorkingCalledOnWorkThread(dataType, data), 除非你想在这个方法运行完成之后继续运行onWorkingCalledOnUIThread(dataType, data)方法
     *
     * @param dataType 任务类型, 可根据这个类型获取相应的数据
     * @param data     获取数据时需要传入的参数
     */
    protected void onWorkingCalledOnWorkThread(int dataType, Bundle data) {
        mCalledStatus.put(Thread.currentThread().getId(), true);
    }

    /**
     * 使用方法参考onWorkingCalledOnUIThread(int dataType, Bundle data)方法
     */
    protected void onSuccessCalledOnUIThread(int dataType, Bundle data) {
    }

    /**
     * 使用方法参考onWorkingCalledOnWorkThread(int dataType, Bundle data)方法
     */
    protected void onSuccessCalledOnWorkThread(int dataType, Bundle data) {
        mCalledStatus.put(Thread.currentThread().getId(), true);
    }
    
    protected void onFailedCalledOnWorkThread(int dataType, Bundle data) {
        mCalledStatus.put(Thread.currentThread().getId(), true);
    }
    
    protected void onFailedCalledOnUIThread(int dataType, Bundle data) {
    }
    
    protected void onErrorCalledOnWorkThread(int dataType, Bundle data) {
        mCalledStatus.put(Thread.currentThread().getId(), true);
    }
    
    protected void onErrorCalledOnUIThread(int dataType, Bundle data) {
    }

    /**
     * 处理消息, 与handler中的handleMessage(Message msg)作用一致, 运行在主线程
     *
     * @param msg
     */
    protected void handleMessage(Message msg) {
    }

    /**
     * 用于model层的工作回调
     */
    protected IBasicHandler.Callback mWorkCallback = new Callback() {

        @Override
        public void call(int dataType, Bundle data) {
            mCalledStatus.put(Thread.currentThread().getId(), false);
            onWorkingCalledOnWorkThread(dataType, data);
            if (mCalledStatus.get(Thread.currentThread().getId())) {
                Message.obtain(mUIHandler, MSG_ON_WORKING_CALLED_ON_UI, dataType, 0, data).sendToTarget();
            }
        }

        @Override
        public void onSuccess(int dataType, Bundle data) {
            mCalledStatus.put(Thread.currentThread().getId(), false);
            onSuccessCalledOnWorkThread(dataType, data);
            if (mCalledStatus.get(Thread.currentThread().getId())) {
                Message.obtain(mUIHandler, MSG_ON_SUCCESS_CALLED_ON_UI, dataType, 0, data).sendToTarget();
            }
        }

        @Override
        public void onFailed(int dataType, Bundle data) {
            mCalledStatus.put(Thread.currentThread().getId(), false);
            onFailedCalledOnWorkThread(dataType, data);
            if (mCalledStatus.get(Thread.currentThread().getId())) {
                Message.obtain(mUIHandler, MSG_ON_FAILED_CALLED_ON_UI, dataType, 0, data).sendToTarget();
            }
        }

        @Override
        public void onError(int dataType, Bundle data) {
            mCalledStatus.put(Thread.currentThread().getId(), false);
            onErrorCalledOnWorkThread(dataType, data);
            if (mCalledStatus.get(Thread.currentThread().getId())) {
                Message.obtain(mUIHandler, MSG_ON_ERROR_CALLED_ON_UI, dataType, 0, data).sendToTarget();
            }
        }
    };

    private class Handler extends android.os.Handler {

        private Handler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BasicPresenter.MSG_ON_WORKING_CALLED_ON_UI:
                    onWorkingCalledOnUIThread(msg.arg1, (Bundle) msg.obj);
                    break;
                case BasicPresenter.MSG_ON_SUCCESS_CALLED_ON_UI:
                    onSuccessCalledOnUIThread(msg.arg1, (Bundle) msg.obj);
                    break;
                case BasicPresenter.MSG_ON_FAILED_CALLED_ON_UI:
                    onFailedCalledOnUIThread(msg.arg1, (Bundle) msg.obj);
                    break;
                case BasicPresenter.MSG_ON_ERROR_CALLED_ON_UI:
                    onErrorCalledOnUIThread(msg.arg1, (Bundle) msg.obj);
                    break;
                default:
                    BasicPresenter.this.handleMessage(msg);
                    break;
            }
        }
    }

    @Override
    public void onCreate(Bundle bundle) {
        mBasicModel.onCreate(bundle);
    }

    @Override
    public void onResume() {
        mBasicModel.onResume();
    }

    @Override
    public void onPause() {
        mBasicModel.onPause();
    }

    @Override
    public void onStart() {
        mBasicModel.onStart();
    }

    @Override
    public void onStop() {
        mBasicModel.onStop();
    }

    @Override
    public void onDestroy() {
        mBasicModel.onDestroy();
        mBasicView = null;
        mBasicModel = null;
    }

    @Override
    public void handleTask(IBasicHandler.Callback callback, int taskType, Bundle data) {
    }

    @Override
    public Object getData(int dataType, Bundle data) {
        return null;
    }

    protected class Callback implements IBasicHandler.Callback {

        @Override
        public void call(int dataType, Bundle data) {
        }

        @Override
        public void onError(int dataType, Bundle data) {
        }

        @Override
        public void onSuccess(int dataType, Bundle data) {
        }

        @Override
        public void onFailed(int dataType, Bundle data) {
        }
    }
}
```

### BasicModel

```java 
public abstract class BasicModel implements ILifeRecycle, IBasicModel {

    private static final String TAG = "BasicModel";
    /**
     * 自动调整线程数量
     */
    public static final int CACHED_THREAD = 0;
    /**
     * 单线程
     */
    public static final int SINGLE_THREAD = 1;
    private boolean isDestroyed = false;
    private ExecutorService mExecutorService;
    /**
     * 用于子类传送消息, 运行在主线程, handleMessage()会推送至线程池中运行
     */
    private Handler mWorkHandler;
    private IBasicHandler.Callback mWorkingCallback;

    /**
     * 默认model层开启1个子线程
     */
    public BasicModel() {
        this(SINGLE_THREAD);
    }

    /**
     * 可以根据给的线程数量设置model层子线程数量, 如果参数值为CACHED_THREAD, 则使用自动调整线程数量, 如果参数值为SINGLE_THREAD, 则为单线程, 如果是其他值则使用指定的线程数量
     *
     * @param nThreads
     */
    public BasicModel(int nThreads) {
        if (nThreads == CACHED_THREAD) {
            mExecutorService = Executors.newCachedThreadPool();
        } else if (nThreads == SINGLE_THREAD) {
            mExecutorService = Executors.newSingleThreadExecutor();
        } else {
            mExecutorService = Executors.newFixedThreadPool(nThreads);
        }
        mWorkHandler = new Handler();
        //从任务队列中读取任务交给线程去执行
    }

    protected android.os.Handler getWorkHandler() {
        return mWorkHandler;
    }

    @Override
    public void setWorkCallback(Callback callback) {
        mWorkingCallback = callback;
    }

    protected IBasicHandler.Callback getWorkingCallback() {
        return mWorkingCallback;
    }

    protected abstract void handleMessage(Message msg);

    @Override
    public void onCreate(Bundle bundle) {
    }

    @Override
    public void onStart() {
    }

    @Override
    public void onResume() {
    }

    @Override
    public void onPause() {
    }

    @Override
    public void onStop() {
    }

    @Override
    public void onDestroy() {
        isDestroyed = true; //标记当前状态为destroyed
        mExecutorService.shutdownNow(); //关闭线程池
        mExecutorService = null;

    }

    @Override
    public void handleTask(Callback callback, int taskType, Bundle data) {
    }

    @Override
    public Object getData(int dataType, Bundle data) {
        return null;
    }

    private class Runnable implements java.lang.Runnable {
        private Message msg;

        Runnable(Message msg) {
            this.msg = msg;
        }

        @Override
        public void run() {
            handleMessage(msg);
            msg.recycle();// Message对象没有经过Looper循环, 需要手动回收
        }
    }

    private class Handler extends android.os.Handler {

        public Handler() {
            super(Looper.getMainLooper());
        }

        public void dispatchMessage(Message msg) {
            if (!isDestroyed) {
                if (msg.getCallback() != null) {
                    // Looper会自动回收Message对象
                    mExecutorService.execute(msg.getCallback());
                } else {
                    // Looper会自动回收msg对象, 所以需要一个新的Message对象供我们的子线程使用
                    mExecutorService.execute(new Runnable(Message.obtain(msg)));
                }
            }

        }
    }
}
```





  [1]: http://jeson.info/open/android-mvp/raw/master/jesonmvp/doc/img/MVP%E6%9E%B6%E6%9E%84%E7%A4%BA%E6%84%8F%E5%9B%BE.svg
  [2]: http://jeson.info/open/android-mvp/raw/master/jesonmvp/doc/img/MVC%E6%9E%B6%E6%9E%84%E7%A4%BA%E6%84%8F%E5%9B%BE.svg
  [3]: http://jeson.info/open/android-mvp/raw/master/jesonmvp/doc/img/MVVM%E6%9E%B6%E6%9E%84%E7%A4%BA%E6%84%8F%E5%9B%BE.svg