package com.jeson.mvp;

import android.os.Bundle;

/**
 * 生命周期接口, 与Activity类似
 * Created by jeson on 2017/5/10.
 */

public interface ILifeRecycle {

    /**
     * 作用与Activity的onCreate(Bundle bundle)类似, 由presenter或者view层调用
     *
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
     * 作用与Activity的onDestroy类似, 由presenter或者view层调用
     */
    void onDestroy();

    enum LifeStatus {
        ON_CREATE(1 << 0), ON_START(1 << 5), ON_RESUME(1 << 10), ON_PAUSE(1 << 15), ON_STOP(1 << 20), ON_DESTROY(1 << 25);

        public final int value;

        LifeStatus(int value) {
            this.value = value;
        }

    }

}
