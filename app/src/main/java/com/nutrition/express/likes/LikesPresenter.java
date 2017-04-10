package com.nutrition.express.likes;

import android.text.TextUtils;

import com.nutrition.express.model.data.bean.PhotoPostsItem;
import com.nutrition.express.model.data.bean.VideoPostsItem;
import com.nutrition.express.model.rest.ApiService.BlogService;
import com.nutrition.express.model.rest.ApiService.UserService;
import com.nutrition.express.model.rest.ResponseListener;
import com.nutrition.express.model.rest.RestCallback;
import com.nutrition.express.model.rest.RestClient;
import com.nutrition.express.model.rest.bean.BaseBean;
import com.nutrition.express.model.rest.bean.BlogLikes;
import com.nutrition.express.model.rest.bean.PostsItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;

/**
 * Created by huang on 11/8/16.
 */

public class LikesPresenter implements LikesContract.LikesPresenter, ResponseListener {
    private LikesContract.LikesView view;
    private UserService userService;
    private BlogService blogService;
    private Call<BaseBean<BlogLikes>> call;
    private final int limit = 20;
    private int total = 0;
    private long before = -1;
    private String name;

    public LikesPresenter(LikesContract.LikesView view) {
        this.view = view;
        userService = RestClient.getInstance().getUserService();
    }

    @Override
    public void getLikePosts() {
        if (call == null) {
            if (userService == null) {
                userService = RestClient.getInstance().getUserService();
            }
            if (before < 0) {
                before = System.currentTimeMillis() / 1000;
            }
            call = userService.getLikes(limit, before);
            call.enqueue(new RestCallback<BlogLikes>(this, "likes"));
        }
    }

    @Override
    public void getLikePosts(String name) {
        if (call == null) {
            if (blogService == null) {
                blogService = RestClient.getInstance().getBlogService();
            }
            this.name = name;
            if (before < 0) {
                before = System.currentTimeMillis() / 1000;
            }
            HashMap<String, String> options = new HashMap<>(2);
            options.put("limit", "" + limit);
            options.put("before", "" + before);
            call = blogService.getBlogLikes(name, options);
            call.enqueue(new RestCallback<BlogLikes>(this, "likes"));
        }
    }

    @Override
    public void nextLikePosts() {
        if (name != null) {
            getLikePosts(name);
        } else {
            getLikePosts();
        }
    }

    @Override
    public void onAttach() {

    }

    @Override
    public void onDetach() {
        view = null;
        if (call != null) {
            call.cancel();
        }
    }

    @Override
    public void onResponse(BaseBean baseBean, String tag) {
        if (view == null) {
            return;
        }
        call = null;
        BlogLikes likes = (BlogLikes) baseBean.getResponse();
        List<PostsItem> data = likes.getList();
        total += data.size();
        if (data.size() > 0) {
            before = data.get(data.size() - 1).getTimestamp();
        }
        boolean hasNext = true;
        if (total >= likes.getCount() || data.size() < 1) {
            hasNext = false;
        }
        //trim to only show videos and photos
        List<PhotoPostsItem> postsItems = new ArrayList<>(data.size());
        for (PostsItem item: data) {
            if (TextUtils.equals(item.getType(), "video")) {
                postsItems.add(new VideoPostsItem(item));
            } else if (TextUtils.equals(item.getType(), "photo")) {
                postsItems.add(new PhotoPostsItem(item));
            }
        }
        if (hasNext && postsItems.size() < 1) {
            nextLikePosts();
        } else {
            view.showLikePosts(postsItems, hasNext);
        }
    }

    @Override
    public void onError(int code, String error, String tag) {
        if (view == null) {
            return;
        }
        call = null;
        view.onError(code, error);
    }

    @Override
    public void onFailure(Throwable t, String tag) {
        if (view == null) {
            return;
        }
        call = null;
        view.onFailure(t);
    }

}
