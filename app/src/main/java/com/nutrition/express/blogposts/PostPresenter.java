package com.nutrition.express.blogposts;

import android.text.TextUtils;

import com.nutrition.express.model.data.bean.PhotoPostsItem;
import com.nutrition.express.model.data.bean.VideoPostsItem;
import com.nutrition.express.model.rest.ApiService.BlogService;
import com.nutrition.express.model.rest.RestCallback;
import com.nutrition.express.model.rest.RestClient;
import com.nutrition.express.model.rest.bean.BaseBean;
import com.nutrition.express.model.rest.bean.BlogPosts;
import com.nutrition.express.model.rest.bean.PostsItem;
import com.nutrition.express.util.PreferencesUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;

/**
 * Created by huang on 5/17/16.
 */
public class PostPresenter implements PostContract.Presenter {
    public static final String[] TYPES = {"", "video", "photo"};
    public static final String FILTER_TYPE = "filter_type";

    private PostContract.View view;
    private BlogService blogService;
    private Call<BaseBean<BlogPosts>> call;
    private final int limit = 20;
    private int offset = 0;
    private String blogName;
    private int type;
    private boolean reset = false;

    public PostPresenter(PostContract.View view) {
        this.view = view;
        blogService = RestClient.getInstance().getBlogService();
        type = PreferencesUtils.getInt(FILTER_TYPE);
        if (type >= TYPES.length) {
            type = 0;
        }
    }

    @Override
    public void loadData(String blogName) {
        if (call == null) {
            this.blogName = blogName;
            HashMap<String, String> para = new HashMap<>();
            para.put("limit", Integer.toString(limit));
            para.put("offset", Integer.toString(offset));
            call = blogService.getBlogPosts(blogName, TYPES[type], para);
            call.enqueue(new RestCallback<BlogPosts>() {
                @Override
                public void onSuccess(BlogPosts blogPosts) {
                    if (view == null) {
                        return;
                    }
                    call = null;
                    showPosts(blogPosts);
                }

                @Override
                public void onError(int code, String message) {
                    if (view == null) {
                        return;
                    }
                    call = null;
                    showError(code, message);
                }
            });
        }
    }

    @Override
    public void onAttach(PostContract.View view) {
        this.view = view;
    }

    @Override
    public void onDetach() {
        view = null;
    }

    @Override
    public void setShowType(int type) {
        if (this.type != type) {
            this.type = type;
            PreferencesUtils.putInt(FILTER_TYPE, this.type);
            if (call != null) {
                call.cancel();
                call = null;
            }
            offset = 0;
            reset = true;
            loadData(blogName);
        }
    }

    @Override
    public int getShowType() {
        return type;
    }

    private void showPosts(BlogPosts blogPosts) {
        offset += blogPosts.getList().size();
        boolean hasNext = true;
        if (blogPosts.getList().size() < limit || offset >= blogPosts.getCount()) {
            hasNext = false;
        }
        boolean isAdmin = blogPosts.getBlogInfo().isAdmin();
        if (isAdmin) {
            view.hideFollowItem();
        } else if (blogPosts.getBlogInfo().isFollowed()) {
            view.onFollowed();
        } else {
            view.onUnfollowed();
        }
        List<PhotoPostsItem> postsItems = new ArrayList<>(blogPosts.getList().size());
        if (type == 0) {
            //trim to only show videos and photos
            for (PostsItem item : blogPosts.getList()) {
                item.setAdmin(isAdmin);
                if (TextUtils.equals(item.getType(), "video")) {
                    postsItems.add(new VideoPostsItem(item));
                } else if (TextUtils.equals(item.getType(), "photo")) {
                    postsItems.add(new PhotoPostsItem(item));
                }
            }
        } else if (type == 1) {
            for (PostsItem item : blogPosts.getList()) {
                item.setAdmin(isAdmin);
                postsItems.add(new VideoPostsItem(item));
            }
        } else if (type == 2) {
            for (PostsItem item : blogPosts.getList()) {
                item.setAdmin(isAdmin);
                postsItems.add(new PhotoPostsItem(item));
            }
        }
        if (reset) {
            view.resetData(postsItems.toArray(), hasNext);
            reset = false;
        } else {
            view.showData(postsItems.toArray(), hasNext);
        }
    }

    private void showError(int code, String error) {
        if (code == 0) {
            // 0 means call is canceled
            // ignore
            return;
        }
        if (view == null) {
            return;
        }
        call = null;
        view.onError(code, error);
    }

}
