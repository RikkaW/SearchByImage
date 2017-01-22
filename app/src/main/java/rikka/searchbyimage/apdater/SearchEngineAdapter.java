package rikka.searchbyimage.apdater;

import android.databinding.Bindable;
import android.databinding.Observable;
import android.databinding.PropertyChangeRegistry;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import java.util.List;

import rikka.searchbyimage.BR;
import rikka.searchbyimage.BuildConfig;
import rikka.searchbyimage.R;
import rikka.searchbyimage.databinding.ListItemEditSitesBinding;
import rikka.searchbyimage.staticdata.SearchEngine;

/**
 * Created by Rikka on 2016/1/24.
 */
public class SearchEngineAdapter extends RecyclerView.Adapter<SearchEngineAdapter.ViewHolder> {
    public interface OnItemClickListener {
        void onItemClick(View view, int position, SearchEngine item);

        void onItemLongClick(View view, int position, SearchEngine item);
    }

    /**
     * interface to show message in UI
     */
    public interface ShowMessage {
        /**
         * show "at least select one engine" message
         */
        void showNoLessThanOne();
    }

    protected OnItemClickListener mOnItemClickListener;
    protected ShowMessage showMessage;

    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    public void setShowMessage(ShowMessage showMessage) {
        this.showMessage = showMessage;
    }

    private List<SearchEngine> mData;

    public SearchEngineAdapter(List<SearchEngine> data) {
        mData = data;
    }

    @Override
    public SearchEngineAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.list_item_edit_sites, parent, false);

        return new ViewHolder(itemView);
    }


    private static final int VIEW_TYPE_ITEM = 1;
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

        SearchEngine engine = mData.get(position);
        boolean needDivider = !(position == BUILT_IN_ENGINES - 1 || (position == mData.size() - 1));
        holder.bind(engine, viewType, needDivider);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    /**
     * get number of enabled engines
     */
    public int getEnabledEngineNumber() {
        int enabledNumber = 0;
        for (SearchEngine searchEngine : mData) {
            if (searchEngine.getEnabled() == 1) {
                enabledNumber++;
            }
        }
        return enabledNumber;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ListItemEditSitesBinding binding;

        public ViewHolder(View itemView) {
            super(itemView);
            binding = ListItemEditSitesBinding.bind(itemView);
        }

        /**
         * bind view
         *
         * @param engine      the engine bind to view
         * @param layout_flag layout flag for view
         * @param needDivider if view need a divider
         */
        private void bind(SearchEngine engine, int layout_flag, boolean needDivider) {
            binding.setEngine(engine);
            binding.setListener(new Listener());
            binding.setLayoutStyle(new LayoutStyle(layout_flag, needDivider));
        }

        public class LayoutStyle implements Observable {

            private PropertyChangeRegistry pcr = new PropertyChangeRegistry();

            private int viewType;
            private boolean needDivider;

            public LayoutStyle(int layout_flag, boolean needDivider) {
                this.viewType = layout_flag;
                this.needDivider = needDivider;
            }

            @Bindable
            public boolean getNeedShowHead() {
                return (viewType & (VIEW_TYPE_HEADER_BUILT_IN | VIEW_TYPE_HEADER_CUSTOM)) != 0;
            }

            @Bindable
            public String getHeadText() {
                return (viewType & VIEW_TYPE_HEADER_BUILT_IN) >= 1 ?
                        itemView.getContext().getString(R.string.built_in) : itemView.getContext().getString(R.string.custom);
            }

            @Bindable
            public int getViewType() {
                return viewType;
            }

            @Bindable
            public boolean getNeedEmptyView() {
                return (viewType & VIEW_TYPE_EMPTY) != 0;
            }

            @Bindable
            public boolean getNeedDivider() {
                return needDivider;
            }

            public void setViewType(int viewType) {
                this.viewType = viewType;
                pcr.notifyChange(this, BR.viewType);
            }

            @Override
            public void addOnPropertyChangedCallback(OnPropertyChangedCallback callback) {
                pcr.add(callback);
            }

            @Override
            public void removeOnPropertyChangedCallback(OnPropertyChangedCallback callback) {
                pcr.remove(callback);
            }
        }

        public class Listener implements Observable {

            private PropertyChangeRegistry pcr = new PropertyChangeRegistry();

            public SwitchCompat.OnCheckedChangeListener switchCheckedChangeListener = new SwitchCompat.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        binding.getEngine().setEnabled(1);
                        return;
                    }
                    if (getEnabledEngineNumber() > 1) {
                        binding.getEngine().setEnabled(0);
                    } else {
                        if (showMessage != null) {
                            showMessage.showNoLessThanOne();
                        }
                        buttonView.setChecked(true);
                    }
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
