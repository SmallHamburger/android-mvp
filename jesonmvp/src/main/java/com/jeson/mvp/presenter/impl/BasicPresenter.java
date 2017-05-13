package com.jeson.mvp.presenter.impl;

import android.os.Bundle;
import android.os.Looper;
import android.os.Message;

import com.jeson.mvp.IBasicHandler;
import com.jeson.mvp.model.IBasicModel;
import com.jeson.mvp.presenter.IBasicPresenter;
import com.jeson.mvp.view.IBasicView;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by jeson on 2017/5/10.
 */

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
