package com.nutrition.express.model.api.repo

import androidx.lifecycle.LiveData
import com.nutrition.express.model.api.*
import com.nutrition.express.model.api.ApiClient.toLiveData
import com.nutrition.express.model.api.service.UserService
import com.nutrition.express.model.api.bean.BlogLikes
import com.nutrition.express.model.api.bean.BlogPosts
import com.nutrition.express.model.api.bean.FollowingBlog
import com.nutrition.express.model.api.bean.UserInfo
import retrofit2.create
import kotlin.coroutines.CoroutineContext

class UserRepo(val context: CoroutineContext) {
    private val userService: UserService = ApiClient.getRetrofit().create()

    fun getFollowing(limit: Int, offset: Int): LiveData<Resource<FollowingBlog>> {
        return toLiveData(context) {
            userService.getFollowing(limit, offset)
        }
    }

    fun getLikes(limit: Int, before: Long): LiveData<Resource<BlogLikes>> {
        return toLiveData(context) {
            userService.getLikes(limit, before)
        }
    }

    fun getInfo(): LiveData<Resource<UserInfo>> {
        return toLiveData(context) {
            userService.getInfo()
        }
    }

    fun getDashboard(options: Map<String, String>): LiveData<Resource<BlogPosts>> {
        return toLiveData(context) {
            userService.getDashboard(options)
        }
    }

    fun follow(url: String): LiveData<Resource<String>> {
        return toLiveData(context) {
            val result = userService.follow(url)
            ApiResponse(result.meta, url)
        }
    }

    fun unfollow(url: String): LiveData<Resource<String>> {
        return toLiveData(context) {
            val result = userService.unfollow(url)
            ApiResponse(result.meta, url)
        }
    }

    fun like(id: Long, key: String): LiveData<Resource<Long>> {
        return toLiveData(context) {
            val result = userService.like(id, key)
            ApiResponse(result.meta, id)
        }
    }

    fun unlike(id: Long, key: String): LiveData<Resource<Long>> {
        return toLiveData(context) {
            val result = userService.unlike(id, key)
            ApiResponse(result.meta, id)
        }
    }

}