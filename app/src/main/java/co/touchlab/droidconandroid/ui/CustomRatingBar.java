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
    private final static int MAX_RATING = 5;
    private float w24;
    private float w16;
    private float w40;
    private Paint fillPaint;
    private Paint emptyPaint;

    //~=~=~=~=~=~=~=~=~=~=~=~=Fields
    private int rating = 0;

    private RatingChangeListener mChangeListener = null;

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

        w24 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, res.getDisplayMetrics());
        w16 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, res.getDisplayMetrics());
        w40 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, res.getDisplayMetrics());
    }

    public CustomRatingBar(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        //get width and divide by 5
        int height = canvas.getHeight();

        for(int x = 0; x < 5; x++)
        {
            Paint paint = (rating <= x)
                    ? emptyPaint
                    : fillPaint;
            canvas.drawCircle(x * w40 + w24 / 2, height / 2, w24 / 2, paint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int h = (int) Math.ceil(w24);
        int w = (int) Math.ceil((w40 * 5) - w16);

        setMeasuredDimension(w, h);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        int division = getWidth() / MAX_RATING;
        float x = event.getX();
        float v = x / division;
        setRating(v);
        if(mChangeListener != null)
        {
            mChangeListener.onChange(rating);
        }
        return true;
    }

    public void setRating(float v)
    {
        int ceil = (int) Math.ceil(v);
        setRating(ceil);
    }

    public int getRating()
    {

        return rating;
    }

    public void setRating(int rate)
    {
        if(rate < 0)
        {
            rating = 0;
        }
        else if(rate > MAX_RATING)
        {
            rating = MAX_RATING;
        }
        else
        {
            rating = rate;
        }
        invalidate();
    }

    public void setChangeListener(RatingChangeListener changeListener)
    {
        mChangeListener = changeListener;
    }

    //~=~=~=~=~=~=~=~=~=~=~=~=Interface
    public interface RatingChangeListener
    {
        void onChange(int rate);
    }

}
