package rikka.searchbyimage.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.annotation.StyleRes;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import rikka.searchbyimage.R;
import rikka.searchbyimage.apdater.SimpleAdapter;
import rikka.searchbyimage.utils.Utils;
import rikka.searchbyimage.viewholder.ListBottomSheetItemViewHolder;

/**
 * Created by Rikka on 2017/1/22.
 */

public class ListBottomSheetDialog extends BottomSheetDialog {

    public ListBottomSheetDialog(@NonNull Context context) {
        super(context);
    }

    public ListBottomSheetDialog(@NonNull Context context, @StyleRes int theme) {
        super(context, theme);
    }

    protected ListBottomSheetDialog(@NonNull Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public void setItems(@StringRes int[] resId, @DrawableRes int[] drawableResId, @Nullable final OnClickListener listener) {
        CharSequence[] items = new CharSequence[resId.length];
        Drawable[] drawables = new Drawable[resId.length];
        for (int i = 0; i < resId.length; i++) {
            items[i] = getContext().getString(resId[i]);
            drawables[i] = AppCompatResources.getDrawable(getContext(), drawableResId[i]);
        }
        setItems(items, drawables, listener);
    }

    public void setItems(CharSequence[] items, Drawable[] drawables, @Nullable final OnClickListener listener) {
        List<Data> list = new ArrayList<>();
        for (int i = 0; i < items.length; i++) {
            list.add(new Data(items[i].toString(), drawables[i]));
        }

        SimpleAdapter<Data, ListBottomSheetItemViewHolder> adapter = new SimpleAdapter<Data, ListBottomSheetItemViewHolder>() {
            @Override
            public void onBindViewHolder(ListBottomSheetItemViewHolder holder, int position) {
                super.onBindViewHolder(holder, position);

                Data data = getHelper().get(position);
                holder.text.setText(data.text);
                TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(holder.text, data.icon, null, null, null);
                DrawableCompat.setTintList(data.icon, holder.text.getTextColors());
            }

            @Override
            public ListBottomSheetItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return new ListBottomSheetItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.bottom_sheet_list_ltem, parent, false));
            }
        };

        adapter.getHelper().setOriginalData(list);
        adapter.setOnItemClickListener(new SimpleAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(int position) {
                if (listener != null) {
                    listener.onClick(ListBottomSheetDialog.this, position);
                }
            }
        });

        RecyclerView recyclerView = new RecyclerView(getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setPadding(0, Utils.dpToPx(8), 0, Utils.dpToPx(8));
        recyclerView.setAdapter(adapter);

        setContentView(recyclerView);
    }

    private static final class Data {
        private Drawable icon;
        private String text;

        public Data(String text, Drawable icon) {
            this.icon = icon;
            this.text = text;
        }

        public Drawable getIcon() {
            return icon;
        }

        public String getText() {
            return text;
        }
    }

    public static final class Builder {

        private Context mContext;
        private int mTheme;
        private boolean mCancelable;
        private OnCancelListener mOnCancelListener;
        private CharSequence[] mItems;
        private Drawable[] mIcons;
        private OnClickListener mOnClickListener;

        public Builder(Context context) {
            this(context, 0);
        }

        public Builder(Context context, int theme) {
            mContext = context;
            mTheme = theme;
            mCancelable = true;
        }

        public Builder setCancelable(boolean cancelable) {
            mCancelable = cancelable;
            return setCancelable(cancelable, null);
        }

        public Builder setCancelable(boolean cancelable, OnCancelListener cancelListener) {
            mCancelable = cancelable;
            mOnCancelListener = cancelListener;
            return this;
        }

        public Builder setItems(Collection<CharSequence> items) {
            return setItems(items.toArray(new CharSequence[items.size()]));
        }

        /*public Builder setItems(Collection<CharSequence> items) {
            return setItems(items.toArray(new CharSequence[items.size()]));
        }*/

        public Builder setItems(CharSequence[] items) {
            mItems = items;
            return this;
        }

        private Builder setItems(@StringRes Integer[] resId) {
            mItems = new CharSequence[resId.length];
            for (int i = 0; i < resId.length; i++) {
                mItems[i] = mContext.getString(resId[i]);
            }
            return this;
        }

        public Builder setItems(@StringRes int[] resId) {
            mItems = new CharSequence[resId.length];
            for (int i = 0; i < resId.length; i++) {
                mItems[i] = mContext.getString(resId[i]);
            }
            return this;
        }

        public Builder setIcons(Drawable[] items) {
            mIcons = items;
            return this;
        }

        public Builder setIcons(Collection<Integer> items) {
            return setIcons(items.toArray(new Integer[items.size()]));
        }

        private Builder setIcons(@DrawableRes Integer[] resId) {
            mIcons = new Drawable[resId.length];
            for (int i = 0; i < resId.length; i++) {
                mIcons[i] = AppCompatResources.getDrawable(mContext, resId[i]);
            }
            return this;
        }

        public Builder setIcons(@DrawableRes int[] resId) {
            mIcons = new Drawable[resId.length];
            for (int i = 0; i < resId.length; i++) {
                mIcons[i] = AppCompatResources.getDrawable(mContext, resId[i]);
            }
            return this;
        }

        public Builder setOnClickListener(OnClickListener onClickListener) {
            mOnClickListener = onClickListener;
            return this;
        }

        public ListBottomSheetDialog build() {
            ListBottomSheetDialog dialog = new ListBottomSheetDialog(mContext, mTheme);
            dialog.setCancelable(mCancelable);
            dialog.setOnCancelListener(mOnCancelListener);
            dialog.setItems(mItems, mIcons, mOnClickListener);
            return dialog;
        }

        public void show() {
            build().show();
        }
    }
}
