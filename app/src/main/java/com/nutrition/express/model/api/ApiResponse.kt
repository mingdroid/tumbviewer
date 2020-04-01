package com.nutrition.express.model.api


data class ApiResponse<T>(val meta: Meta, val response: T?) {
    data class Meta(val status: Int, val msg: String)
}