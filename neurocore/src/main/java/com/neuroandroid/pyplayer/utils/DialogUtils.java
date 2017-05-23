package com.neuroandroid.pyplayer.utils;

import com.neuroandroid.pyplayer.bean.NormalListBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by NeuroAndroid on 2017/3/10.
 */

public class DialogUtils {
    public static List<NormalListBean> provideGenderDataList() {
        List<NormalListBean> dataList = new ArrayList<>();
        dataList.add(provideNormalListBean("男", true));
        dataList.add(provideNormalListBean("女", false));
        return dataList;
    }

    public static NormalListBean provideNormalListBean(String text, boolean selected) {
        NormalListBean normalListBean = new NormalListBean();
        normalListBean.setText(text);
        normalListBean.setSelected(selected);
        return normalListBean;
    }
}
