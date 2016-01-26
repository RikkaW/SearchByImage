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
            R.layout.list_item_edit_sites_header,
            R.layout.list_item_edit_sites_header,
            R.layout.list_item_edit_sites_empty
    };

    @Override
    public SearchEngineAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(RESOURCE_ID[viewType], parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return 1;
        } else if (position == 6 + 1 && mData.size() > 6) {
            return 2;
        } else if (position == getItemCount() - 1) {
            return 3;
        }
        return 0;
    }

    @Override
    public void onBindViewHolder(final SearchEngineAdapter.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        if (viewType == 0) {
            position = holder.getRealPosition();
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
                holder.itemView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        int pos = holder.getRealPosition();
                        mOnItemClickListener.onItemClick(holder.itemView,
                                holder.getLayoutPosition(),
                                pos,
                                mData.get(pos));
                    }
                });

                holder.itemView.setOnLongClickListener(new View.OnLongClickListener()
                {
                    @Override
                    public boolean onLongClick(View v)
                    {
                        int pos = holder.getRealPosition();
                        mOnItemClickListener.onItemLongClick(holder.itemView,
                                holder.getLayoutPosition(),
                                pos,
                                mData.get(pos));
                        return false;
                    }
                });
            }
        } else if (viewType != 3) {
            holder.vHead.setText(viewType == 1 ? "Build-in" : "Custom");
        }
    }

    @Override
    public int getItemCount() {
        return (mData.size() > 6 ? mData.size() + 2 : mData.size() + 1) + 1;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        protected TextView vName;
        protected TextView vUrl;
        protected ImageView vIcon;
        protected TextView vHead;
        protected View vDivider;

        public int getRealPosition() {
            int pos = getLayoutPosition();
            pos --;
            if (pos > 6) {
                pos --;
            }
            return pos;
        }

        public ViewHolder(View itemView) {
            super(itemView);

            vName = (TextView) itemView.findViewById(R.id.item_name);
            vUrl = (TextView) itemView.findViewById(R.id.item_url);
            vIcon = (ImageView) itemView.findViewById(R.id.item_icon);
            vHead = (TextView) itemView.findViewById(R.id.item_header);
            vDivider = itemView.findViewById(R.id.fake_divider);
        }
    }
}
