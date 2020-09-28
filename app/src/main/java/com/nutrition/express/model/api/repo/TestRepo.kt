package com.nutrition.express.model.api.repo

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.nutrition.express.model.api.ApiClient
import com.nutrition.express.model.api.InProgress
import com.nutrition.express.model.api.Resource
import com.nutrition.express.model.api.callFromNet
import com.nutrition.express.model.api.service.TestService
import retrofit2.create
import kotlin.coroutines.CoroutineContext

class TestRepo(val context: CoroutineContext) {
    private val service: TestService = ApiClient.getRetrofit().create()

    fun getUserInfo(uid: String): LiveData<Resource<String>> {
        return liveData(context) {
            emit(InProgress)
            val result = callFromNet {
                service.getUserInfo()
            }
            when (result) {
                is Resource.Success -> emit(Resource.Success(uid))
                is Resource.Error -> emit(Resource.Error(result.code, result.message))
                is Resource.Loading -> {
                }
            }
        }
    }
}