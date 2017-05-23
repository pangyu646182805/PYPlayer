package com.neuroandroid.pyplayer.mvp.presenter;


import com.neuroandroid.pyplayer.base.BasePresenter;
import com.neuroandroid.pyplayer.base.BaseResponse;
import com.neuroandroid.pyplayer.model.response.User;
import com.neuroandroid.pyplayer.mvp.contract.ILoginContract;
import com.neuroandroid.pyplayer.mvp.model.ILoginModel;
import com.neuroandroid.pyplayer.mvp.model.impl.LoginModelImpl;
import com.neuroandroid.pyplayer.net.RetrofitCallBack;

import retrofit2.Response;

/**
 * Created by NeuroAndroid on 2017/3/15.
 */

public class LoginPresenter extends BasePresenter<BaseResponse<User>> implements ILoginContract.Presenter {
    private ILoginModel mLoginModel;
    private ILoginContract.View mLoginView;

    public LoginPresenter(String baseUrl, ILoginContract.View loginView) {
        super(baseUrl);
        mLoginView = loginView;
        mLoginModel = new LoginModelImpl(baseUrl);
        mLoginView.setPresenter(this);
    }

    @Override
    public void login(String param, String password, int userType, String ip) {
        mCall = mLoginModel.login(param, password, userType, ip);
        mCall.enqueue(new RetrofitCallBack<BaseResponse<User>>() {
            @Override
            public void onSuccess(Response<BaseResponse<User>> response) {
                mLoginView.showLoginMsg(response.body());
            }

            @Override
            public void onFail(String msg) {
                mLoginView.showTip(msg);
            }
        });
    }

    @Override
    public void onDestroy() {
        if (mCall != null) {
            mCall.cancel();
        }
    }
}
