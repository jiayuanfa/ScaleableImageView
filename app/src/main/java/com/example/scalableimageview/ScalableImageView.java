package com.example.scalableimageview;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.view.GestureDetectorCompat;

public class ScalableImageView extends View implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {
    private static final float IMAGE_WIDTH = Utils.dpToPixel(300);
    /**
     * 放缩系数
     */
    private static final float OVER_SCALE_FACTOR = 1.5f;

    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    Bitmap bitmap;

    private float offsetX;
    private float offsetY;
    float smallScale;
    float bigScale;

    /**
     * 判断是放大还是缩小
     */
    boolean big = false;

    public float getScaleFraction() {
        return scaleFraction;
    }

    public void setScaleFraction(float scaleFraction) {
        this.scaleFraction = scaleFraction;
        invalidate();
    }

    /**
     * 动画 以及动画完成度
     */
    float scaleFraction;
    ObjectAnimator scaleObjectAnimator;

    /**
     * 手势监听
     */
    GestureDetectorCompat gestureDetectorCompat;

    public ScalableImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        bitmap = Utils.getAvatar(getResources(), (int) IMAGE_WIDTH);
        /**
         * 初始化手势 并设置双击回调
         */
        gestureDetectorCompat = new GestureDetectorCompat(context, this);
        gestureDetectorCompat.setOnDoubleTapListener(this);
    }

    private ObjectAnimator getScaleAnimator() {
        if (scaleObjectAnimator == null) {
            scaleObjectAnimator = ObjectAnimator.ofFloat(this, "scaleFraction", 0, 1);
        }
        return scaleObjectAnimator;
    }

    /**
     * 在每次Size发生改变的时候，重新计算Bitmap的位置
     * @param w
     * @param h
     * @param oldw
     * @param oldh
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        offsetX = (getWidth() - bitmap.getWidth()) / 2f;
        offsetY = (getHeight() - bitmap.getHeight()) / 2f;

        /**
         * 计算最大最小的Scale
         * 如果是宽大于高的图片，那么最小的Scale就是View宽/Bitmap宽
         * 如果是宽小于高的图片，那么最小的Scale就是View高/Bitmap高
         * */
        if ((float) bitmap.getWidth() / bitmap.getHeight() > (float) getWidth() / getHeight()) {
            smallScale = (float) getWidth() / bitmap.getWidth();
            bigScale = (float) getHeight() / bitmap.getHeight() * OVER_SCALE_FACTOR;
        } else {
            smallScale = (float) getHeight() / bitmap.getHeight();
            bigScale = (float) getWidth() / bitmap.getWidth() * OVER_SCALE_FACTOR;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 放缩，使之撑满屏幕
        /**
         * Scale变化过程，使用动画完成度进行计算，使之呈现绘制动画的顺滑变大的动画效果
         */
        float scale = smallScale + (bigScale - smallScale) * scaleFraction;
        canvas.scale(scale, scale, getWidth() / 2f, getHeight() / 2f);
        canvas.drawBitmap(bitmap, offsetX, offsetY, paint);
    }

    /**
     * 只有在这里设置为自定义手势的拦截，才能生效双击方法
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetectorCompat.onTouchEvent(event);
    }

    /**
     * Return true 使之响应点击回调
     * @param e
     * @return
     */
    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        big = !big;
        if (big) {
            getScaleAnimator().start();
        } else {
            getScaleAnimator().reverse();
        }
        Log.d("gesture", "double click");
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }
}
