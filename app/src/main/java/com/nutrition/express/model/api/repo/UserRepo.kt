package com.nutrition.express.model.api.repo

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.nutrition.express.model.api.ApiClient
import com.nutrition.express.model.api.InProgress
import com.nutrition.express.model.api.Resource
import com.nutrition.express.model.api.bean.BlogLikes
import com.nutrition.express.model.api.bean.BlogPosts
import com.nutrition.express.model.api.bean.FollowingBlog
import com.nutrition.express.model.api.bean.UserInfo
import com.nutrition.express.model.api.callFromNet
import com.nutrition.express.model.api.service.UserService
import retrofit2.create
import kotlin.coroutines.CoroutineContext

class UserRepo(val context: CoroutineContext) {
    private val userService: UserService = ApiClient.getRetrofit().create()

    fun getFollowing(limit: Int, offset: Int): LiveData<Resource<FollowingBlog>> {
        return liveData(context) {
            emit(InProgress)
            emit(callFromNet {
                userService.getFollowing(limit, offset)
            })
        }
    }

    fun getLikes(limit: Int, before: Long): LiveData<Resource<BlogLikes>> {
        return liveData(context) {
            emit(InProgress)
            emit(callFromNet {
                userService.getLikes(limit, before)
            })
        }
    }

    fun getInfo(): LiveData<Resource<UserInfo>> {
        return liveData(context) {
            emit(InProgress)
            emit(callFromNet(userService::getInfo))
        }
    }

    fun getDashboard(options: Map<String, String>): LiveData<Resource<BlogPosts>> {
        return liveData(context) {
            emit(InProgress)
            emit(callFromNet {
                userService.getDashboard(options)
            })
        }
    }

    fun follow(url: String): LiveData<Resource<String>> {
        return liveData(context) {
            emit(InProgress)
            val result = callFromNet {
                userService.follow(url)
            }
            when (result) {
                is Resource.Success -> emit(Resource.Success(url))
                is Resource.Error -> emit(Resource.Error(result.code, result.message))
                is Resource.Loading -> {
                }
            }
        }
    }

    fun unfollow(url: String): LiveData<Resource<String>> {
        return liveData(context) {
            emit(InProgress)
            val result = callFromNet {
                userService.unfollow(url)
            }
            when (result) {
                is Resource.Success -> emit(Resource.Success(url))
                is Resource.Error -> emit(Resource.Error(result.code, result.message))
                is Resource.Loading -> {
                }
            }
        }
    }

    fun like(id: Long, key: String): LiveData<Resource<Long>> {
        return liveData(context) {
            emit(InProgress)
            val result = callFromNet {
                userService.like(id, key)
            }
            when (result) {
                is Resource.Success -> emit(Resource.Success(id))
                is Resource.Error -> emit(Resource.Error(result.code, result.message))
                is Resource.Loading -> {
                }
            }
        }
    }

    fun unlike(id: Long, key: String): LiveData<Resource<Long>> {
        return liveData(context) {
            emit(InProgress)
            val result = callFromNet {
                userService.unlike(id, key)
            }
            when (result) {
                is Resource.Success -> emit(Resource.Success(id))
                is Resource.Error -> emit(Resource.Error(result.code, result.message))
                is Resource.Loading -> {
                }
            }
        }
    }

}