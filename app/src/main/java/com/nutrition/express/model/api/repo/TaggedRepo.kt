package com.nutrition.express.model.api.repo

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.nutrition.express.model.api.*
import com.nutrition.express.model.api.service.TaggedService
import com.nutrition.express.model.data.bean.PhotoPostsItem
import com.nutrition.express.model.data.bean.VideoPostsItem
import retrofit2.create
import kotlin.collections.ArrayList
import kotlin.coroutines.CoroutineContext

class TaggedRepo(val context: CoroutineContext) {
    val service: TaggedService = ApiClient.getRetrofit().create()
    fun getTaggedPosts(tag: String, filter: String?, timestamp: Long?, limit: Int): LiveData<Resource<List<PhotoPostsItem>>> {
        return liveData(context) {
            emit(Resource.loading(null))
            val result = callFromNet {
                service.getTaggedPosts(tag, filter, timestamp, limit)
            }
            //trim to only show videos and photos
            if (result.status == Status.SUCCESS) {
                val item = result.data
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
                emit(Resource.success(postsItems.toList()))
            } else {
                emit(Resource.error(result.code, result.message, null))
            }
        }
    }
}