package rikka.searchbyimage.ui.apdater;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import rikka.searchbyimage.R;
import rikka.searchbyimage.staticdata.CustomEngine;

/**
 * Created by Rikka on 2016/1/24.
 */
public class SearchEngineAdapter extends RecyclerView.Adapter<SearchEngineAdapter.ViewHolder> {
    public interface OnItemClickListener
    {
        void onItemClick(View view, int position, int realPosition, CustomEngine item);
        void onItemLongClick(View view , int position, int realPosition, CustomEngine item);
    }

    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener)
    {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    private List<CustomEngine> mData;

    public SearchEngineAdapter(List<CustomEngine> data) {
        mData = data;
    }

    private static final int RESOURCE_ID[] = {
            R.layout.list_item_edit_sites,
            R.layout.list_item_edit_sites,
            R.layout.list_item_edit_sites,
            R.layout.list_item_edit_sites_empty
    };

    @Override
    public SearchEngineAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(RESOURCE_ID[viewType], parent, false);

        return new ViewHolder(itemView);
    }

    private static final int VIEW_TYPE_ITEM = 0;
    private static final int VIEW_TYPE_HEADER_BUILT_IN = 1;
    private static final int VIEW_TYPE_HEADER_CUSTOM = 2;
    private static final int VIEW_TYPE_EMPTY = 3;

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return VIEW_TYPE_HEADER_BUILT_IN;
        } else if (position == 6 || position == getItemCount() - 1) {
            return VIEW_TYPE_EMPTY;
        } else if (position == 7) {
            return VIEW_TYPE_HEADER_CUSTOM;
        }
        return VIEW_TYPE_ITEM;
    }

    public int toRealPosition(int pos) {
        if (pos > 6) {
            pos -= 1;
        }
        return pos;
    }

    @Override
    public void onBindViewHolder(final SearchEngineAdapter.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);

        if (viewType == VIEW_TYPE_EMPTY) {
            return;
        }

        position = toRealPosition(position);

        if (viewType == VIEW_TYPE_ITEM) {
            holder.vHead.setVisibility(View.GONE);
        }

        holder.vHead.setText(viewType == VIEW_TYPE_HEADER_BUILT_IN ? "Built-in" : "Custom");

        CustomEngine item = mData.get(position);

        if (position == 5 || (position == mData.size() - 1)) {
            holder.vDivider.setVisibility(View.GONE);
        }

        int start = item.upload_url.indexOf("//") + 2;
        start = item.upload_url.indexOf("/", start);
        String url = start != -1 ? item.upload_url.substring(0, start) : item.upload_url;

        holder.vName.setText(item.name);
        holder.vUrl.setText(url);

        Glide.with(holder.vIcon.getContext())
                .load(url + "/favicon.ico")
                .crossFade()
                .into(holder.vIcon);

        if (mOnItemClickListener != null)
        {
            holder.View.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    int pos = toRealPosition(holder.getLayoutPosition());
                    mOnItemClickListener.onItemClick(holder.itemView,
                            holder.getLayoutPosition(),
                            pos,
                            mData.get(pos));
                }
            });

            holder.View.setOnLongClickListener(new View.OnLongClickListener()
            {
                @Override
                public boolean onLongClick(View v)
                {
                    int pos = toRealPosition(holder.getLayoutPosition());
                    mOnItemClickListener.onItemLongClick(holder.itemView,
                            holder.getLayoutPosition(),
                            pos,
                            mData.get(pos));
                    return false;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return (mData.size() > 6 ? mData.size() + 2 : mData.size() + 1);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        protected TextView vName;
        protected TextView vUrl;
        protected ImageView vIcon;
        protected TextView vHead;
        protected View vDivider;
        protected View View;

        public ViewHolder(View itemView) {
            super(itemView);

            vName = (TextView) itemView.findViewById(R.id.item_name);
            vUrl = (TextView) itemView.findViewById(R.id.item_url);
            vIcon = (ImageView) itemView.findViewById(R.id.item_icon);
            vHead = (TextView) itemView.findViewById(R.id.item_header);
            vDivider = itemView.findViewById(R.id.fake_divider);
            View = itemView.findViewById(R.id.view);
        }
    }
}
