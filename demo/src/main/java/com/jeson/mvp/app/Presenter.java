package com.jeson.mvp.app;

import android.os.Bundle;

import com.jeson.mvp.presenter.impl.BasicPresenter;

/**
 * Created by Windows on 2017/5/13.
 */

public class Presenter extends BasicPresenter<IView, IModel> implements IPresenter{

    private IView mBasicView;
    private IModel mBasicModel;

    public Presenter(IView view) {
        super(view, new Model());
        mBasicView = view;
        mBasicModel = getBasicModel();
    }

    @Override
    public void startAction() {
        mBasicModel.startAction();
    }

    @Override
    protected void onWorkingCalledOnUIThread(int dataType, Bundle data) {
        mBasicView.onDataUpdate(data);
    }
}
