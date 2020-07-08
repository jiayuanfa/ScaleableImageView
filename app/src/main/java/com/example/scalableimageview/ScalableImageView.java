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
import android.widget.OverScroller;

import androidx.annotation.Nullable;
import androidx.core.view.GestureDetectorCompat;

public class ScalableImageView extends View implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener, Runnable {
    private static final float IMAGE_WIDTH = Utils.dpToPixel(300);
    /**
     * 放缩系数
     */
    private static final float OVER_SCALE_FACTOR = 1.5f;

    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    Bitmap bitmap;

    float originOffsetX;
    float originOffsetY;
    float offsetX;
    float offsetY;
    float smallScale;
    float bigScale;

    // 滑动最大边界
    float maxOffsetX;
    float maxOffsetY;

    // 惯性滑动
    OverScroller overScroller;

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

        overScroller = new OverScroller(context);
    }

    /**
     * 动画
     * @return
     */
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
        originOffsetX = (getWidth() - bitmap.getWidth()) / 2f;
        originOffsetY = (getHeight() - bitmap.getHeight()) / 2f;

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

        maxOffsetX = (bitmap.getWidth() * bigScale - getWidth()) / 2;
        maxOffsetY = (bitmap.getHeight() * bigScale - getHeight()) / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 放缩，使之撑满屏幕
        /**
         * Scale变化过程，使用动画完成度进行计算，使之呈现绘制动画的顺滑变大的动画效果
         */
        canvas.translate(offsetX, offsetY);
        float scale = smallScale + (bigScale - smallScale) * scaleFraction;
        canvas.scale(scale, scale, getWidth() / 2f, getHeight() / 2f);
        canvas.drawBitmap(bitmap, originOffsetX, originOffsetY, paint);
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

    /**
     * 手指跟随滚动
     * @param e1
     * @param e2
     * @param distanceX
     * @param distanceY
     * @return
     */
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (big) {
            offsetX -= distanceX;
            /**
             * 左右滑动，不能超过图片边界
             */
            offsetX = Math.max(offsetX, -maxOffsetX);
            offsetX = Math.min(offsetX, maxOffsetX);
            offsetY -= distanceY;
            /**
             * 上下滑动不能超过图片边界
             */
            offsetY = Math.max(offsetY, -maxOffsetY);
            offsetY = Math.min(offsetY, maxOffsetY);
            invalidate();
        }
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    /**
     * 惯性滑动代码
     * @param e1
     * @param e2
     * @param velocityX
     * @param velocityY
     * @return
     */
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (big) {
            /**
             * 最后两个参数是回弹效果
             */
            overScroller.fling((int)offsetX, (int)offsetY, (int)velocityX, (int)velocityY,
                    -(int)maxOffsetX, (int)maxOffsetX, -(int)maxOffsetY, (int)maxOffsetY,
                    (int)Utils.dpToPixel(50), (int)Utils.dpToPixel(50));
            postOnAnimation(this);
        }
        return false;
    }

    @Override
    public void run() {
        if (overScroller.computeScrollOffset()) {
            offsetX = overScroller.getCurrX();
            offsetY = overScroller.getCurrY();
            invalidate();
            postOnAnimation(this);
        }
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
