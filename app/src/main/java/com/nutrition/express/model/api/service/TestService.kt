package com.nutrition.express.model.api.service

import com.nutrition.express.model.api.ApiResponse
import com.nutrition.express.model.api.bean.UserInfo
import retrofit2.http.GET

interface TestService {
    @GET("/v2/user/info")
    suspend fun getUserInfo(): ApiResponse<UserInfo>
}