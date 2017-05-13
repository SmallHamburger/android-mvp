package com.jeson.mvp.model;

import com.jeson.mvp.IBasicHandler;
import com.jeson.mvp.ILifeRecycle;

/**
 * Created by jeson on 2017/5/5.
 */

public interface IBasicModel extends IBasicHandler, ILifeRecycle {

    /**
     * 设置工作回调
     */
    void setWorkCallback(IBasicHandler.Callback callback);
}
