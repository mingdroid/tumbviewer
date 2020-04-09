package com.nutrition.express.ui.post.blog

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.nutrition.express.model.api.ApiClient
import com.nutrition.express.model.api.ApiResponse
import com.nutrition.express.model.api.Resource
import com.nutrition.express.model.api.repo.TestRepo
import com.nutrition.express.model.api.service.TestService
import com.nutrition.express.model.api.bean.UserInfo
import kotlinx.coroutines.*
import retrofit2.create

class TestViewModel : ViewModel() {
    private val testRepo =  TestRepo(viewModelScope.coroutineContext)
    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                spendCPU(5000)
                Log.d("TestViewModel", "spendCPU: end")
            }
            Log.d("TestViewModel", "spendCPU: this block code continue in ${Thread.currentThread().name}")
        }
        Log.d("TestViewModel", "init: not block by spendCPU -> ${Thread.currentThread().name}")
    }

    fun spendCPU(ms: Long) = runBlocking {
        val start = System.currentTimeMillis()
        var gap = start
        while (isActive && System.currentTimeMillis() - start < ms) {
            if (System.currentTimeMillis() - gap > 100) {
                println("spend cpu")
                gap = System.currentTimeMillis()
            }
        }
    }

    fun log() {
        Log.d("TestViewModel", "log: just log")
    }

    val userInfoData1 = MutableLiveData<Resource<UserInfo>>()
    fun fetchUserInfo() {
        Log.d("TestViewModel", "init: ${Thread.currentThread().name}")
        viewModelScope.launch {
            Log.d("TestViewModel", "launch: ${Thread.currentThread().name}")
            try {
                Log.d("TestViewModel", "thread: ${Thread.currentThread().id}")
                val service: TestService = ApiClient.getRetrofit().create()
                val response: ApiResponse<UserInfo> = service.getUserInfo()
                Log.d("TestViewModel", "response: ${Thread.currentThread().name}")
                Log.d("TestViewModel", "response: ${response.response?.user?.name}")
                userInfoData1.value = Resource.Success(response.response)
            } catch (e: Throwable) {
                Log.d("TestViewModel", "e: ${e.message}")
            }
            Log.d("TestViewModel", "fetchUserInfo: after await")
        }
        Log.d("TestViewModel", "fetchUserInfo: not block")
    }

    private val _uidData = MutableLiveData<String>()
    val userInfoData = _uidData.switchMap {
        testRepo.getUserInfo(it)
    }

    fun setUserId(uid: String) {
        _uidData.value = uid
    }

}