package com.jeson.mvp.presenter;


import com.jeson.mvp.IBasicHandler;
import com.jeson.mvp.ILifeRecycle;

/**
 * Created by jeson on 2017/5/5.
 */

public interface IBasicPresenter extends IBasicHandler, ILifeRecycle {

    /**
     * 子线程回调时, 使接下来的代码运行在主线程
     */
    int MSG_ON_WORKING_CALL_ON_UI = 0xF0000001;
}
