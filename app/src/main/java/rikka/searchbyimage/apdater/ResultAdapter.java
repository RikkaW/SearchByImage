package rikka.searchbyimage.apdater;

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
public class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.ViewHolder> {

    private ArrayList<IqdbResultCollecter.IqdbItem> mData;
    private int mCount = 0;

    public interface OnItemClickListener {
        void onItemClick(View view, int position, IqdbResultCollecter.IqdbItem item);
        void onItemLongClick(View view , int position, IqdbResultCollecter.IqdbItem item);
    }

    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    public ResultAdapter(ArrayList<IqdbResultCollecter.IqdbItem> mData) {
        this.mData = mData;
        this.mCount = mData.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_iqdb_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        IqdbResultCollecter.IqdbItem item = mData.get(position);
        holder.mTextViewURL.setText(item.imageURL);
        holder.mTextViewSize.setText(item.size);
        holder.mTextViewSimilarity.setText(item.similarity);

        Glide.with(holder.mImageView.getContext())
                .load(item.thumbnailURL)
                .centerCrop()
                .placeholder(R.mipmap.ic_launcher)
                .crossFade()
                .into(holder.mImageView);

        if (mOnItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    int pos = holder.getLayoutPosition();
                    mOnItemClickListener.onItemClick(holder.itemView, pos, mData.get(pos));
                }
            });

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener()
            {
                @Override
                public boolean onLongClick(View v)
                {
                    int pos = holder.getLayoutPosition();
                    mOnItemClickListener.onItemLongClick(holder.itemView, pos, mData.get(pos));
                    return false;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mCount;
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