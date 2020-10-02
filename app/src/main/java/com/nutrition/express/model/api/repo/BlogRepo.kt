package com.nutrition.express.model.api.repo

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.nutrition.express.model.api.ApiClient
import com.nutrition.express.model.api.InProgress
import com.nutrition.express.model.api.Resource
import com.nutrition.express.model.api.bean.BlogInfo
import com.nutrition.express.model.api.bean.BlogLikes
import com.nutrition.express.model.api.bean.BlogPosts
import com.nutrition.express.model.api.callFromNet
import com.nutrition.express.model.api.service.BlogService
import retrofit2.create
import kotlin.coroutines.CoroutineContext

class BlogRepo constructor(val context: CoroutineContext) {
    private val blogService: BlogService = ApiClient.getRetrofit().create()

    fun getBlogInfo(id: String, key: String): LiveData<Resource<BlogInfo>> {
        return liveData(context) {
            emit(InProgress)
            emit(callFromNet {
                blogService.getBlogInfo(id, key)
            })
        }
    }

    fun getBlogLikes(id: String, map: HashMap<String, String>): LiveData<Resource<BlogLikes>> {
        return liveData(context) {
            emit(InProgress)
            emit(callFromNet {
                blogService.getBlogLikes(id, map)
            })
        }
    }

    fun getBlogPosts(
        id: String,
        type: String,
        map: HashMap<String, String>
    ): LiveData<Resource<BlogPosts>> {
        return liveData(context) {
            emit(InProgress)
            emit(callFromNet {
                blogService.getBlogPosts(id, type, map)
            })
        }
    }

    fun deletePost(id: String, postId: String): LiveData<Resource<String>> {
        return liveData(context) {
            emit(InProgress)
            val result = callFromNet {
                blogService.deletePost(id, postId)
            }
            when (result) {
                is Resource.Success -> emit(Resource.Success(postId))
                is Resource.Error -> emit(Resource.Error(result.code, result.message))
                is Resource.Loading -> {
                }
            }
        }
    }

}