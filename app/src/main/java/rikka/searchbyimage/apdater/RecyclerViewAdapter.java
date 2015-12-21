package rikka.searchbyimage.apdater;

import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import rikka.searchbyimage.R;
import rikka.searchbyimage.utils.IqdbResultCollecter;

/**
 * Created by Rikka on 2015/12/20.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<IqdbResultCollecter.IqdbItem> list;
    private int count = 0;

    public interface OnItemClickListener
    {
        void onItemClick(View view, int position, IqdbResultCollecter.IqdbItem item);
        void onItemLongClick(View view , int position, IqdbResultCollecter.IqdbItem item);
    }

    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener)
    {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    public RecyclerViewAdapter(ArrayList<IqdbResultCollecter.IqdbItem> list) {
        this.list = list;
        this.count = list.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        final ViewHolder mViewHolder = (ViewHolder)holder;
        mViewHolder.mTextViewURL.setText(list.get(position).imageURL);
        mViewHolder.mTextViewSize.setText(list.get(position).size);
        mViewHolder.mTextViewSimilarity.setText(list.get(position).similarity);

        Glide.with(mViewHolder.mImageView.getContext())
                .load(list.get(position).thumbnailURL)
                .centerCrop()
                .placeholder(R.mipmap.ic_launcher)
                .crossFade()
                .into(mViewHolder.mImageView);

        if (mOnItemClickListener != null)
        {
            holder.itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    int pos = holder.getLayoutPosition();
                    mOnItemClickListener.onItemClick(holder.itemView, pos, list.get(pos));
                }
            });

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener()
            {
                @Override
                public boolean onLongClick(View v)
                {
                    int pos = holder.getLayoutPosition();
                    mOnItemClickListener.onItemLongClick(holder.itemView, pos, list.get(pos));
                    return false;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return count;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public RelativeLayout mView;
        public TextView mTextViewURL;
        public TextView mTextViewSize;
        public TextView mTextViewSimilarity;
        public ImageView mImageView;

        public ViewHolder(View itemView) {
            super(itemView);

            mView = (RelativeLayout) itemView.findViewById(R.id.view);
            mTextViewURL = (TextView) itemView.findViewById(R.id.item_text_url);
            mTextViewSize = (TextView) itemView.findViewById(R.id.item_text_size);
            mTextViewSimilarity = (TextView) itemView.findViewById(R.id.item_text_similarity);
            mImageView = (ImageView) itemView.findViewById(R.id.item_image);
        }
    }
}