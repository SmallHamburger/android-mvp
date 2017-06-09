package com.jeson.mvp.presenter;


import com.jeson.mvp.IBasicHandler;
import com.jeson.mvp.ILifeRecycle;

/**
 * Created by jeson on 2017/5/5.
 */

public interface IBasicPresenter extends IBasicHandler, ILifeRecycle {

    /**
     * 手动回调结束当前Presenter
     */
    void finish();

}
