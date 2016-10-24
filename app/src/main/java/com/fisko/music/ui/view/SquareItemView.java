package com.fisko.music.ui.view;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.fisko.music.R;

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
        int footerHeight = 0;
//        int footerHeight = (int) getResources().getDimension(R.dimen.grid_item_footer_height);
//        int height = widthMeasureSpec + footerHeight;
//        super.onMeasure(widthMeasureSpec, height);

        if(widthMeasureSpec != 0) {
            int height = widthMeasureSpec + footerHeight;
            super.onMeasure(widthMeasureSpec, height);
        } else {
            int width = heightMeasureSpec - footerHeight;
            super.onMeasure(width, heightMeasureSpec);
        }
    }
}