package com.nutrition.express.common;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;

import com.nutrition.express.R;
import com.nutrition.express.util.Utils;



/**
 * Created by huang on 4/15/17.
 */

public class ProgressCircle extends View {
    private Paint paint = new Paint();
    private int rx, ry;//圆心坐标
    private int r;//半径
    private int strokeWidth;
    private RectF rectF;
    private int unit;
    private int progress;

    public ProgressCircle(Context context) {
        super(context);
        initPaint();
    }

    public ProgressCircle(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initPaint();
    }

    public ProgressCircle(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint();
    }

    public ProgressCircle(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initPaint();
    }

    private void initPaint() {
        unit = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1f,
                getResources().getDisplayMetrics());
        strokeWidth = 2 * unit;
        paint.setColor(getResources().getColor(R.color.divider_color));
        paint.setStrokeWidth(strokeWidth);
        paint.setAntiAlias(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int dw = (int) Utils.dp2Pixels(getContext(), 56);
        int dh = (int) Utils.dp2Pixels(getContext(), 56);
        dw += (getPaddingLeft() + getPaddingRight());
        dh += (getPaddingTop() + getPaddingBottom());

        final int measuredWidth = resolveSizeAndState(dw, widthMeasureSpec, 0);
        final int measuredHeight = resolveSizeAndState(dh, heightMeasureSpec, 0);
        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        rx = w / 2;
        ry = h / 2;
        r = Math.min(w, h) / 2 - strokeWidth;
        int rr = r - strokeWidth - unit;
        rectF = new RectF(rx - rr, ry - rr, rx + rr, ry + rr);
        Log.d("onSizeChanged", rx + "-" + ry + "-" + r);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(rx, ry, r, paint);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawArc(rectF, -90, progress, true, paint);
    }

    public void setProgress(long max, long progress) {
        int newProgress = (int) (progress * 360L / max);
        if (Math.abs(newProgress - this.progress) > 6) {
            this.progress = newProgress;
            invalidate();
        }
    }

}
