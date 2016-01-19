package rikka.searchbyimage.view;

import android.content.Context;
import android.util.TypedValue;
import android.widget.ScrollView;
import android.widget.TextView;

import rikka.searchbyimage.R;
import rikka.searchbyimage.utils.Utils;

/**
 * Created by Rikka on 2016/1/7.
 */
public class ContextMenuTitleView extends ScrollView {
    private static final int MAX_HEIGHT_DP = 70;
    private static final int PADDING_DP = 16;

    public ContextMenuTitleView(Context context, String title) {
        super(context);

        int padding = Utils.dpToPx(PADDING_DP);
        setPadding(padding, padding, padding, 0);

        TextView titleView = new TextView(context);
        titleView.setText(title);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        titleView.setTextColor(context.getResources().getColor(R.color.contextMenuTitle));
        addView(titleView);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(Utils.dpToPx(MAX_HEIGHT_DP), MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
