package com.jeson.mvp.view;

import android.os.Bundle;

import com.jeson.mvp.IBasicHandler;


/**
 * Created by jeson on 2017/4/25.
 */

public interface IBasicView{

    /**
     * 当有新的数据更新时, presentr会调用这个方法
     *
     * @param data 数据更新时的数据载体
     */
    void onDataUpdate(Bundle data);

}
