package com.neuroandroid.pyplayer.mvp.model.impl;


import com.neuroandroid.pyplayer.base.BaseModel;
import com.neuroandroid.pyplayer.base.BaseResponse;
import com.neuroandroid.pyplayer.model.response.User;
import com.neuroandroid.pyplayer.mvp.model.ILoginModel;

import retrofit2.Call;

/**
 * Created by NeuroAndroid on 2017/3/15.
 */

public class LoginModelImpl extends BaseModel implements ILoginModel {
    public LoginModelImpl(String baseUrl) {
        super(baseUrl);
    }

    @Override
    public Call<BaseResponse<User>> login(String param, String password, int userType, String ip) {
        return mService.login(param, password, userType, ip);
    }
}
