package rikka.searchbyimage.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import rikka.searchbyimage.R;

/**
 * Created by Rikka on 2016/1/18.
 */
public class InfoBarLayout extends FrameLayout {
    public interface OnLayoutChangeListener {
        void onLayoutChange(View view, int left, int top, int right, int bottom);
    }

    private OnLayoutChangeListener mOnLayoutChangeListener;

    private Drawable mLineDrawable;

    public InfoBarLayout(Context context) {
        super(context);
    }

    public InfoBarLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        mLineDrawable = new ColorDrawable(ContextCompat.getColor(context, R.color.divider));
    }

    public void setOnLayoutChangeListener(OnLayoutChangeListener onLayoutChangeListener) {
        mOnLayoutChangeListener = onLayoutChangeListener;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed && mOnLayoutChangeListener != null) {
            mOnLayoutChangeListener.onLayoutChange(this, l, t, r, b);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            mLineDrawable.setBounds(0, 0, canvas.getWidth(), 2);
            mLineDrawable.draw(canvas);
        }
    }
}