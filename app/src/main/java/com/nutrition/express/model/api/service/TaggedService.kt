package com.nutrition.express.model.api.service

import com.nutrition.express.model.api.ApiResponse
import com.nutrition.express.model.api.bean.PostsItem
import retrofit2.http.GET
import retrofit2.http.Query

interface TaggedService {
    @GET("/v2/tagged")
    suspend fun getTaggedPosts(@Query("tag") tag: String,
                               @Query("filter") filter: String?,
                               @Query("before") timestamp: Long?,
                               @Query("limit") limit: Int): ApiResponse<List<PostsItem>>
}