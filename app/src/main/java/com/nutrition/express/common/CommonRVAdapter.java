package com.nutrition.express.common;

import android.graphics.Color;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

    //保存了layout_id与MultiType键值对
    private SparseArray<MultiType> typeArray;
    //保存了数据类型名称与layout_id的键值对
    private HashMap<String, Integer> typeMap;
    private OnLoadListener loadListener;
    private List<Object> data = new ArrayList<>();

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
        typeArray = builder.typeArray;
        typeMap = builder.typeMap;
        loadListener = builder.loadListener;
        if (builder.data == null) {
            state = LOADING;
        } else if (builder.data.length == 0) {
            state = EMPTY;
        } else {
            Collections.addAll(data, builder.data);
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
            holder.bindView(null);
            if (state == LOADING_NEXT) {
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
        return state == LOADING_FINISH ? data.size() : data.size() + 1;
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
            notifyItemRangeChanged(lastDataIndex, size);
        } else {
            if (this.data.size() > 0) {
                //all data has been loaded
                state = LOADING_FINISH;
            } else {
                //no data, show empty view
                state = EMPTY;
            }
            notifyItemChanged(this.data.size());
        }
    }

    public void showLoadingFinish() {
        state = LOADING_FINISH;
        notifyItemChanged(data.size());
    }

    /**
     * 目前无数据，显示加载失败
     */
    public void showLoadingFailure() {
        state = LOADING_FAILURE;
        notifyItemChanged(data.size());
    }

    /**
     * 目前有数据，显示加载下一页失败
     */
    public void showLoadingNextFailure() {
        state = LOADING_NEXT_FAILURE;
        notifyItemChanged(data.size());
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
        private CreateViewHolder emptyCreator = null;
        private CreateViewHolder loadingCreator = null;
        private CreateViewHolder failureCreator = null;
        private CreateViewHolder nextCreator = null;
        private CreateViewHolder nextFailureCreator = null;
        private OnLoadListener loadListener;
        private Object[] data;
        private SparseArray<MultiType> typeArray = new SparseArray<>();
        private HashMap<String, Integer> typeMap = new HashMap<>();
        private int base = TYPE_DATA_BASE;
        private CreateViewHolder defaultCreator = new CreateViewHolder() {
            @Override
            public CommonViewHolder createVH(View view) {
                return new CommonViewHolder(view);
            }
        };


        private Builder() {
        }

        public Builder setEmptyView(@LayoutRes int emptyView,
                                    @Nullable CreateViewHolder creator) {
            this.emptyView = emptyView;
            return this;
        }

        public Builder setLoadingView(@LayoutRes int loadingView,
                                      @Nullable CreateViewHolder creator) {
            this.loadingView = loadingView;
            return this;
        }

        public Builder setFailureView(@LayoutRes int failureView,
                                      @Nullable CreateViewHolder creator) {
            this.failureView = failureView;
            return this;
        }

        public Builder setNextView(@LayoutRes int nextView,
                                   @Nullable CreateViewHolder creator) {
            this.nextView = nextView;
            return this;
        }

        public Builder setNextFailureView(@LayoutRes int nextFailureView,
                                          @Nullable CreateViewHolder creator) {
            this.nextFailureView = nextFailureView;
            return this;
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

        public Builder setData(Object[] data) {
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
            if (emptyCreator == null) {
                emptyCreator = defaultCreator;
            }
            if (loadingCreator == null) {
                loadingCreator = defaultCreator;
            }
            if (failureCreator == null) {
                failureCreator = defaultCreator;
            }
            if (nextCreator == null) {
                nextCreator = defaultCreator;
            }
            if (nextFailureCreator == null) {
                nextFailureCreator = defaultCreator;
            }
        }

        private void addStateType() {
            checkAndSetDefault();
            typeArray.put(EMPTY, new MultiType(emptyView, emptyCreator));
            typeArray.put(LOADING, new MultiType(loadingView, loadingCreator));
            typeArray.put(LOADING_FAILURE, new MultiType(failureView, failureCreator));
            typeArray.put(LOADING_NEXT, new MultiType(nextView, nextCreator));
            typeArray.put(LOADING_NEXT_FAILURE, new MultiType(nextFailureView, nextFailureCreator));
        }
    }

}
