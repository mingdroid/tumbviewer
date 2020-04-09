package com.nutrition.express.model.api

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nutrition.express.BuildConfig
import com.nutrition.express.model.data.AppData
import com.nutrition.express.model.data.NetErrorData
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import retrofit2.HttpException
import kotlin.coroutines.CoroutineContext

suspend fun <T: Any> callFromNet(apiFunc: suspend () -> ApiResponse<T>): Resource<T> {
    return try {
        val result = apiFunc.invoke()
        Resource.Success(result.response)
    } catch (error: HttpException) {
        if (BuildConfig.DEBUG) error.printStackTrace()
        handleError(error)
    } catch (throwable: Throwable) {
        if (BuildConfig.DEBUG) throwable.printStackTrace()
        Resource.Error(0, throwable.message ?: throwable.toString())
    }
}

private fun <T: Any> handleError(error: HttpException): Resource<T> {
    var code: Int? = null
    var errorMsg: String? = null
    try {
        val body = error.response()?.errorBody()?.string()
        val errorResponse: ApiResponse<Any>? = Gson().fromJson(body,
                object : TypeToken<ApiResponse<Any>?>() {}.type)
        code = errorResponse?.meta?.status
        errorMsg = errorResponse?.meta?.msg
    } catch (throwable: Throwable) {

    }
    when (error.code()) {
        401 -> {
            ApiClient.cancelAllCall()
            //todo
            //AppData.removeAccount(AppData.getPositiveAccount())
            if (AppData.switchToNextRoute()) {
                errorMsg = "Failed, touch to retry"
            } else {
                NetErrorData.setError401(true)
            }
        }
        429 -> {
            ApiClient.cancelAllCall()
            if (AppData.switchToNextRoute()) {
                errorMsg = "Failed, touch to retry"
            } else{
                NetErrorData.setError429(true)
            }
        }
    }
    return Resource.Error(code ?: error.code(), errorMsg ?: error.message())
}

fun spentCPU(context: CoroutineContext, millis: Long) = runBlocking(context) {
    val start = System.currentTimeMillis()
    var step = start
    while (isActive && System.currentTimeMillis() - start < millis) {
        if (System.currentTimeMillis() - step > 1000) {
            Log.d("spentCPU", (System.currentTimeMillis() - start).toString())
            step = System.currentTimeMillis()
        }
    }
}