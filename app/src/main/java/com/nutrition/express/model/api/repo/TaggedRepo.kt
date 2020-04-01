package com.nutrition.express.model.api.repo

import androidx.lifecycle.LiveData
import com.nutrition.express.model.api.ApiClient
import com.nutrition.express.model.api.ApiResponse
import com.nutrition.express.model.api.Resource
import com.nutrition.express.model.api.service.TaggedService
import com.nutrition.express.model.data.bean.PhotoPostsItem
import com.nutrition.express.model.data.bean.VideoPostsItem
import retrofit2.create
import kotlin.collections.ArrayList
import kotlin.coroutines.CoroutineContext

class TaggedRepo(val context: CoroutineContext) {
    val service: TaggedService = ApiClient.getRetrofit().create()
    fun getTaggedPosts(tag: String, filter: String?, timestamp: Long?, limit: Int): LiveData<Resource<List<PhotoPostsItem>>> {
        return ApiClient.toLiveData(context) {
            val result = service.getTaggedPosts(tag, filter, timestamp, limit)
            //trim to only show videos and photos
            val item = result.response
            val postsItems: MutableList<PhotoPostsItem> = ArrayList()
            item?.let {
                for (postItem in it) {
                    if (postItem.type == "video") {
                        postsItems.add(VideoPostsItem(postItem))
                    } else if (postItem.type == "photo") {
                        postsItems.add(PhotoPostsItem(postItem))
                    }
                }
            }
            ApiResponse(result.meta, postsItems.toList())
        }
    }
}