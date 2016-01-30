package rikka.searchbyimage.ui.apdater;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import rikka.searchbyimage.BuildConfig;
import rikka.searchbyimage.R;
import rikka.searchbyimage.staticdata.CustomEngine;

/**
 * Created by Rikka on 2016/1/24.
 */
public class SearchEngineAdapter extends RecyclerView.Adapter<SearchEngineAdapter.ViewHolder> {
    public interface OnItemClickListener
    {
        void onItemClick(View view, int position, CustomEngine item);
        void onItemLongClick(View view , int position, CustomEngine item);
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

    @Override
    public SearchEngineAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.list_item_edit_sites, parent, false);

        return new ViewHolder(itemView);
    }

    private static final int VIEW_TYPE_ITEM = 1<<0;
    private static final int VIEW_TYPE_HEADER_BUILT_IN = 1<<1;
    private static final int VIEW_TYPE_HEADER_CUSTOM = 1<<2;
    private static final int VIEW_TYPE_EMPTY = 1<<3;

    private static final int BUILT_IN_ENGINES = (BuildConfig.hideOtherEngine ? 1 : 6);

    @Override
    public int getItemViewType(int position) {
        int flag = VIEW_TYPE_ITEM;
        if (position == 0) {
            flag |= VIEW_TYPE_HEADER_BUILT_IN;
        }
        if (position == BUILT_IN_ENGINES) {
            flag |= VIEW_TYPE_HEADER_CUSTOM;
        }

        if (position == BUILT_IN_ENGINES - 1 || position == getItemCount() -1) {
            flag |= VIEW_TYPE_EMPTY;
        }

        return flag;
    }

    @Override
    public void onBindViewHolder(final SearchEngineAdapter.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);

        if ((viewType & VIEW_TYPE_EMPTY) == 0) {
            holder.ViewEmpty.setVisibility(View.GONE);
        }

        if ((viewType & (VIEW_TYPE_HEADER_BUILT_IN | VIEW_TYPE_HEADER_CUSTOM)) == 0) {
            holder.vHead.setVisibility(View.GONE);
        } else {
            holder.vHead.setText(((viewType & VIEW_TYPE_HEADER_BUILT_IN) == 1) ?
                    holder.itemView.getContext().getString(R.string.built_in) : holder.itemView.getContext().getString(R.string.custom));
        }

        final CustomEngine item = mData.get(position);

        if (position == BUILT_IN_ENGINES - 1 || (position == mData.size() - 1)) {
            holder.vDivider.setVisibility(View.GONE);
        }

        holder.vSwitch.setChecked(item.enabled == 1);

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
            holder.View.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = holder.getLayoutPosition();
                    mOnItemClickListener.onItemClick(holder.itemView,
                            pos,
                            mData.get(pos));
                }
            });

            holder.View.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int pos = holder.getLayoutPosition();
                    mOnItemClickListener.onItemLongClick(holder.itemView,
                            pos,
                            mData.get(pos));
                    return false;
                }
            });
        }

        holder.vSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                item.enabled = isChecked ? 1 : 0;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        protected TextView vName;
        protected TextView vUrl;
        protected ImageView vIcon;
        protected TextView vHead;
        protected View vDivider;
        protected View View;
        protected View ViewEmpty;
        protected SwitchCompat vSwitch;

        public ViewHolder(View itemView) {
            super(itemView);

            vName = (TextView) itemView.findViewById(R.id.item_name);
            vUrl = (TextView) itemView.findViewById(R.id.item_url);
            vIcon = (ImageView) itemView.findViewById(R.id.item_icon);
            vHead = (TextView) itemView.findViewById(R.id.item_header);
            vDivider = itemView.findViewById(R.id.fake_divider);
            View = itemView.findViewById(R.id.view);
            ViewEmpty = itemView.findViewById(R.id.empty_view);
            vSwitch = (SwitchCompat) itemView.findViewById(R.id.switch_site_enabled);
        }
    }
}
