package com.nutrition.express.model.api.service

import com.nutrition.express.model.api.ApiResponse
import com.nutrition.express.model.api.bean.BlogLikes
import com.nutrition.express.model.api.bean.BlogPosts
import com.nutrition.express.model.api.bean.FollowingBlog
import com.nutrition.express.model.api.bean.UserInfo
import retrofit2.http.*

interface UserService {
    @GET("/v2/user/following")
    suspend fun getFollowing(@Query("limit") limit: Int,
                             @Query("offset") offset: Int): ApiResponse<FollowingBlog>

    @GET("/v2/user/likes")
    suspend fun getLikes(@Query("limit") limit: Int,
                         @Query("before") before: Long): ApiResponse<BlogLikes>

    @GET("/v2/user/info")
    suspend fun getInfo(): ApiResponse<UserInfo>

    @GET("/v2/user/dashboard")
    suspend fun getDashboard(@QueryMap options: Map<String, String>): ApiResponse<BlogPosts>

    @FormUrlEncoded
    @POST("/v2/user/follow")
    suspend fun follow(@Field("url") url: String): ApiResponse<Any>

    @FormUrlEncoded
    @POST("/v2/user/unfollow")
    suspend fun unfollow(@Field("url") url: String): ApiResponse<Any>

    @FormUrlEncoded
    @POST("/v2/user/like")
    suspend fun like(@Field("id") id: Long, @Field("reblog_key") key: String): ApiResponse<Array<Any>>

    @FormUrlEncoded
    @POST("/v2/user/unlike")
    suspend fun unlike(@Field("id") id: Long, @Field("reblog_key") key: String): ApiResponse<Array<Any>>
}