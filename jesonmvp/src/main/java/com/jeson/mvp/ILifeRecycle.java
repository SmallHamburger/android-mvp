package com.jeson.mvp;

import android.os.Bundle;

/**
 * 生命周期接口, 与Activity类似
 * Created by jeson on 2017/5/10.
 */

public interface ILifeRecycle {

    /**
     * 作用与Activity的onCreate(Bundle bundle)类似, 由presenter或者view层调用
     * @param bundle
     */
    void onCreate(Bundle bundle);

    /**
     * 作用与Activity的onStart类似, 由presenter或者view层调用
     */
    void onStart();

    /**
     * 作用与Activity的onResume类似, 由presenter或者view层调用
     */
    void onResume();

    /**
     * 作用与Activity的onPause类似, 由presenter或者view层调用
     */
    void onPause();

    /**
     * 作用与Activity的onStop类似, 由presenter或者view层调用
     */
    void onStop();

    /**
     * 作用与Activity的onDestory类似, 由presenter或者view层调用
     */
    void onDestroy();

}
