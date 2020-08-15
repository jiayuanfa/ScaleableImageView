package com.example.scalableimageview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

public class MultiTouchView4 extends View {

    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private SparseArray<Path> paths = new SparseArray<>();

    public MultiTouchView4(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Utils.dpToPixel(4));
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {

            // 单指或者多个指头落下 绘制某个路径 并创建相应的Path，装进数组里面
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                int actionIndex = event.getActionIndex();
                int pointerId = event.getPointerId(actionIndex);
                Path path = new Path();
                path.moveTo(event.getX(actionIndex), event.getY(actionIndex));
                paths.append(pointerId, path);
                invalidate();
                break;

                // 单指或者多个指头移动，遍历数组，进行绘制
            case MotionEvent.ACTION_MOVE:
                for (int i = 0; i < event.getPointerCount(); i++) {
                    pointerId = event.getPointerId(i);
                    path = paths.get(pointerId);
                    path.lineTo(event.getX(i), event.getY(i));
                }
                invalidate();
                break;

                // 单指抬起或者多指抬起 清除某个绘制路径
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                paths.remove(event.getPointerId(event.getActionIndex()));
                invalidate();
                break;
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 取值 绘制
        for (int i = 0; i < paths.size(); i++) {
            canvas.drawPath(paths.valueAt(i), paint);
        }
    }
}
