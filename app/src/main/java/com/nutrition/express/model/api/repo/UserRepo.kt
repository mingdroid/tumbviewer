package com.nutrition.express.model.api.repo

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.nutrition.express.model.api.*
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
        return liveData(context) {
            emit(Resource.loading(null))
            emit(callFromNet {
                userService.getFollowing(limit, offset)
            })
        }
    }

    fun getLikes(limit: Int, before: Long): LiveData<Resource<BlogLikes>> {
        return liveData(context) {
            emit(Resource.loading(null))
            emit(callFromNet {
                userService.getLikes(limit, before)
            })
        }
    }

    fun getInfo(): LiveData<Resource<UserInfo>> {
        return liveData(context) {
            emit(Resource.loading(null))
            emit(callFromNet {
                userService.getInfo()
            })
        }
    }

    fun getDashboard(options: Map<String, String>): LiveData<Resource<BlogPosts>> {
        return liveData(context) {
            emit(Resource.loading(null))
            emit(callFromNet {
                userService.getDashboard(options)
            })
        }
    }

    fun follow(url: String): LiveData<Resource<String>> {
        return liveData(context) {
            emit(Resource.loading(null))
            val result = callFromNet {
                userService.follow(url)
            }
            if (result.status == Status.SUCCESS) {
                emit(Resource.success(url))
            } else {
                emit(Resource.error(result.code, result.message, null))
            }
        }
    }

    fun unfollow(url: String): LiveData<Resource<String>> {
        return liveData(context) {
            emit(Resource.loading(null))
            val result = callFromNet {
                userService.unfollow(url)
            }
            if (result.status == Status.SUCCESS) {
                emit(Resource.success(url))
            } else {
                emit(Resource.error(result.code, result.message, null))
            }
        }
    }

    fun like(id: Long, key: String): LiveData<Resource<Long>> {
        return liveData(context) {
            emit(Resource.loading(null))
            val result = callFromNet {
                userService.like(id, key)
            }
            if (result.status == Status.SUCCESS) {
                emit(Resource.success(id))
            } else {
                emit(Resource.error(result.code, result.message, null))
            }
        }
    }

    fun unlike(id: Long, key: String): LiveData<Resource<Long>> {
        return liveData(context) {
            emit(Resource.loading(null))
            val result = callFromNet {
                userService.unlike(id, key)
            }
            if (result.status == Status.SUCCESS) {
                emit(Resource.success(id))
            } else {
                emit(Resource.error(result.code, result.message, null))
            }
        }
    }

}