package com.nutrition.express.model.api.repo

import androidx.lifecycle.LiveData
import com.nutrition.express.model.api.ApiClient
import com.nutrition.express.model.api.ApiResponse
import com.nutrition.express.model.api.Resource
import com.nutrition.express.model.api.service.TestService
import retrofit2.create
import kotlin.coroutines.CoroutineContext

class TestRepo(val context: CoroutineContext) {
    fun getUserInfo(uid: String): LiveData<Resource<String>> {
        return ApiClient.toLiveData(context) {
            val service: TestService = ApiClient.getRetrofit().create()
            val result = service.getUserInfo()
            ApiResponse(result.meta, uid)
        }
    }
}