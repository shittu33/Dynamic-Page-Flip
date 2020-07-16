package com.example.sample.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;

import androidx.annotation.ColorInt;
import androidx.appcompat.widget.AppCompatEditText;
import com.example.sample.R;

public class LinedEditText extends AppCompatEditText {
    private Rect mRect;
    private Paint mPaint;
    private int mLineColor;

    public LinedEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!show_line)
            return;

        mRect = new Rect();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // define the style of line
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        // define the color of line
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LinedEditText);
            mLineColor = a.getColor(R.styleable.LinedEditText_line_color
                    , context.getResources().getColor(R.color.note_light_purple));
            mPaint.setColor(mLineColor);
            a.recycle();
        } else {
            setLineColor(mLineColor);
        }
    }

    public void setLineColor(@ColorInt int color) {
        mLineColor = color;
        mPaint.setColor(mLineColor);
        invalidate();
    }

    boolean show_line = true;

    public void showLine(boolean show_line) {
        this.show_line = show_line;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!show_line) {
            super.onDraw(canvas);
            return;
        }
        int height = getHeight();
        int lHeight = getLineHeight();
        // the number of line
        int count = height / lHeight;
        if (getLineCount() > count) {
            // for long text with scrolling
            count = getLineCount();
        }
        Rect r = mRect;
        Paint paint = mPaint;

        // first line
        int baseline = getLineBounds(0, r);

        // draw the remaining lines.
        for (int i = 0; i < count; i++) {
            canvas.drawLine(r.left, baseline + 1, r.right, baseline + 1, paint);
            // next line
            baseline += getLineHeight();
        }
        super.onDraw(canvas);
    }
}
