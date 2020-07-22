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
                downX = event.getX();
                downY = event.getY();
                originalX = offsetX;
                originalY = offsetY;
                break;
            /**
             * 图形跟随手指移动的方法
             */
            case MotionEvent.ACTION_MOVE:
                offsetX = event.getX() - downX + originalX;
                offsetY = event.getY() - downY + originalY;
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
