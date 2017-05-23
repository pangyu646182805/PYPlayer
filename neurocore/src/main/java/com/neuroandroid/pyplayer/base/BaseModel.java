package com.neuroandroid.pyplayer.base;

import com.neuroandroid.pyplayer.model.api.ApiService;
import com.neuroandroid.pyplayer.net.RetrofitUtils;

/**
 * Created by NeuroAndroid on 2017/3/15.
 */

public class BaseModel {
    protected ApiService mService;

    public BaseModel(String baseUrl) {
        mService = RetrofitUtils.getInstance(baseUrl, false, true).create(ApiService.class);
    }
}
