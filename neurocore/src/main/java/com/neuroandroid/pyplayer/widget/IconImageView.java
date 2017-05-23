package com.neuroandroid.pyplayer.widget;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.neuroandroid.pyplayer.R;
import com.neuroandroid.pyplayer.utils.UIUtils;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class IconImageView extends AppCompatImageView {
    public IconImageView(Context context) {
        super(context);
        init(context);
    }

    public IconImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public IconImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        if (context == null) return;
        setColorFilter(UIUtils.getColor(R.color.colorGray333), PorterDuff.Mode.SRC_IN);
    }
}
