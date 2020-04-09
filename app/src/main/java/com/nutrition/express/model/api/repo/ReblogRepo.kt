package com.nutrition.express.model.api.repo

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.nutrition.express.model.api.ApiClient
import com.nutrition.express.model.api.InProgress
import com.nutrition.express.model.api.Resource
import com.nutrition.express.model.api.callFromNet
import com.nutrition.express.model.api.service.ReblogService
import retrofit2.create
import kotlin.coroutines.CoroutineContext

class ReblogRepo(val context: CoroutineContext) {
    private val service: ReblogService = ApiClient.getRetrofit().create()

    fun reblogPost(id: String, map: Map<String, String>): LiveData<Resource<Any>> {
        return liveData(context) {
            emit(InProgress)
            emit(callFromNet {
                service.reblogPost(id, map)
            })
        }
    }

}