package com.nutrition.express.model.api.service

import com.nutrition.express.model.api.ApiResponse
import com.nutrition.express.model.api.bean.BlogInfo
import com.nutrition.express.model.api.bean.BlogLikes
import com.nutrition.express.model.api.bean.BlogPosts
import retrofit2.http.*

interface BlogService {
    @GET("/v2/blog/{id}/info")
    suspend fun getBlogInfo(
        @Path("id") id: String,
        @Query("api_key") key: String
    ): ApiResponse<BlogInfo>

    @GET("/v2/blog/{id}/likes")
    suspend fun getBlogLikes(
        @Path("id") id: String,
        @QueryMap map: HashMap<String, String>
    ): ApiResponse<BlogLikes>

    @GET("/v2/blog/{id}/posts/{type}")
    suspend fun getBlogPosts(
        @Path("id") id: String,
        @Path("type") type: String,
        @QueryMap map: HashMap<String, String>
    ): ApiResponse<BlogPosts>

    @FormUrlEncoded
    @POST("/v2/blog/{id}/post/delete")
    suspend fun deletePost(@Path("id") id: String, @Field("id") postId: String): ApiResponse<Any>

}