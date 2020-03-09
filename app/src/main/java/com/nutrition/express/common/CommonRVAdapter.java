package com.nutrition.express.common;

import android.graphics.Color;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.nutrition.express.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


/**
 * Created by huang on 10/19/16.
 */

public class CommonRVAdapter extends RecyclerView.Adapter<CommonViewHolder> {
    private static final int TYPE_DATA_BASE = 1000;
    private static final int TYPE_UNKNOWN = 0;       //未知类型
    /* 状态 */
    private static final int EMPTY = 10;                //显示EMPTY VIEW
    private static final int LOADING = 11;              //显示LOADING VIEW
    private static final int LOADING_FAILURE = 12;      //显示LOADING FAILURE VIEW
    private static final int LOADING_NEXT = 13;         //显示LOADING NEXT VIEW
    private static final int LOADING_NEXT_FAILURE = 14; //显示LOADING NEXT FAILURE VIEW
    private static final int LOADING_FINISH = 15;       //显示LOADING FINISH VIEW

    private int state = LOADING_FINISH;

    private boolean isFinishViewEnabled = false;
    //保存了layout_id与MultiType键值对
    private SparseArray<MultiType> typeArray;
    //保存了数据类型名称与layout_id的键值对
    private HashMap<String, Integer> typeMap;
    private OnLoadListener loadListener;
    private List<Object> data;
    private Object extra;

    private View.OnClickListener onRetryListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (loadListener != null) {
                loadListener.retry();
                if (data.size() > 0) {
                    state = LOADING_NEXT;
                } else {
                    state = LOADING;
                }
                notifyItemChanged(data.size());
            }
        }
    };

    private CommonRVAdapter(Builder builder) {
        isFinishViewEnabled = builder.isFinishViewEnabled;
        typeArray = builder.typeArray;
        typeMap = builder.typeMap;
        loadListener = builder.loadListener;
        data = builder.data;
        if (data == null) {
            data = new ArrayList<>();
            state = LOADING;
        } else if (data.size() == 0) {
            state = EMPTY;
        }
    }

    @Override
    public CommonViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        MultiType type = typeArray.get(viewType);
        if (type == null) {     //check if unknown type
            TextView textView = (TextView) inflater
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
            textView.setText("Error!!! Unknown type!!!");
            textView.setTextColor(Color.parseColor("#ff0000"));
            return new CommonViewHolder(textView);
        } else {
            View view = inflater.inflate(type.layout, parent, false);
            if (state == LOADING_FAILURE) {
                view.setOnClickListener(onRetryListener);
            } else if (state == LOADING_NEXT_FAILURE) {
                view.setOnClickListener(onRetryListener);
            }
            return type.creator.createVH(view);
        }
    }

    @Override
    public void onBindViewHolder(CommonViewHolder holder, int position) {
        if (position < data.size()) {
            holder.bindView(data.get(position));
        } else {
            holder.bindView(extra);
            if (state == LOADING_NEXT && loadListener != null) {
                loadListener.loadNextPage();
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == data.size()) {
            return state;
        } else {
            Integer type = typeMap.get(data.get(position).getClass().getName());
            return (null == type ? TYPE_UNKNOWN : type);
        }
    }

    @Override
    public int getItemCount() {
        if (data.size() > 0) {
            if (state == EMPTY) {
                state = LOADING_FINISH;
            }
        } else if (state == LOADING_FINISH) {
            state = EMPTY;
        }
        if (isFinishViewEnabled) {
            return data.size() + 1;
        } else {
            return state == LOADING_FINISH ? data.size() : data.size() + 1;
        }
    }

    @Override
    public void onViewAttachedToWindow(@NonNull CommonViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        holder.onAttach();
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull CommonViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.onDetach();
    }

    /**
     *
     * @param data 列表数据，为空或无数据则表示加载完毕，不再自动加载下一页无论autoLoadingNext是否为true，
     * @param autoLoadingNext 是否自动加载下一页
     */
    public void append(Object[] data, boolean autoLoadingNext) {
        if (data != null && data.length > 0) {
            int lastDataIndex = this.data.size();
            int size = data.length;
            Collections.addAll(this.data, data);
            if (autoLoadingNext) {
                state = LOADING_NEXT;
                size++;
            } else {
                state = LOADING_FINISH;
            }
            if (lastDataIndex == 0) {
                notifyDataSetChanged();
            } else {
                notifyItemRangeChanged(lastDataIndex, size);
            }
        } else {
            if (this.data.size() > 0) {
                //all data has been loaded
                state = LOADING_FINISH;
                notifyItemChanged(this.data.size());
            } else {
                //no data, show empty view
                state = EMPTY;
                notifyDataSetChanged();
            }
        }
    }

    public void remove(int position) {
        data.remove(position);
        notifyItemRemoved(position);
    }

    public void resetData(Object[] data, boolean autoLoadingNext ) {
        this.data.clear();
        append(data, autoLoadingNext);
    }

    public void showLoadingFinish() {
        state = LOADING_FINISH;
        notifyItemChanged(data.size());
    }

    /**
     * 目前无数据，显示加载失败
     */
    public void showLoadingFailure(Object error) {
        this.extra = error;
        if (data.size() > 0) {
            state = LOADING_NEXT_FAILURE;
        } else {
            state = LOADING_FAILURE;
        }
        notifyItemChanged(data.size());
    }

    public void showReloading() {
        data.clear();
        state = LOADING;
        notifyDataSetChanged();
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public interface OnLoadListener {
        void retry();
        void loadNextPage();
    }

    public interface CreateViewHolder {
        CommonViewHolder createVH(View view);
    }

    private static class MultiType {
        int layout;
        CreateViewHolder creator;

        private MultiType(int layout, CreateViewHolder creator) {
            this.layout = layout;
            this.creator = creator;
        }
    }

    public static class Builder {
        private int emptyView = 0;
        private int loadingView = 0;
        private int failureView = 0;
        private int nextView = 0;
        private int nextFailureView = 0;
        private int finishView = 0;
        private CreateViewHolder emptyCreator = null;
        private CreateViewHolder loadingCreator = null;
        private CreateViewHolder failureCreator = null;
        private CreateViewHolder nextCreator = null;
        private CreateViewHolder nextFailureCreator = null;
        private CreateViewHolder finishCreator = null;
        private boolean isFinishViewEnabled = false;
        private OnLoadListener loadListener;
        private List<Object> data;
        private SparseArray<MultiType> typeArray = new SparseArray<>();
        private HashMap<String, Integer> typeMap = new HashMap<>();
        private int base = TYPE_DATA_BASE;
        private CreateViewHolder defaultCreator = new CreateViewHolder() {
            @Override
            public CommonViewHolder createVH(View view) {
                return new CommonViewHolder(view);
            }
        };
        private CreateViewHolder errorCreator = new CreateViewHolder() {
            @Override
            public CommonViewHolder createVH(View view) {
                return new ErrorViewHolder(view);
            }
        };


        private Builder() {
        }

        public Builder setEmptyView(@LayoutRes int emptyView,
                                    @Nullable CreateViewHolder creator) {
            this.emptyView = emptyView;
            this.emptyCreator = creator;
            return this;
        }

        public Builder setLoadingView(@LayoutRes int loadingView,
                                      @Nullable CreateViewHolder creator) {
            this.loadingView = loadingView;
            this.loadingCreator = creator;
            return this;
        }

        public Builder setFailureView(@LayoutRes int failureView,
                                      @Nullable CreateViewHolder creator) {
            this.failureView = failureView;
            this.failureCreator = creator;
            return this;
        }

        public Builder setNextView(@LayoutRes int nextView,
                                   @Nullable CreateViewHolder creator) {
            this.nextView = nextView;
            this.nextCreator = creator;
            return this;
        }

        public Builder setNextFailureView(@LayoutRes int nextFailureView,
                                          @Nullable CreateViewHolder creator) {
            this.nextFailureView = nextFailureView;
            this.nextFailureCreator = creator;
            return this;
        }

        public Builder setFinishView(@LayoutRes int finishView,
                                     @Nullable CreateViewHolder creator) {
            this.finishView = finishView;
            this.finishCreator = creator;
            return this;
        }

        /**
         * set true to show finish view when loading finished;
         * @param finishViewEnabled
         */
        public void setFinishViewEnabled(boolean finishViewEnabled) {
            isFinishViewEnabled = finishViewEnabled;
        }

        public Builder setLoadListener(OnLoadListener loadListener) {
            this.loadListener = loadListener;
            return this;
        }

        public Builder addItemType(Class c, int layout, CreateViewHolder create) {
            typeArray.put(base, new MultiType(layout, create));
            typeMap.put(c.getName(), base);
            base++;
            return this;
        }

        public Builder setData(List<Object> data) {
            this.data = data;
            return this;
        }

        public CommonRVAdapter build() {
            addStateType();
            return new CommonRVAdapter(this);
        }

        private void checkAndSetDefault() {
            if (emptyView <= 0) {
                emptyView = R.layout.item_empty;
            }
            if (loadingView <= 0) {
                loadingView = R.layout.item_loading;
            }
            if (failureView <= 0) {
                failureView = R.layout.item_loading_failure;
            }
            if (nextView <= 0) {
                nextView = R.layout.item_loading;
            }
            if (nextFailureView <= 0) {
                nextFailureView = R.layout.item_loading_failure;
            }
            if (finishView <= 0) {
                finishView = R.layout.item_finish;
            }
            if (emptyCreator == null) {
                emptyCreator = defaultCreator;
            }
            if (loadingCreator == null) {
                loadingCreator = defaultCreator;
            }
            if (failureCreator == null) {
                failureCreator = errorCreator;
            }
            if (nextCreator == null) {
                nextCreator = defaultCreator;
            }
            if (nextFailureCreator == null) {
                nextFailureCreator = errorCreator;
            }
            if (finishCreator == null) {
                finishCreator = defaultCreator;
            }
        }

        private void addStateType() {
            checkAndSetDefault();
            typeArray.put(EMPTY, new MultiType(emptyView, emptyCreator));
            typeArray.put(LOADING, new MultiType(loadingView, loadingCreator));
            typeArray.put(LOADING_FAILURE, new MultiType(failureView, failureCreator));
            typeArray.put(LOADING_NEXT, new MultiType(nextView, nextCreator));
            typeArray.put(LOADING_NEXT_FAILURE, new MultiType(nextFailureView, nextFailureCreator));
            typeArray.put(LOADING_FINISH, new MultiType(finishView, finishCreator));
        }
    }

}
