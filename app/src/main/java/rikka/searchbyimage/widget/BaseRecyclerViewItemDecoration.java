package rikka.searchbyimage.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import rikka.searchbyimage.R;

/**
 * Created by Rikka on 2015/12/20.
 */
public class BaseRecyclerViewItemDecoration extends RecyclerView.ItemDecoration {
    private Drawable mDivider;

    public BaseRecyclerViewItemDecoration(Context context) {
        mDivider = ContextCompat.getDrawable(context, R.drawable.line_divider);
    }

    public BaseRecyclerViewItemDecoration(Context context, int resId) {
        mDivider = ContextCompat.getDrawable(context, resId);
    }

    public BaseRecyclerViewItemDecoration(Drawable drawable) {
        mDivider = drawable;
    }

    public Drawable getDrawable() {
        return mDivider;
    }

    public int getHeight() {
        return mDivider.getIntrinsicHeight();
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        int position = parent.getChildLayoutPosition(view);
        if (position < 1/* || !canDraw(parent, view, parent.getChildCount(), position)*/) {
            return;
        }

        outRect.top = getHeight();
    }

    public boolean canDraw(RecyclerView parent, View child, int childCount, int position) {
        return true;
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);

            if (!canDraw(parent, child, childCount, i)) {
                continue;
            }

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            int top = child.getBottom() + params.bottomMargin;
            int bottom = top + getHeight();

            //mDivider.draw(left, top, right, bottom);
            draw(c, left, top, right, bottom);
        }
    }

    public void draw(Canvas c, int left, int top, int right, int bottom) {
        mDivider.setBounds(left, top, right, bottom);
        mDivider.draw(c);
    }
}