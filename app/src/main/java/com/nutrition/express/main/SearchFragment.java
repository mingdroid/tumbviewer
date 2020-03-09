package com.nutrition.express.main;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.nutrition.express.R;
import com.nutrition.express.blogposts.PostListActivity;
import com.nutrition.express.common.CommonRVAdapter;
import com.nutrition.express.common.CommonViewHolder;
import com.nutrition.express.model.data.DataManager;
import com.nutrition.express.taggedposts.TaggedActivity;
import com.nutrition.express.util.FrescoUtils;

/**
 * Created by huang on 11/2/16.
 */

public class SearchFragment extends Fragment {
    private RecyclerView recyclerView;
//    private SearchHistoryHelper historyHelper;
    private CommonRVAdapter adapter;
    private boolean loaded = false;
    private DataManager dataManager = DataManager.getInstance();
    private int referSize = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.blog_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        SearchView searchView = (SearchView) view.findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                openBlogPosts(query);
//                openTaggedPosts(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        loaded = false;
        referSize = 0;
        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && isResumed()) {
            showSearchHistory();
            if (referSize < dataManager.getReferenceBlog().size()) {
                if (referSize > 0) {
                    adapter.notifyItemRangeInserted(referSize,
                            dataManager.getReferenceBlog().size() - referSize);
                } else {
                    adapter.notifyDataSetChanged();
                }
                referSize = dataManager.getReferenceBlog().size();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getUserVisibleHint()) {
            showSearchHistory();
        }
    }

    private void showSearchHistory() {
        if (loaded) {
            return;
        }
//        historyHelper = new SearchHistoryHelper();
        referSize = dataManager.getReferenceBlog().size();

        CommonRVAdapter.Builder builder = CommonRVAdapter.newBuilder();
//        builder.setData(historyHelper.getHistories());
        builder.setData(dataManager.getReferenceBlog());
        builder.addItemType(String.class, R.layout.item_search_refer_blog,
                new CommonRVAdapter.CreateViewHolder() {
                    @Override
                    public CommonViewHolder createVH(View view) {
                        return new BaseVH(view);
                    }
                });
        adapter = builder.build();
        recyclerView.setAdapter(adapter);
        loaded = true;
    }

    private void openBlogPosts(String blogName) {
        Intent intent = new Intent(getActivity(), PostListActivity.class);
        intent.putExtra("blog_name", blogName);
        startActivity(intent);
//        historyHelper.add(blogName);
        adapter.notifyDataSetChanged();
    }

    private void openTaggedPosts(String tag) {
        Intent intent = new Intent(getActivity(), TaggedActivity.class);
        intent.putExtra("tag", tag);
        startActivity(intent);
//        historyHelper.add(tag);
        adapter.notifyDataSetChanged();
    }

    private void showDeleteDialog(final  String text, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.download_delete_title);
        builder.setPositiveButton(R.string.delete_positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
//                historyHelper.remove(text);
                adapter.notifyItemRemoved(position);
            }
        });
        builder.create().show();
    }

    public void refreshData() {
        loaded = false;
        referSize = 0;
    }

    public class BaseVH extends CommonViewHolder<String>
            implements View.OnClickListener, View.OnLongClickListener {
        String name;
        TextView textView;
        SimpleDraweeView avatarView;

        public BaseVH(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
//            itemView.setOnLongClickListener(this);
            textView = (TextView) itemView.findViewById(R.id.blog_name);
            avatarView = (SimpleDraweeView) itemView.findViewById(R.id.blog_avatar);
        }

        @Override
        public void bindView(String object) {
            name = object;
            textView.setText(name);
            FrescoUtils.setTumblrAvatarUri(avatarView, name, 128);
        }

        @Override
        public void onClick(View v) {
            openBlogPosts(name);
//            openTaggedPosts(name);
        }

        @Override
        public boolean onLongClick(View v) {
            showDeleteDialog(name, getAdapterPosition());
            return true;
        }
    }


}
