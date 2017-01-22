package rikka.searchbyimage.apdater;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by Rikka on 2017/1/22.
 */

public abstract class SimpleAdapter<T, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    public interface OnItemClickListener {
        void OnItemClick(int position);
    }

    private OnItemClickListener mListener;

    private FilterAdapterHelper<T> mFilterAdapterHelper;

    public SimpleAdapter() {
        mFilterAdapterHelper = new FilterAdapterHelper<T>(this) {
            @Override
            public boolean contains(String key, T obj) {
                return true;
            }

            @Override
            public boolean check(int key, int value, T obj) {
                return true;
            }

            @Override
            public boolean check(int key, boolean value, T obj) {
                return true;
            }
        };
    }

    @Override
    public void onBindViewHolder(final VH holder, int position) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.OnItemClick(holder.getAdapterPosition());
                }
            }
        });
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    @Override
    public int getItemCount() {
        return mFilterAdapterHelper.getFilteredData().size();
    }

    public FilterAdapterHelper<T> getHelper() {
        return mFilterAdapterHelper;
    }
}
