package rikka.searchbyimage.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Rikka on 2017/1/22.
 */

public class ListBottomSheetItemViewHolder extends RecyclerView.ViewHolder {

    public TextView text;

    public ListBottomSheetItemViewHolder(View itemView) {
        super(itemView);

        text = (TextView) itemView.findViewById(android.R.id.text1);
    }
}
