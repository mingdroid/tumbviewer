package com.nutrition.express.model.api

import com.google.gson.GsonBuilder
import com.nutrition.express.BuildConfig
import com.nutrition.express.application.Constant
import com.nutrition.express.application.TumbApp
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

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

}