package rikka.searchbyimage.widget;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import rikka.searchbyimage.utils.Utils;

/**
 * Created by Rikka on 2016/1/27.
 */
public class MyLinearLayoutManager extends LinearLayoutManager {
    private final int DEFAULT_CHILD_HEIGHT = Utils.dpToPx(48);
    private int mFakeItemCount = 0;

    public MyLinearLayoutManager(Context context) {
        super(context);
    }

    public void setFakeItemCount(int fakeItemCount) {
        mFakeItemCount = fakeItemCount;
    }

    @Override
    public boolean canScrollVertically() {
        return false;
    }

    @Override
    public void onMeasure(RecyclerView.Recycler recycler, RecyclerView.State state,
                          int widthSpec, int heightSpec) {
        final int width = View.MeasureSpec.getSize(widthSpec);
        int height = DEFAULT_CHILD_HEIGHT * mFakeItemCount;
        int childHeight = DEFAULT_CHILD_HEIGHT;
        for (int i = 0; i < getItemCount(); i++) {
            try {
                childHeight = measureScrapChildHeight(recycler, i,
                        View.MeasureSpec.makeMeasureSpec(i, View.MeasureSpec.UNSPECIFIED),
                        View.MeasureSpec.makeMeasureSpec(i, View.MeasureSpec.UNSPECIFIED));
                height = height + childHeight;

            } catch (IndexOutOfBoundsException ignore) {
                height = height + childHeight;
            }
        }
        setMeasuredDimension(width, height);
    }

    private int measureScrapChildHeight(RecyclerView.Recycler recycler, int position, int widthSpec,
                                        int heightSpec) throws IndexOutOfBoundsException {
        View view = recycler.getViewForPosition(position);
        int height = 0;
        if (view != null) {

            RecyclerView.LayoutParams p = (RecyclerView.LayoutParams) view.getLayoutParams();
            int childWidthSpec = ViewGroup.getChildMeasureSpec(widthSpec,
                    view.getPaddingLeft() + view.getPaddingRight(), p.width);
            int childHeightSpec = ViewGroup.getChildMeasureSpec(heightSpec,
                    view.getPaddingTop() + view.getPaddingBottom(), p.height);
            view.measure(childWidthSpec, childHeightSpec);
            height = view.getMeasuredHeight() + p.bottomMargin + p.topMargin;
            recycler.recycleView(view);
        }
        return height;
    }
}
