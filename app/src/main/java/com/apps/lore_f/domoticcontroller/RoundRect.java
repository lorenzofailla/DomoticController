package com.apps.lore_f.domoticcontroller;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by lore_f on 10/08/2018.
 */

public class RoundRect extends View {

    private static final int DEFAULT_THICKNESS = 3;
    private static final int DEFAULT_PADDING = 10;
    private static final int DEFAULT_RADIUS = 10;
    private static final int DEFAULT_FORECOLOR = Color.BLACK;

    private int thickness;
    private int fore;
    private Color back;
    private int backAlpha;
    private int radius;
    private int padding;

    private Paint paint = new Paint();

    public RoundRect(Context context, AttributeSet attrs) {
        super(context, attrs);

        // recupera gli attributi
        TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.RoundRect, 0, 0);

        thickness = attributes.getDimensionPixelSize(R.styleable.RoundRect_RR_line_thickness, DEFAULT_THICKNESS);
        padding = attributes.getDimensionPixelSize(R.styleable.RoundRect_RR_padding, DEFAULT_PADDING);
        radius = attributes.getDimensionPixelSize(R.styleable.RoundRect_RR_radius, DEFAULT_RADIUS);
        fore = attributes.getColor(R.styleable.RoundRect_RR_forecolor, DEFAULT_FORECOLOR);

        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(thickness);
        paint.setColor(fore);

    }

    @Override
    public void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        canvas.drawRoundRect(new RectF(padding, padding, width - padding, height - padding), radius, radius, paint);

    }

    public int getNetWidth() {

        return getMeasuredWidth() - 2 * padding - 2 * radius;

    }

    public int getNetHeight() {

        return getMeasuredHeight() - 2 * padding - 2 * radius;

    }

}
