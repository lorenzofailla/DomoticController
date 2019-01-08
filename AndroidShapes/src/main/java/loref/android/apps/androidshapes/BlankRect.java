package loref.android.apps.androidshapes;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * Created by lore_f on 10/08/2018.
 */

public class BlankRect extends View {

    private int fore;

    private Paint paint = new Paint();

    public BlankRect(Context context, AttributeSet attrs) {

        super(context, attrs);

        // recupera gli attributi
        TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.RoundRect, 0, 0);

        TypedValue typedValue = new TypedValue();
        if (getContext().getTheme().resolveAttribute(android.R.attr.windowBackground, typedValue, true)) {

            fore = typedValue.data;

        } else {

            fore = Color.BLACK;

        }

        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setColor(fore);

    }

    @Override
    public void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        canvas.drawRect(new RectF(0, 0, width , height ), paint);

    }

}
