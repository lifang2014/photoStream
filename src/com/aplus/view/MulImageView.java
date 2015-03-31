package com.aplus.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.ScrollView;

/**
 * Created by lifang on 2015/3/29.
 */
public class MulImageView extends ScrollView{

    private LinearLayout parentLineLayout;

    public MulImageView(Context context) {
        this(context, null);
    }

    public MulImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MulImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private boolean once;
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if(changed && !once){
            parentLineLayout = new LinearLayout(getContext());
            parentLineLayout.setOrientation(LinearLayout.HORIZONTAL);
            parentLineLayout.setBackgroundColor(Color.parseColor("#228B22"));
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            layoutParams.leftMargin = 3;
            layoutParams.rightMargin = 3;
            parentLineLayout.setLayoutParams(layoutParams);
            addView(parentLineLayout);
            once = true;
        }
    }
}
