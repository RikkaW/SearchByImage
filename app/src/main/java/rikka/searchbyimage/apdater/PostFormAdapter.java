package rikka.searchbyimage.apdater;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import rikka.searchbyimage.R;
import rikka.searchbyimage.staticdata.SearchEngine;

/**
 * Created by Rikka on 2016/1/26.
 */
public class PostFormAdapter extends RecyclerView.Adapter<PostFormAdapter.ViewHolder> {
    public interface OnFocusChangeListener
    {
        void onFocusChange(View view, boolean hasFocus);
    }

    private OnFocusChangeListener mOnFocusChangeListener;

    public void setOnFocusChangeListener(OnFocusChangeListener mOnFocusChangeListener)
    {
        this.mOnFocusChangeListener = mOnFocusChangeListener;
    }

    SearchEngine mData;
    boolean mEnabled;
    PostFormAdapter mAdapter;
    int mCount;
    RecyclerView mRecyclerView;

    @Override
    public int getItemViewType(int position) {
        return position == (getItemCount() - 1) ? 1 : 0;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(viewType == 1 ? R.layout.list_item_post_form_add : R.layout.list_item_post_form, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        if (position == (getItemCount() - 1)) {
            if (mEnabled) {
                holder.vView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mAdapter.notifyItemInserted(mCount - 1);
                        mCount++;
                    }
                });
            } else {
                holder.vView.setVisibility(View.GONE);
            }

        } else {
            if (position < mData.post_text_key.size()) {
                holder.vKey.setText(mData.post_text_key.get(position));
                holder.vValue.setText(mData.post_text_value.get(position));

                if (mData.post_text_type.get(position) == -1) {
                    holder.vValue.setText(R.string.upload_form_built_in_selector);
                    holder.vValue.setEnabled(false);
                }
            }

            holder.vKey.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (mOnFocusChangeListener != null) {
                        mOnFocusChangeListener.onFocusChange(v, hasFocus);
                    }
                }
            });
            holder.vValue.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (mOnFocusChangeListener != null) {
                        mOnFocusChangeListener.onFocusChange(v, hasFocus);
                    }
                }
            });

            if (!mEnabled) {
                holder.vKey.setEnabled(false);
                holder.vValue.setEnabled(false);
            }
        }
    }

    public PostFormAdapter(SearchEngine data, boolean enabled) {
        mData = data;
        mEnabled = enabled;
        mAdapter = this;
        mCount = mData.post_text_key.size() + 1;
    }

    public PostFormAdapter() {
        mData = new SearchEngine();
        mEnabled = true;
        mAdapter = this;
        mCount = 1;
    }

    @Override
    public int getItemCount() {
        return mCount;
    }

    public void setItemCount(int count) {
        mCount = count;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        protected LinearLayout vView;
        protected EditText vKey;
        protected EditText vValue;

        public ViewHolder(View itemView) {
            super(itemView);

            vView = (LinearLayout) itemView.findViewById(R.id.linearLayout);
            vKey = (EditText) itemView.findViewById(R.id.editText_key);
            vValue = (EditText) itemView.findViewById(R.id.editText_value);
        }
    }
}
