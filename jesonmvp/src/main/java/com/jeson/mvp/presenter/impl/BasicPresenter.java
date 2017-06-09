package com.jeson.mvp.presenter.impl;

import android.app.Activity;
import android.app.Application;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;

import com.jeson.mvp.IBasicHandler;
import com.jeson.mvp.utils.NotCalledInCreateMethodException;
import com.jeson.mvp.utils.SuperNotCalledException;
import com.jeson.mvp.model.IBasicModel;
import com.jeson.mvp.presenter.IBasicPresenter;
import com.jeson.mvp.view.IBasicView;

import java.util.HashMap;
import java.util.Map;

public abstract class BasicPresenter<T extends IBasicView, K extends IBasicModel> implements IBasicPresenter {

    private static final String TAG = "BasicPresenter";
    private static boolean DEBUG_LIFECYCLE = false;
    /**
     * 检查自动销毁的间隔
     */
    private static final long DESTROY_CHECKER_DURATION = 1000;
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
    /**
     * View层
     */
    private T mBasicView;
    /**
     * Model层
     */
    private K mBasicModel;
    /**
     * 运行在主线程的Handler
     */
    private android.os.Handler mUIHandler;
    /**
     * 当前是否被销毁
     */
    private boolean isDestroyed = false;
    /**
     * 宿主Activity
     */
    private Activity mHostActivity;
    /**
     * 生命周期托管回调
     */
    private ActivityLifecycleCallbacks mActivityLifecycleCallbacks;
    /**
     * 当前状态的生命周期
     */
    private LifeStatus mLifeStatus = LifeStatus.ON_CREATE;
    /**
     * 用于判断某个方法是否被调用过
     */
    private boolean isCalled = false;
    /**
     * 表明onCreate()方法是否已经被调用过
     */
    private boolean isCreate = false;

    public BasicPresenter(T view, K model) {
        mBasicView = view;
        mBasicModel = model;
        mUIHandler = new Handler();
        mCalledStatus = new HashMap<>();
        mBasicModel.setWorkCallback(mWorkCallback);
        // 将Presenter的生命周期托付给宿主Activity
        mHostActivity = getViewActivity();
        if (mHostActivity != null) {
            Application application = mHostActivity.getApplication();
            if (application == null) {
                throw new NotCalledInCreateMethodException("BasicPresenter " + BasicPresenter.this
                        + " did not call in activity's onCreate() or after activity's onCreate()");
            }
            mActivityLifecycleCallbacks = new ActivityLifecycleCallbacks();
            application.registerActivityLifecycleCallbacks(mActivityLifecycleCallbacks);
        }
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
            if (isDestroyed) {
                return;
            }
            mCalledStatus.put(Thread.currentThread().getId(), false);
            onWorkingCalledOnWorkThread(dataType, data);
            if (mCalledStatus.get(Thread.currentThread().getId())) {
                Message.obtain(mUIHandler, MSG_ON_WORKING_CALLED_ON_UI, dataType, 0, data).sendToTarget();
            }
        }

        @Override
        public void onSuccess(int dataType, Bundle data) {
            if (isDestroyed) {
                return;
            }
            mCalledStatus.put(Thread.currentThread().getId(), false);
            onSuccessCalledOnWorkThread(dataType, data);
            if (mCalledStatus.get(Thread.currentThread().getId())) {
                Message.obtain(mUIHandler, MSG_ON_SUCCESS_CALLED_ON_UI, dataType, 0, data).sendToTarget();
            }
        }

        @Override
        public void onFailed(int dataType, Bundle data) {
            if (isDestroyed) {
                return;
            }
            mCalledStatus.put(Thread.currentThread().getId(), false);
            onFailedCalledOnWorkThread(dataType, data);
            if (mCalledStatus.get(Thread.currentThread().getId())) {
                Message.obtain(mUIHandler, MSG_ON_FAILED_CALLED_ON_UI, dataType, 0, data).sendToTarget();
            }
        }

        @Override
        public void onError(int dataType, Bundle data) {
            if (isDestroyed) {
                return;
            }
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
            if (isDestroyed) {
                return;
            }
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

    public void finish() {
        onDestroy();
    }

    private void moveLife(LifeStatus lifeStatus, Bundle savedInstanceState) {
        switch (lifeStatus) {
            case ON_CREATE:
                if (mLifeStatus.value <= LifeStatus.ON_CREATE.value) {
                    isCreate = true;
                    getBasicModel().onCreate(savedInstanceState);
                    mLifeStatus = LifeStatus.ON_CREATE;
                }
                break;
            case ON_START:
                if (mLifeStatus.value <= LifeStatus.ON_CREATE.value) {
                    if (!isCreate) {
                        isCalled = false;
                        onCreate(null);
                        if (!isCalled) {
                            throw new SuperNotCalledException("BasicPresenter " + BasicPresenter.this
                                    + " did not call through to super.onCreate()");
                        }
                    }
                    getBasicModel().onStart();
                    mLifeStatus = LifeStatus.ON_START;
                }
                break;
            case ON_RESUME:
                if (mLifeStatus.value < LifeStatus.ON_RESUME.value) {
                    getBasicModel().onStart();
                    mLifeStatus = LifeStatus.ON_RESUME;
                }
                break;
            case ON_PAUSE:
                mLifeStatus = LifeStatus.ON_PAUSE;
                getBasicModel().onPause();
                break;
            case ON_STOP:
                if (mLifeStatus.value < LifeStatus.ON_PAUSE.value) {
                    isCalled = false;
                    onPause();
                    if (!isCalled) {
                        throw new SuperNotCalledException("BasicPresenter " + BasicPresenter.this
                                + " did not call through to super.onPause()");
                    }
                    getBasicModel().onStop();
                    mLifeStatus = LifeStatus.ON_STOP;
                }
                break;
            case ON_DESTROY:
                if (!isDestroyed && mLifeStatus.value < LifeStatus.ON_DESTROY.value) {
                    isCalled = false;
                    onStop();
                    if (!isCalled) {
                        throw new SuperNotCalledException("BasicPresenter " + BasicPresenter.this
                                + " did not call through to super.onStop()");
                    }
                    getBasicModel().onDestroy();
                    isDestroyed = true;
                    mBasicModel.onDestroy();
                    mHostActivity.getApplication().unregisterActivityLifecycleCallbacks(mActivityLifecycleCallbacks);
                    mBasicView = null;
                    mBasicModel = null;
                    mHostActivity = null;
                    mLifeStatus = LifeStatus.ON_DESTROY;
                }
                break;
        }
    }

    /**
     * 获取宿主Activity
     *
     * @return
     */
    private Activity getViewActivity() {
        if (mBasicView instanceof Activity) {
            return (Activity) mBasicView;
        }
        if (mBasicView instanceof Fragment) {
            return ((Fragment) mBasicView).getActivity();
        }
        if (mBasicView instanceof View) {
            Context context = ((View) mBasicView).getContext();
            if (context != null && context instanceof Activity) {
                return (Activity) context;
            }
        }
        return null;
    }

    /**
     * 生命周期回调, 通过宿主Activity将其托付给Application
     */
    private class ActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            if (activity == mHostActivity) {
                isCalled = false;
                onCreate(savedInstanceState);
                if (!isCalled) {
                    throw new SuperNotCalledException("BasicPresenter " + BasicPresenter.this
                            + " did not call through to super.onCreate()");
                }
            }
        }

        @Override
        public void onActivityStarted(Activity activity) {
            if (activity == mHostActivity) {
                isCalled = false;
                onStart();
                if (!isCalled) {
                    throw new SuperNotCalledException("BasicPresenter " + BasicPresenter.this
                            + " did not call through to super.onStart()");
                }
            }
        }

        @Override
        public void onActivityResumed(Activity activity) {
            if (activity == mHostActivity) {
                isCalled = false;
                onResume();
                if (!isCalled) {
                    throw new SuperNotCalledException("BasicPresenter " + BasicPresenter.this
                            + " did not call through to super.onResume()");
                }
            }
        }

        @Override
        public void onActivityPaused(Activity activity) {
            if (activity == mHostActivity) {
                isCalled = false;
                onPause();
                if (!isCalled) {
                    throw new SuperNotCalledException("BasicPresenter " + BasicPresenter.this
                            + " did not call through to super.onPause()");
                }
            }
        }

        @Override
        public void onActivityStopped(Activity activity) {
            if (activity == mHostActivity) {
                isCalled = false;
                onStop();
                if (!isCalled) {
                    throw new SuperNotCalledException("BasicPresenter " + BasicPresenter.this
                            + " did not call through to super.onStop()");
                }
            }
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            if (activity == mHostActivity) {
                isCalled = false;
                onDestroy();
                if (!isCalled) {
                    throw new SuperNotCalledException("BasicPresenter " + BasicPresenter.this
                            + " did not call through to super.onDestroy()");
                }
            }
        }
    }

    @Override
    public void onCreate(Bundle bundle) {
        moveLife(LifeStatus.ON_CREATE, bundle);
        if (DEBUG_LIFECYCLE) {
            Log.d(TAG, "BasicPresenter: onCreate " + this);
        }
        isCalled = true;
    }

    @Override
    public void onStart() {
        moveLife(LifeStatus.ON_START, null);
        if (DEBUG_LIFECYCLE) {
            Log.d(TAG, "BasicPresenter: onStart " + this);
        }
        isCalled = true;
    }

    @Override
    public void onResume() {
        moveLife(LifeStatus.ON_RESUME, null);
        if (DEBUG_LIFECYCLE) {
            Log.d(TAG, "BasicPresenter: onResume " + this);
        }
        isCalled = true;
    }

    @Override
    public void onPause() {
        moveLife(LifeStatus.ON_PAUSE, null);
        if (DEBUG_LIFECYCLE) {
            Log.d(TAG, "BasicPresenter: onPause " + this);
        }
        isCalled = true;
    }

    @Override
    public void onStop() {
        moveLife(LifeStatus.ON_STOP, null);
        if (DEBUG_LIFECYCLE) {
            Log.d(TAG, "BasicPresenter: onStop " + this);
        }
        isCalled = true;
    }

    @Override
    public void onDestroy() {
        moveLife(LifeStatus.ON_DESTROY, null);
        if (DEBUG_LIFECYCLE) {
            Log.d(TAG, "BasicPresenter: onDestroy " + this);
        }
        isCalled = true;
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


    /**
     * 设置是否debug生命周期
     *
     * @param debugLifecycle
     */
    protected void setDebugLifecycle(boolean debugLifecycle) {
        DEBUG_LIFECYCLE = debugLifecycle;
    }

    protected boolean isDestroyed(){
        return isDestroyed;
    }

}
