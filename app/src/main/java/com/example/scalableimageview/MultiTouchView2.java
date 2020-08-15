package com.example.scalableimageview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

public class MultiTouchView2 extends View {

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

    public MultiTouchView2(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        bitmap = Utils.getAvatar(getResources(), IMAGE_WIDTH);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        // 求协同手指的中心点
        float sumX = 0;
        float sumY = 0;
        // 判断是不是某根手指抬起，如果是，需要减去手指数量，因为他们同属于一个触摸事件
        boolean isPointUp = event.getActionMasked() == MotionEvent.ACTION_POINTER_UP;
        for (int i = 0; i < event.getPointerCount(); i++) {
            if (!isPointUp || i != event.getActionIndex()) {
                sumX += event.getX(i);
                sumY += event.getY(i);
            }
        }
        // 获取手指数量
        int pointCount = event.getPointerCount();
        if (isPointUp) {
            pointCount--;
        }
        // 计算中心点坐标
        float focusX = sumX / pointCount;
        float focusY = sumY / pointCount;

        switch (event.getActionMasked()) {
            // 每次手指落下、多个手指落下、抬起其中一个的时候，更新手指落下位置 以及初始的位置
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_POINTER_UP:
                downX = focusX;
                downY = focusY;
                originalX = offsetX;
                originalY = offsetY;
                break;

                // 每次移动过后，计算出偏移量 然后重绘界面
            case MotionEvent.ACTION_MOVE:
                offsetX = focusX - downX + originalX;
                offsetY = focusY - downY + originalY;
                invalidate();
                break;
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(bitmap, offsetX, offsetY, paint);
    }
}
