package com.vlad.player.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;


public class ScaleImageView extends ImageView {
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;
    private float scaleFactor = 1.f;

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
        this.scaleGestureDetector = new ScaleGestureDetector(this.getContext(), new ScaleGestureListener());
        this.gestureDetector = new GestureDetector(this.getContext(), new GestureListener());
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        this.scaleGestureDetector.onTouchEvent(ev);
        this.gestureDetector.onTouchEvent(ev);
        return true;
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.save();
        canvas.scale(this.scaleFactor, this.scaleFactor, this.getWidth() / 2, this.getHeight() / 2);
        super.onDraw(canvas);
        canvas.restore();
    }

    private class ScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            ScaleImageView.this.scaleFactor *= detector.getScaleFactor();
            ScaleImageView.this.scaleFactor = Math.max(0.1f, Math.min(ScaleImageView.this.scaleFactor, 2.0f));

            ScaleImageView.this.invalidate();
            return true;
        }
    }

    private class GestureListener extends  GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            ScaleImageView.this.scaleFactor = 1;
            ScaleImageView.this.invalidate();
            return true;
        }
    }
}
