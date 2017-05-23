package com.neuroandroid.pyplayer.mvp.model;


import com.neuroandroid.pyplayer.base.BaseResponse;
import com.neuroandroid.pyplayer.model.response.User;

import retrofit2.Call;

/**
 * Created by NeuroAndroid on 2017/3/15.
 */

public interface ILoginModel {
    Call<BaseResponse<User>> login(String param, String password, int userType, String Ip);
}
