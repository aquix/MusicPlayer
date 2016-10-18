package com.fisko.music.ui.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.fisko.music.R;

public class GridItemView extends ImageView {

    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;


    private int mMode = NONE;

    private Matrix mMatrix = new Matrix();
    private Matrix mSavedMatrix = new Matrix();

    private PointF mStartPoint = new PointF();
    private PointF mMidPoint = new PointF();
    private float mPrevDist = 1f;

    public GridItemView(Context context) {
        super(context);
    }

    public GridItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GridItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int footerHeight = (int) getResources().getDimension(R.dimen.grid_item_footer_height);
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int zoomMinDist = (int) getResources().getDimension(R.dimen.min_zoom_size);
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mSavedMatrix.set(mMatrix);
                mStartPoint.set(event.getX(), event.getY());
                mMode = DRAG;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                mPrevDist = spacing(event);
                if (mPrevDist > zoomMinDist) {
                    mSavedMatrix.set(mMatrix);
                    midPoint(mMidPoint, event);
                    mMode = ZOOM;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mMode = NONE;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mMode == DRAG) {
                    mMatrix.set(mSavedMatrix);
                    mMatrix.postTranslate(event.getX() - mStartPoint.x, event.getY() - mStartPoint.y);
                } else if (mMode == ZOOM) {
                    float newDist = spacing(event);
                    if (newDist > zoomMinDist) {
                        mMatrix.set(mSavedMatrix);
                        float scale = newDist / mPrevDist;
                        mMatrix.postScale(scale, scale, mMidPoint.x, mMidPoint.y);
                    }
                }
                break;
        }

        setImageMatrix(mMatrix);
        return true;
    }

    /** Determine the space between the first two fingers */
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /** Calculate the mMidPoint point of the first two fingers */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

}