package com.nutrition.express.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.nutrition.express.model.api.Resource
import com.nutrition.express.model.api.repo.UserRepo
import java.util.*

class UserViewModel : ViewModel() {
    private val userRepo = UserRepo(viewModelScope.coroutineContext)
    private val _userData = MutableLiveData<Boolean>()
    val userInfoData = _userData.switchMap {
        userRepo.getInfo()
    }

    //follow/unFollow
    private val _followData = MutableLiveData<String>()
    private val _unFollowData = MutableLiveData<String>()
    val followData = _followData.switchMap { userRepo.follow(it) }
    val unFollowData = _unFollowData.switchMap { userRepo.unfollow(it) }

    //like/unLike
    private val _likeData = MutableLiveData<LikeRequest>()
    private val _unLikeData = MutableLiveData<LikeRequest>()
    val likeData = _likeData.switchMap { userRepo.like(it.id, it.key) }
    val unLikeData = _unLikeData.switchMap { userRepo.unlike(it.id, it.key) }

    //get dashboard
    private val _dashboard = MutableLiveData<DashboardRequest>()
    val dashboardData = _dashboard.switchMap {
        val options = HashMap<String, String>()
        options["offset"] = it.offset.toString()
        options["type"] = it.type
        userRepo.getDashboard(options)
    }
    private val _dashboardNext = MutableLiveData<DashboardRequest>()
    val dashboardNextPageData = _dashboardNext.switchMap {
        val options = HashMap<String, String>()
        options["offset"] = it.offset.toString()
        options["type"] = it.type
        userRepo.getDashboard(options)
    }

    fun fetchUserInfo() {
        _userData.value = true
    }

    //retry to get user info
    fun retryIfFailed() {
        if (userInfoData.value is Resource.Error) {
            _userData.value = true
        }
    }

    fun fetchDashboardData(type: String) {
        _dashboard.value = DashboardRequest(type, 0)
    }

    fun fetchDashboardNextPageData(offset: Int) {
        _dashboard.value?.let {
            _dashboardNext.value = DashboardRequest(it.type, offset)
        }
    }

    fun follow(url: String) {
        _followData.value = url
    }

    fun unFollow(url: String) {
        _unFollowData.value = url
    }

    fun like(id: Long, key: String) {
        _likeData.value = LikeRequest(id, key)
    }

    fun unLike(id: Long, key: String) {
        _unLikeData.value = LikeRequest(id, key)
    }

    data class LikeRequest(val id: Long, val key: String)
    data class DashboardRequest(val type: String, val offset: Int)
}