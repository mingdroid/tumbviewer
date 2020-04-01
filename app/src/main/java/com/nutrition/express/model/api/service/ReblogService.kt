package com.nutrition.express.model.api.service

import com.nutrition.express.model.api.ApiResponse
import io.reactivex.Observable
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Path

interface ReblogService {
    @FormUrlEncoded
    @POST("/v2/blog/{id}/post/reblog")
    suspend fun reblogPost(@Path("id") id: String,
                           @FieldMap map: Map<String, String>): ApiResponse<Any>
}