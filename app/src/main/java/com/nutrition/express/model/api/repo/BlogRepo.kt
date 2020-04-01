package com.nutrition.express.model.api.repo

import androidx.lifecycle.LiveData
import com.nutrition.express.model.api.*
import com.nutrition.express.model.api.ApiClient.toLiveData
import com.nutrition.express.model.api.service.BlogService
import com.nutrition.express.model.api.bean.BlogInfo
import com.nutrition.express.model.api.bean.BlogLikes
import com.nutrition.express.model.api.bean.BlogPosts
import retrofit2.create
import kotlin.coroutines.CoroutineContext

class BlogRepo constructor(val context: CoroutineContext) {
    private val blogService: BlogService = ApiClient.getRetrofit().create()

    fun getBlogInfo(id: String, key: String): LiveData<Resource<BlogInfo>> {
        return toLiveData(context) {
            blogService.getBlogInfo(id, key)
        }
    }

    fun getBlogLikes(id: String, map: HashMap<String, String>): LiveData<Resource<BlogLikes>> {
        return toLiveData(context) {
            blogService.getBlogLikes(id, map)
        }
    }

    fun getBlogPosts(id: String, type: String, map: HashMap<String, String>): LiveData<Resource<BlogPosts>> {
        return toLiveData(context) {
            blogService.getBlogPosts(id, type, map)
        }
    }

    fun deletePost(id: String, postId: String): LiveData<Resource<String>> {
        return toLiveData(context) {
            val result = blogService.deletePost(id, postId)
            ApiResponse(result.meta, postId)
        }
    }

}