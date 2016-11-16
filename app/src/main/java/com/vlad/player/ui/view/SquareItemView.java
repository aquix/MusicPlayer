package com.vlad.player.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class SquareItemView extends ImageView {

    public SquareItemView(Context context) {
        super(context);
    }

    public SquareItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
//        Log.d("smt", "" + width + " " + height);
        if(width != 0) {
            super.onMeasure(widthMeasureSpec, widthMeasureSpec);
        } else {
            super.onMeasure(heightMeasureSpec, heightMeasureSpec);
        }
    }
}