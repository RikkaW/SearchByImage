package rikka.searchbyimage.ui.apdater;

import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import rikka.searchbyimage.R;
import rikka.searchbyimage.staticdata.CustomEngine;

/**
 * Created by Rikka on 2016/1/26.
 */
public class PostFormAdapter extends RecyclerView.Adapter<PostFormAdapter.ViewHolder> {

    CustomEngine mData;
    boolean mEnabled;
    PostFormAdapter mAdapter;

    public CustomEngine getData() {
        return mData;
    }

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
                        mData.post_text_key.add("");
                        mData.post_text_value.add("");
                        mData.post_text_type.add(0);
                        mAdapter.notifyItemInserted(mData.post_text_key.size() - 1);
                    }
                });
            } else {
                holder.vView.setVisibility(View.GONE);
            }

        } else {
            holder.vKey.setText(mData.post_text_key.get(position));
            holder.vValue.setText(mData.post_text_value.get(position));

            if (mData.post_text_type.get(position) == -1) {
                holder.vValue.setText(R.string.upload_form_built_in_selector);
                holder.vValue.setEnabled(false);
            }

            if (mEnabled) {
                holder.vKey.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        mData.post_text_key.set(position, s.toString());
                    }
                });
                holder.vValue.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        mData.post_text_value.set(position, s.toString());
                    }
                });
            } else {
                holder.vKey.setEnabled(false);
                holder.vValue.setEnabled(false);
            }
        }
    }

    public PostFormAdapter(CustomEngine data, boolean enabled) {
        mData = data;
        mEnabled = enabled;
        mAdapter = this;
    }

    public PostFormAdapter() {
        mData = new CustomEngine();
        mEnabled = true;
        mAdapter = this;
    }

    @Override
    public int getItemCount() {
        return mData.post_text_key.size() + 1;
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
