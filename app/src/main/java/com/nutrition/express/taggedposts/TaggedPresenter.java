package com.nutrition.express.taggedposts;

import android.text.TextUtils;

import com.nutrition.express.model.data.bean.PhotoPostsItem;
import com.nutrition.express.model.data.bean.VideoPostsItem;
import com.nutrition.express.model.rest.ApiService.TaggedService;
import com.nutrition.express.model.rest.RestCallback;
import com.nutrition.express.model.rest.RestClient;
import com.nutrition.express.model.rest.bean.BaseBean;
import com.nutrition.express.model.rest.bean.PostsItem;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;

/**
 * Created by huang on 11/21/16.
 */

public class TaggedPresenter implements TaggedContract.Presenter {
    private TaggedService service;
    private Call<BaseBean<List<PostsItem>>> call;
    private TaggedContract.View view;
    private final int LIMIT = 20;
    private long featuredTimestamp;

    public TaggedPresenter(TaggedContract.View view) {
        this.view = view;
        service = RestClient.getInstance().getTaggedService();
        featuredTimestamp = System.currentTimeMillis() / 1000;
    }

    private void getData(String tag) {
        if (call == null) {
            call = service.getTaggedPosts(tag, "html", featuredTimestamp, LIMIT);
            call.enqueue(new RestCallback<List<PostsItem>>() {
                @Override
                public void onSuccess(List<PostsItem> list) {
                    if (view == null) {
                        return;
                    }
                    call = null;
                    //trim to only show videos and photos
                    List<PhotoPostsItem> postsItems = new ArrayList<>(list.size());
                    for (PostsItem item: list) {
                        if (TextUtils.equals(item.getType(), "video")) {
                            postsItems.add(new VideoPostsItem(item));
                        } else if (TextUtils.equals(item.getType(), "photo")) {
                            postsItems.add(new PhotoPostsItem(item));
                        }
                    }
                    if (list.size() > 0 &&
                            (featuredTimestamp = list.get(list.size() - 1).getFeatured_timestamp()) > 0) {
                        view.showTaggedPosts(postsItems, true);
                    } else {
                        view.showTaggedPosts(postsItems, false);
                    }
                }

                @Override
                public void onError(int code, String message) {
                    if (view == null) {
                        return;
                    }
                    call = null;
                    view.onError(code, message);
                }
            });
        }
    }

    @Override
    public void onAttach(TaggedContract.View view) {

    }

    @Override
    public void onDetach() {
        view = null;
    }

    @Override
    public void getTaggedPosts(String tag) {
        getData(tag);
    }

    @Override
    public void nextTaggedPosts(String tag) {
        getData(tag);
    }

}
