package com.jeson.mvp;

import android.os.Bundle;

/**
 * Created by jeson on 2017/5/5.
 */

public interface IBasicHandler {

    /**
     * 处理派发下来的任务
     *
     * @param taskType 任务执行时所需的数据类型
     * @param data     任务执行时所需的数据
     * @param callback 任务执行时执行的回调
     */
    void handleTask(Callback callback, int taskType, Bundle data);

    /**
     * 获取数据
     *
     * @param dataType 任务类型, 可根据这个类型获取相应的数据
     * @param data     获取数据时需要传入的参数
     * @return 要获取的数据
     */
    Object getData(int dataType, Bundle data);

    /**
     * 回调接口
     */
    interface Callback{
        void call(int dataType, Bundle data);

        void onError(int dataType, Bundle data);

        void onSuccess(int dataType, Bundle data);

        void onFailed(int dataType, Bundle data);

    }
}
