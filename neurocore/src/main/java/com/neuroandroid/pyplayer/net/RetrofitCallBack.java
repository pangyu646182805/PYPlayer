package com.neuroandroid.pyplayer.net;

import com.google.gson.Gson;
import com.neuroandroid.pyplayer.base.BaseResponse;
import com.neuroandroid.pyplayer.utils.L;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by NeuroAndroid on 2017/3/14.
 */

public abstract class RetrofitCallBack<T extends BaseResponse> implements Callback<T> {
    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        L.tag("main").e("response : " + new Gson().toJson(response.body()));
        if (!call.isCanceled()) {  // 如果retrofit请求没有被取消
            if (response.raw().code() == 200) {
                T body = response.body();
                if (body != null) {
                    onSuccess(response);
                } else {
                    onFail("response body is null");
                }
            } else {
                onFailure(call, new Exception("response error, detail = " + response.raw().toString()));
            }
        } else {
            onFail("retrofit request is be canceled");
        }
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        if (!call.isCanceled()) {
            onFail(t.getMessage());
        } else {
            onFail("retrofit request is be canceled");
        }
    }

    public abstract void onSuccess(Response<T> response);

    public abstract void onFail(String msg);
}
