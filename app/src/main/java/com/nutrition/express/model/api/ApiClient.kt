package com.nutrition.express.model.api

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.nutrition.express.BuildConfig
import com.nutrition.express.application.Constant
import com.nutrition.express.application.TumbApp
import com.nutrition.express.model.data.AppData
import com.nutrition.express.model.data.NetErrorData
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.coroutines.CoroutineContext

/*
 * okhttp + retrofit
 */
object ApiClient {
    private var okHttpClient: OkHttpClient
    private var retrofit: Retrofit

    init {
        val logger = HttpLoggingInterceptor()
        if (BuildConfig.DEBUG) {
            logger.level = HttpLoggingInterceptor.Level.BODY
        } else {
            logger.level = HttpLoggingInterceptor.Level.NONE
        }
        val cache = Cache(TumbApp.app.cacheDir, 10 * 1024 * 1024)
        okHttpClient = OkHttpClient.Builder().run {
            addInterceptor(OAuth1Interceptor())
            addInterceptor(logger)
            cache(cache)
            build()
        }
        val gson = GsonBuilder()
                .setLenient()
                .create()
        retrofit = Retrofit.Builder().run {
            baseUrl(Constant.BASE_URL)
            addConverterFactory(GsonConverterFactory.create(gson))
            addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            client(okHttpClient)
            build()
        }
    }

    fun cancelAllCall() {
        okHttpClient.dispatcher.cancelAll()
    }

    fun getRetrofit(): Retrofit {
        return retrofit
    }

    fun getOkHttpClient(): OkHttpClient {
        return okHttpClient
    }

    fun <T: Any> toLiveData(context: CoroutineContext, func: suspend () -> ApiResponse<T>): LiveData<Resource<T>> {
        return liveData(context) {
            emit(Resource.loading(null))
            try {
                val result = func.invoke()
                emit(Resource.success(result.response))
            } catch (error: HttpException) {
                emit(handleError<T>(error))
            } catch (throwable: Throwable) {
                throwable.printStackTrace()
                Log.d("ApiClient", "toLiveData: ->${throwable.message}")
                Log.d("ApiClient", "toLiveData: ${System.currentTimeMillis()}")
                emit(Resource.error(0, throwable.message ?: throwable.toString(), null))
            }
        }
    }

    suspend fun <T: Any> wrap(func: suspend () -> ApiResponse<T>): Resource<T> {
        try {
            val result = func.invoke()
            return Resource.success(result.response)
        } catch (error: HttpException) {
            return handleError(error)
        } catch (ex: Throwable) {
            return Resource.error(0, ex.message ?: ex.toString(), null)
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
                cancelAllCall()
                //todo
                //AppData.removeAccount(AppData.getPositiveAccount())
                if (AppData.switchToNextRoute()) {
                    errorMsg = "Failed, touch to retry"
                } else {
                    NetErrorData.setError401(true)
                }
            }
            429 -> {
                cancelAllCall()
                if (AppData.switchToNextRoute()) {
                    errorMsg = "Failed, touch to retry"
                } else{
                    NetErrorData.setError429(true)
                }
            }
        }
        return Resource.error(code ?: error.code(), errorMsg ?: error.message(), null)
    }
}