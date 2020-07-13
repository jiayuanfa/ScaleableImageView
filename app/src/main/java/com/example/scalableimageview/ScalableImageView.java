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
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.OverScroller;

import androidx.annotation.Nullable;
import androidx.core.view.GestureDetectorCompat;

public class ScalableImageView extends View {

    /**
     * 把方法实现都提取出来
     */
    /**
     * 手势监听
     */
    GestureDetectorCompat gestureDetectorCompat;
    GestureDetector.OnGestureListener fageGestureListener = new FageGestureListerner();
    Runnable fageRunner = new FageRunner();

    /**
     * 双指放缩功能
     */
    ScaleGestureDetector scaleGestureDetector;
    ScaleGestureDetector.OnScaleGestureListener fageScaleGestureListener = new FageScaleGestureListener();

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

    public ScalableImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        bitmap = Utils.getAvatar(getResources(), (int) IMAGE_WIDTH);
        /**
         * 初始化手势 并设置双击回调
         * 双指缩放等
         */
        gestureDetectorCompat = new GestureDetectorCompat(context, fageGestureListener);
        overScroller = new OverScroller(context);
        scaleGestureDetector = new ScaleGestureDetector(context, fageScaleGestureListener);
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
        canvas.translate(offsetX * scaleFraction, offsetY * scaleFraction);
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
     * 处理放大或者滑动之后的边界问题
     * 也就是若方法或者滑动之后边界超出屏幕，则撑满屏幕即可
     */
    private void fixOffset() {
        /**
         * 左右滑动，不能超过图片边界
         */
        offsetX = Math.max(offsetX, -maxOffsetX);
        offsetX = Math.min(offsetX, maxOffsetX);
        /**
         * 上下滑动不能超过图片边界
         */
        offsetY = Math.max(offsetY, -maxOffsetY);
        offsetY = Math.min(offsetY, maxOffsetY);
    }

    /**
     * 手势内部实现类
     */
    private class FageGestureListerner extends GestureDetector.SimpleOnGestureListener {
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
                offsetY -= distanceY;
                fixOffset();
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
                postOnAnimation(fageRunner);
            }
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
                // 放大的时候，减去偏移量，以保证触摸放大的时候，触摸点不偏移
                offsetX = (e.getX() - getWidth() / 2f) * (1 - bigScale / smallScale);
                offsetY = (e.getY() - getHeight() / 2f) * (1 - bigScale / smallScale);
                fixOffset();
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

    /**
     * 动画Runnable
     */
    private class FageRunner implements Runnable {
        @Override
        public void run() {
            if (overScroller.computeScrollOffset()) {
                offsetX = overScroller.getCurrX();
                offsetY = overScroller.getCurrY();
                invalidate();
                postOnAnimation(this);
            }
        }
    }

    /**
     * 放缩功能类
     */
    private class FageScaleGestureListener implements ScaleGestureDetector.OnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            return false;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return false;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {

        }
    }
}
