package com.nutrition.express.model.api

import com.nutrition.express.model.data.AppData
import com.nutrition.express.model.helper.OAuth1SigningHelper
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okio.Buffer
import java.util.*

class OAuth1Interceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val auth = request.header("Authorization")
        if (request.url.host == "api.tumblr.com" && AppData.isLogin() && auth.isNullOrEmpty()) {
            request = signRequest(request)
        }
        val response = chain.proceed(request)
        val headers = response.headers
        AppData.dayLimit = headers["X-RateLimit-PerDay-Limit"]?.toLongOrNull() ?: 0
        AppData.dayRemaining = headers["X-RateLimit-PerDay-Remaining"]?.toLongOrNull() ?: 0
        AppData.dayReset = headers["X-RateLimit-PerDay-Reset"]?.toLongOrNull() ?: 0
        AppData.hourLimit = headers["X-RateLimit-PerHour-Limit"]?.toLongOrNull() ?: 0
        AppData.hourRemaining = headers["X-RateLimit-PerHour-Remaining"]?.toLongOrNull() ?: 0
        AppData.hourReset = headers["X-RateLimit-PerHour-Reset"]?.toLongOrNull() ?: 0

        return response
    }

    private fun signRequest(request: Request): Request {
        val parameters: SortedMap<String, String?> = TreeMap()
        val url = request.url
        for (i in 0 until url.querySize) {
            parameters[url.queryParameterName(i)] = url.queryParameterValue(i)
        }
        val baseUrl = url.newBuilder().query(null).build().toString()

        val body = Buffer()

        val requestBody = request.body
        requestBody?.writeTo(body)

        while (!body.exhausted()) {
            val keyEnd = body.indexOf('='.toByte())
            check(keyEnd != -1L) { "Key with no value: " + body.readUtf8() }
            val key = body.readUtf8(keyEnd)
            body.skip(1) // Equals.
            val valueEnd = body.indexOf('&'.toByte())
            val value = if (valueEnd == -1L) body.readUtf8() else body.readUtf8(valueEnd)
            if (valueEnd != -1L) body.skip(1) // Ampersand.
            parameters[key] = value
        }
        val account = AppData.getPositiveAccount() ?: return request
        val auth = OAuth1SigningHelper(account.apiKey, account.apiSecret)
            .buildAuthHeader(request.method, baseUrl, account.token, account.secret, parameters)

        return request.newBuilder().header("Authorization", auth).build()
    }


}