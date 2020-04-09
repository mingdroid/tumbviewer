package com.nutrition.express.model.api.repo

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.nutrition.express.model.api.*
import com.nutrition.express.model.api.service.TestService
import retrofit2.create
import kotlin.coroutines.CoroutineContext

class TestRepo(val context: CoroutineContext) {
    private val service: TestService = ApiClient.getRetrofit().create()

    fun getUserInfo(uid: String): LiveData<Resource<String>> {
        return liveData(context) {
            emit(Resource.loading(null))
            val result = callFromNet {
                service.getUserInfo()
            }
            if (result.status == Status.SUCCESS) {
                emit(Resource.success(uid))
            } else {
                emit(Resource.error(result.code, result.message, null))
            }
        }
    }
}