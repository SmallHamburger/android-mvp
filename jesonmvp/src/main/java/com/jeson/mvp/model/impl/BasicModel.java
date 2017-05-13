package com.jeson.mvp.model.impl;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.jeson.mvp.IBasicHandler;
import com.jeson.mvp.ILifeRecycle;
import com.jeson.mvp.model.IBasicModel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * Created by jeson on 2017/5/10.
 */

public abstract class BasicModel implements ILifeRecycle, IBasicModel {

    private static final String TAG = "BasicModel";
    public static final int CACHED_THREAD = -1;
    public static final int SINGLE_THREAD = 0;
    private boolean isDestroyed = false;
    private ExecutorService mExecutorService;
    private Handler mWorkHandler;
    private IBasicHandler.Callback mWorkingCallback;
    private LinkedBlockingQueue<Message> mMessages;

    public BasicModel() {
        this(SINGLE_THREAD);
    }

    public BasicModel(int nThreads) {
        if (nThreads == CACHED_THREAD) {
            mExecutorService = Executors.newCachedThreadPool();
        } else if (nThreads == SINGLE_THREAD) {
            mExecutorService = Executors.newSingleThreadExecutor();
        } else {
            mExecutorService = Executors.newFixedThreadPool(nThreads);
        }
        mMessages = new LinkedBlockingQueue();
        mWorkHandler = new Handler();
        new MessageThread().start();
    }

    protected android.os.Handler getWorkHandler(){
        return mWorkHandler;
    }

    @Override
    public void setWorkCallback(Callback callback) {
        mWorkingCallback = callback;
    }

    protected IBasicHandler.Callback getWorkingCallback(){
        return mWorkingCallback;
    }

    protected abstract void handleMessage(Message msg);

    @Override
    public void onCreate(Bundle bundle) {}

    @Override
    public void onStart() {}

    @Override
    public void onResume() {}

    @Override
    public void onPause() {}

    @Override
    public void onStop() {}

    @Override
    public void onDestroy() {
        isDestroyed = true;
        mMessages.clear();
        mExecutorService.shutdownNow();
        mExecutorService = null;

    }

    @Override
    public void handleTask(Callback callback, int taskType, Bundle data) {}

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
        }
    }

    private class Handler extends android.os.Handler{

        public Handler(){
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            if (!isDestroyed) {
                mMessages.offer(msg);
            }
        }
    }

    private class MessageThread extends Thread{
        @Override
        public void run() {
            for (; !isDestroyed; ) {
                try {
                    mExecutorService.execute(new Runnable(mMessages.take()));
                } catch (InterruptedException e) {
                    Log.w(TAG, "Taking message from message queue is interrupted");
                    e.printStackTrace();
                }
            }
        }
    }
}
