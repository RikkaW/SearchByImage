package rikka.searchbyimage.ui.apdater;

import android.databinding.Bindable;
import android.databinding.BindingAdapter;
import android.databinding.Observable;
import android.databinding.PropertyChangeRegistry;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.List;

import rikka.searchbyimage.BR;
import rikka.searchbyimage.BuildConfig;
import rikka.searchbyimage.R;
import rikka.searchbyimage.databinding.ListItemEditSitesBinding;
import rikka.searchbyimage.staticdata.CustomEngine;

/**
 * Created by Rikka on 2016/1/24.
 */
public class SearchEngineAdapter extends RecyclerView.Adapter<SearchEngineAdapter.ViewHolder> {
    public interface OnItemClickListener {
        void onItemClick(View view, int position, CustomEngine item);

        void onItemLongClick(View view, int position, CustomEngine item);
    }

    protected static OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        SearchEngineAdapter.mOnItemClickListener = mOnItemClickListener;
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


    private static final int VIEW_TYPE_ITEM = 1 << 0;
    private static final int VIEW_TYPE_HEADER_BUILT_IN = 1 << 1;
    private static final int VIEW_TYPE_HEADER_CUSTOM = 1 << 2;
    private static final int VIEW_TYPE_EMPTY = 1 << 3;

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

        if (position == BUILT_IN_ENGINES - 1 || position == getItemCount() - 1) {
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

        CustomEngine engine = mData.get(position);
        holder.bind(engine, viewType);

        if (position == BUILT_IN_ENGINES - 1 || (position == mData.size() - 1)) {
            holder.vDivider.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @BindingAdapter({"bind:layout_flag"})
    public static void setHeadViewableAndText(TextView vHead, Integer layout_flag) {
        if (layout_flag == null) {
            layout_flag = VIEW_TYPE_HEADER_BUILT_IN;
        }
        if ((layout_flag & (VIEW_TYPE_HEADER_BUILT_IN | VIEW_TYPE_HEADER_CUSTOM)) == 0) {
            vHead.setVisibility(View.GONE);
        } else {
            vHead.setText(((layout_flag & VIEW_TYPE_HEADER_BUILT_IN) >= 1) ?
                    vHead.getContext().getString(R.string.built_in) : vHead.getContext().getString(R.string.custom));
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        protected View vDivider;
        protected View ViewEmpty;

        private ListItemEditSitesBinding binding;

        public ViewHolder(View itemView) {
            super(itemView);
            binding = ListItemEditSitesBinding.bind(itemView);
            vDivider = itemView.findViewById(R.id.fake_divider);
            ViewEmpty = itemView.findViewById(R.id.empty_view);
        }

        private void bind(CustomEngine engine, int layout_flag) {
            binding.setEngine(engine);
            binding.setListener(new Listener(layout_flag));
        }

        public class Listener implements Observable {
            private int layout_flag;
            private PropertyChangeRegistry pcr = new PropertyChangeRegistry();

            @Bindable
            public int getLayout_flag() {
                return layout_flag;
            }

            public void setLayout_flag(int layout_flag) {
                this.layout_flag = layout_flag;
                pcr.notifyChange(this, BR.layout_flag);
            }

            public Listener(int layout_flag) {
                this.layout_flag = layout_flag;
            }

            public SwitchCompat.OnCheckedChangeListener switchCheckedChangeListener = new SwitchCompat.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    binding.getEngine().setEnabled(isChecked ? 1 : 0);
                }
            };

            public View.OnClickListener viewOnClick = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getLayoutPosition();
                    mOnItemClickListener.onItemClick(itemView,
                            pos,
                            binding.getEngine());
                }
            };
            public View.OnLongClickListener viewOnLongClick = new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int pos = getLayoutPosition();
                    mOnItemClickListener.onItemLongClick(itemView,
                            pos,
                            binding.getEngine());
                    return false;
                }
            };

            @Override
            public void addOnPropertyChangedCallback(OnPropertyChangedCallback callback) {
                pcr.add(callback);
            }

            @Override
            public void removeOnPropertyChangedCallback(OnPropertyChangedCallback callback) {
                pcr.remove(callback);
            }
        }
    }
}
