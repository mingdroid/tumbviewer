package com.nutrition.express.model.api.repo

import androidx.lifecycle.LiveData
import com.nutrition.express.model.api.ApiClient
import com.nutrition.express.model.api.Resource
import com.nutrition.express.model.api.service.ReblogService
import retrofit2.create
import kotlin.coroutines.CoroutineContext

class ReblogRepo(val context: CoroutineContext) {
    private val service: ReblogService = ApiClient.getRetrofit().create()

    fun reblogPost(id: String, map: Map<String, String>): LiveData<Resource<Any>> {
        return ApiClient.toLiveData(context) {
            service.reblogPost(id, map)
        }
    }

}