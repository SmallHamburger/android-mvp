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
    private LinkedBlockingQueue<Message> mMessages;

    /**
     * 默认model层开启1个子线程
     */
    public BasicModel() {
        this(SINGLE_THREAD);
    }

    /**
     * 可以根据给的线程数量设置model层子线程数量, 如果参数值为CACHED_THREAD, 则使用自动调整线程数量, 如果参数值为SINGLE_THREAD, 则为单线程, 如果是其他值则使用指定的线程数量
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
        mMessages = new LinkedBlockingQueue();
        mWorkHandler = new Handler();
        //从任务队列中读取任务交给线程去执行
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
        isDestroyed = true; //标记当前状态为destroyed
        mMessages.clear();//清理队列
        mExecutorService.shutdownNow(); //关闭线程池
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
                    // 如果队列有任务则take, 如果没有则阻塞
                    // 如果有了新的任务进入, 则唤醒
                    mExecutorService.execute(new Runnable(mMessages.take()));
                } catch (InterruptedException e) {
                    Log.w(TAG, "Taking message from message queue is interrupted");
                    e.printStackTrace();
                }
            }
        }
    }
}
