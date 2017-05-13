package com.jeson.mvp.app;

import android.os.Message;

import com.jeson.mvp.model.impl.BasicModel;

/**
 * Created by Windows on 2017/5/13.
 */

public class Model extends BasicModel implements IModel{

    private static final int START_ACTION = 0;

    @Override
    protected void handleMessage(Message msg) {
        getWorkingCallback().call(0, null);
    }

    @Override
    public void startAction() {
        getWorkHandler().sendEmptyMessage(START_ACTION);
    }
}
