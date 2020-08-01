package com.example.scalableimageview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

public class MultiTouchView extends View {

    private static int IMAGE_WIDTH = (int)Utils.dpToPixel(200);
    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    Bitmap bitmap;
    private float offsetX;
    private float offsetY;
    private float downX;
    private float downY;
    private float originalX;
    private float originalY;

    // 追踪手指的ID
    int trackingPointerId;

    public MultiTouchView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        bitmap = Utils.getAvatar(getResources(), IMAGE_WIDTH);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            // 记录手指按下位置
            // 更新图片偏移量
            case MotionEvent.ACTION_DOWN:
                // 按下之后记录跟踪手指的ID，刚开始的时候index为0
                trackingPointerId = event.getPointerId(0);
                downX = event.getX(0);
                downY = event.getY(0);
                originalX = offsetX;
                originalY = offsetY;
                break;
            /**
             * 图形跟随手指移动的方法
             */
            case MotionEvent.ACTION_MOVE:
                // 获取正在移动手指的index
                int index = event.findPointerIndex(trackingPointerId);
                offsetX = event.getX(index) - downX + originalX;
                offsetY = event.getY(index) - downY + originalY;
                invalidate();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                // 获取新的手指按下之后，将要接管跟踪id
                int actionIndex = event.getActionIndex();
                trackingPointerId = event.getPointerId(actionIndex);
                // 更新新手指位置
                downX = event.getX(actionIndex);
                downY = event.getY(actionIndex);
                originalX = offsetX;
                originalY = offsetY;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                // 每次抬起一个手指之后，就要用新按下的手指，或者是已经存在的手指去接管位置
                // 每次用最后落下的手指接管，如果抬起的就是最后一个，那么就用倒数第二个手指接管
                actionIndex = event.getActionIndex();
                int pointId = event.getPointerId(actionIndex);
                // 抬起的手指是正在追踪的手指，那么就要替换到新的已经落在屏幕上的手指上面
                if (pointId == trackingPointerId) {
                    int newIndex;
                    if (actionIndex == event.getPointerCount() - 1) {
                        newIndex = event.getPointerCount() - 2;
                    } else {
                        newIndex = event.getPointerCount() - 1;
                    }
                    trackingPointerId = event.getPointerId(newIndex);
                    // 更新位置
                    downX = event.getX(newIndex);
                    downY = event.getY(newIndex);
                    originalX = offsetX;
                    originalY = offsetY;
                }

                break;
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(bitmap, offsetX, offsetY, paint);
    }
}
