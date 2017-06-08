package com.jeson.mvp.utils;

import android.util.AndroidRuntimeException;

/**
 * 异常类, 表明该方法没有在Activity的onCreate方法内或之后调用
 */

public class NotCalledInCreateMethodException extends AndroidRuntimeException {
    public NotCalledInCreateMethodException(String msg) {
        super(msg);
    }
}
