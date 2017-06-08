package com.jeson.mvp.utils;

import android.util.AndroidRuntimeException;

/**
 * 异常类, 表明父类没有被调用
 */

public class SuperNotCalledException extends AndroidRuntimeException {
    public SuperNotCalledException(String msg) {
        super(msg);
    }
}
