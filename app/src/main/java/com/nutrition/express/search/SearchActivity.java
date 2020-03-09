package com.nutrition.express.search;

import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
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

public class SearchActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
//    private SearchHistoryHelper historyHelper;
    private CommonRVAdapter adapter;
    private DataManager dataManager = DataManager.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(null);
        }
        recyclerView = (RecyclerView) findViewById(R.id.blog_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        showSearchHistory();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        SearchView searchView = null;
        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
        }
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
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
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSearchHistory() {
//        historyHelper = new SearchHistoryHelper();
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
    }

    private void openBlogPosts(String blogName) {
        Intent intent = new Intent(this, PostListActivity.class);
        intent.putExtra("blog_name", blogName);
        startActivity(intent);
//        historyHelper.add(blogName);
        adapter.notifyDataSetChanged();
    }

    private void openTaggedPosts(String tag) {
        Intent intent = new Intent(this, TaggedActivity.class);
        intent.putExtra("tag", tag);
        startActivity(intent);
//        historyHelper.add(tag);
        adapter.notifyDataSetChanged();
    }

    private void showDeleteDialog(final  String text, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
