package com.nutrition.express.ui.following

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.nutrition.express.model.api.repo.UserRepo

class FollowingViewModel : ViewModel() {
    private val userRepo = UserRepo(viewModelScope.coroutineContext)
    private val _offset = MutableLiveData<Int>()
    val followingData = _offset.switchMap {
        userRepo.getFollowing(20, it)
    }

    fun getFollowingList(offset: Int) {
        _offset.value = offset
    }

}