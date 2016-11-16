package com.vlad.player.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;


public class ScaleImageView extends ImageView {

    private ScaleGestureDetector gestureDetector;
    private float mScaleFactor = 1.f;

    public ScaleImageView(Context context) {
        super(context);
        this.init();
    }

    public ScaleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init();
    }

    public ScaleImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.init();
    }

    private void init() {
        this.gestureDetector = new ScaleGestureDetector(this.getContext(), new ScaleListener());
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        this.gestureDetector.onTouchEvent(ev);
        return true;
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.save();
        canvas.scale(this.mScaleFactor, this.mScaleFactor, this.getWidth() / 2, this.getHeight() / 2);
        super.onDraw(canvas);
        canvas.restore();
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            ScaleImageView.this.mScaleFactor *= detector.getScaleFactor();
            ScaleImageView.this.mScaleFactor = Math.max(0.1f, Math.min(ScaleImageView.this.mScaleFactor, 1.0f));

            ScaleImageView.this.invalidate();
            return true;
        }
    }
}
