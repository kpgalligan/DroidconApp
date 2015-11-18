package co.touchlab.droidconandroid.ui;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import co.touchlab.droidconandroid.R;

/**
 * Created by toidiu on 11/18/15.
 */
public class CustomRatingBar extends View
{

    private Paint fillPaint;
    private Paint emptyPaint;

    public int rating = 0;
    public int maxRating = 5;

    public CustomRatingBar(Context context)
    {
        super(context);
    }

    public CustomRatingBar(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        Resources res = getResources();
        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(res.getColor(R.color.vote_rating_blue));

        emptyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        emptyPaint.setStyle(Paint.Style.FILL);
        emptyPaint.setColor(res.getColor(R.color.vote_title_gray));
    }

    public CustomRatingBar(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void onDraw(Canvas canvas)
    {
        Resources res = getResources();
        float w24 = TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, res.getDisplayMetrics());
        float w16 = TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, res.getDisplayMetrics());
        float w40 = TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, res.getDisplayMetrics());

        super.onDraw(canvas);
        //get width and divide by 5
        int height = canvas.getHeight();
        int width = canvas.getWidth();
        int i = width / 5;
        int ihalf = i / 2;

        for(int x = 0; x < 5; x++)
        {
            Paint paint = (rating <= x)
                    ? fillPaint
                    : emptyPaint;
            canvas.drawCircle(x * w40 + w16, height / 2, w16 - 10, paint);
        }

    }


    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        return super.onTouchEvent(event);
    }
}
