package com.jeson.mvp.presenter.impl;

import android.os.Bundle;
import android.os.Looper;
import android.os.Message;

import com.jeson.mvp.IBasicHandler;
import com.jeson.mvp.model.IBasicModel;
import com.jeson.mvp.presenter.IBasicPresenter;
import com.jeson.mvp.view.IBasicView;


/**
 * Created by jeson on 2017/5/10.
 */

public abstract class BasicPresenter<T extends IBasicView, K extends IBasicModel> implements IBasicPresenter {

    private boolean isRunOnUIThread;
    private T mBasicView;
    private K mBasicModel;
    private android.os.Handler mUIHandler = new Handler();

    public BasicPresenter(T view, K model) {
        mBasicView = view;
        mBasicModel = model;
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
     * 处理来自model层的消息, 运行在主线程,如果onWorkingCallOnWorkThread()被重写了, 这个方法便不会运行<br/>
     * 如果重写了onWorkingCallOnWorkThread()这个方法, 还想使onWorkingCallOnUIThread()这个方法运行, 则需要调用super.onWorkingCallOnWorkThread(), 并且onWorkingCallOnWorkThread()总是运行在onWorkingCallOnUIThread()之前
     *
     * @param dataType 任务类型, 可根据这个类型获取相应的数据
     * @param data     获取数据时需要传入的参数
     */
    protected void onWorkingCalledOnUIThread(int dataType, Bundle data) {
    }

    /**
     * 处理来自model层的消息, 运行在子线程, 当这个方法被重写时onWorkingCallOnUIThread()方法不再运行
     *
     * @param dataType 任务类型, 可根据这个类型获取相应的数据
     * @param data     获取数据时需要传入的参数
     */
    protected void onWorkingCalledOnWorkThread(int dataType, Bundle data) {
        isRunOnUIThread = true;
    }

    /**
     * 处理消息, 与handler中的handleMessage(Message msg)作用一致, 运行在主线程
     * @param msg
     */
    protected void handleMessage(Message msg){}

    /**
     * 用于model层的工作回调
     */
    protected IBasicHandler.Callback mWorkCallback = new Callback() {

        @Override
        public void call(int dataType, Bundle data) {
            isRunOnUIThread = false;
            onWorkingCalledOnWorkThread(dataType, data);
            if (isRunOnUIThread) {
                Message.obtain(mUIHandler, MSG_ON_WORKING_CALL_ON_UI, dataType, 0, data).sendToTarget();
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
                case IBasicPresenter.MSG_ON_WORKING_CALL_ON_UI:
                    onWorkingCalledOnUIThread(msg.arg1, (Bundle) msg.obj);
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
