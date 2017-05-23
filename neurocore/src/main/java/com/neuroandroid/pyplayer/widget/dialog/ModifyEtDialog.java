package com.neuroandroid.pyplayer.widget.dialog;

import android.content.Context;
import android.support.annotation.NonNull;

import com.neuroandroid.pyplayer.R;
import com.neuroandroid.pyplayer.widget.ClearEditText;
import com.neuroandroid.pyplayer.widget.dialog.base.BaseDialog;

import butterknife.BindView;

/**
 * Created by NeuroAndroid on 2017/3/8.
 */

public class ModifyEtDialog extends BaseDialog<ModifyEtDialog> {
    @BindView(R.id.et_content)
    ClearEditText mEtContent;

    public ClearEditText getEtContent() {
        return mEtContent;
    }

    public void setInputType(int type) {
        mEtContent.setInputType(type);
    }

    public ModifyEtDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected int attachLayoutRes() {
        return R.layout.layout_modify_et_dialog;
    }

    @Override
    protected void initView() {

    }
}
