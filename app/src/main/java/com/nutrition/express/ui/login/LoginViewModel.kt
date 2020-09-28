package com.nutrition.express.ui.login

import androidx.lifecycle.*
import com.nutrition.express.application.Constant
import com.nutrition.express.model.api.ApiClient
import com.nutrition.express.model.api.InProgress
import com.nutrition.express.model.api.Resource
import com.nutrition.express.model.data.AppData
import com.nutrition.express.model.data.bean.TumblrApp
import com.nutrition.express.model.helper.OAuth1SigningHelper
import com.nutrition.express.ui.login.LoginType.NEW_ROUTE
import com.nutrition.express.ui.login.LoginType.NORMAL
import com.nutrition.express.ui.login.LoginType.ROUTE_SWITCH
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.util.*
import kotlin.coroutines.coroutineContext
import kotlin.coroutines.resume
import kotlin.random.Random

class LoginViewModel : ViewModel() {
    private var _tumblrApp = MutableLiveData<TumblrApp>()
    private var _oauthVerifier = MutableLiveData<String>()
    val requestToken = _tumblrApp.switchMap {
        getRequestToken(it)
    }
    val accessToken = _oauthVerifier.switchMap {
        getAccessToken(it)
    }

    private var type = NORMAL
    private var oauthToken: OauthToken? = null


    fun setOauthVerifier(oauthVerifier: String) {
        _oauthVerifier.value = oauthVerifier
    }

    /*
     * init api key/secret
     */
    fun setType(type: Int) {
        this.type = type;
        var tumblrApp: TumblrApp? = null
        if (type == NEW_ROUTE) {
            tumblrApp = AppData.getTumblrApp()
        }
        if (tumblrApp == null) {
            tumblrApp = selectUnusedTumblrApp(AppData.getDefaultTumplrApps())
        }
        if (tumblrApp == null) {
            tumblrApp = TumblrApp(Constant.CONSUMER_KEY, Constant.CONSUMER_SECRET)
        }
        _tumblrApp.value = tumblrApp
    }

    /**
     * get tumblr app key for positive account, just route switching.
     *
     * @param map
     */
    private fun selectUnusedTumblrApp(map: HashMap<String, String>): TumblrApp? {
        val accounts = AppData.getTumblrAccounts()
        if (accounts.size < map.size) {
            for (account in accounts) {
                map.remove(account.apiKey)
            }
        } else {
            val positiveAccount = AppData.getPositiveAccount()
            positiveAccount?.let {
                if (positiveAccount.name.isNullOrEmpty()) {
                    map.remove(positiveAccount.apiKey)
                } else {
                    for (account in accounts) {
                        if (positiveAccount.name == account.name) {
                            map.remove(account.apiKey)
                        }
                    }
                }
            }
        }
        return if (map.size > 0) {
            val list: List<String> = ArrayList(map.keys)
            Random.nextInt()
            val randomIndex = Random.nextInt(list.size)
            val key = list[randomIndex]
            TumblrApp(key, map[key])
        } else {
            null
        }
    }

    private fun getRequestToken(tumblrApp: TumblrApp): LiveData<Resource<OauthToken>> {
        return liveData {
            emit(InProgress)
            val auth = OAuth1SigningHelper(tumblrApp.apiKey, tumblrApp.apiSecret)
                .buildRequestHeader("POST", Constant.REQUEST_TOKEN_URL)
            val request = Request.Builder().run {
                url(Constant.REQUEST_TOKEN_URL)
                method("POST", "".toRequestBody("text/plain; charset=utf-8".toMediaType()))
                header("Authorization", auth)
                build()
            }
            withContext(viewModelScope.coroutineContext + Dispatchers.IO) {
                val call = ApiClient.getOkHttpClient().newCall(request)
                suspendCancellableCoroutine<Result<Response>> { continuation ->
                    continuation.invokeOnCancellation {
                        call.cancel()
                    }
                    runCatching {
                        call.execute()
                    }.onSuccess {
                        continuation.resume(Result.success(it))
                    }.onFailure {
                        continuation.resume(Result.failure<Response>(it))
                    }
                }
            }.onSuccess { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    val hashMap = convert(body)
                    val oauthToken = hashMap["oauth_token"]
                    val oauthTokenSecret = hashMap["oauth_token_secret"]
                    if (oauthToken != null && oauthTokenSecret != null) {
                        this@LoginViewModel.oauthToken = OauthToken(oauthToken, oauthTokenSecret)
                        emit(Resource.Success(this@LoginViewModel.oauthToken))
                    } else {
                        emit(Resource.Error(0, "unknown error"))
                    }
                } else {
                    emit(Resource.Error(response.code, response.message))
                }
                response.close()
            }.onFailure {
                emit(Resource.Error(0, it.message ?: it.toString()))
            }
        }
    }

    private fun getAccessToken(oauthVerifier: String): LiveData<Resource<OauthToken>> {
        val loginResult = MutableLiveData<Resource<OauthToken>>()
        val tumblrApp = _tumblrApp.value ?: return loginResult
        val oauth = oauthToken ?: return loginResult

        loginResult.value = InProgress
        val auth = OAuth1SigningHelper(tumblrApp.apiKey, tumblrApp.apiSecret)
            .buildAccessHeader(
                "POST", Constant.ACCESS_TOKEN_URL,
                oauth.token, oauthVerifier, oauth.secret
            )
        val request = Request.Builder()
            .url(Constant.ACCESS_TOKEN_URL)
            .method("POST", "".toRequestBody("text/plain; charset=utf-8".toMediaType()))
            .header("Authorization", auth)
            .build()
        return liveData(viewModelScope.coroutineContext) {
            emit(InProgress)
            withContext(coroutineContext + Dispatchers.IO) {
                val call = ApiClient.getOkHttpClient().newCall(request)
                suspendCancellableCoroutine<Result<Response>> { continuation ->
                    continuation.invokeOnCancellation {
                        call.cancel()
                    }
                    runCatching {
                        call.execute()
                    }.onSuccess {
                        continuation.resume(Result.success(it))
                    }.onFailure {
                        continuation.resume(Result.failure<Response>(it))
                    }
                }
            }.onSuccess { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    val hashMap = convert(body)
                    val oauthToken = hashMap["oauth_token"]
                    val oauthTokenSecret = hashMap["oauth_token_secret"]
                    if (oauthToken != null && oauthTokenSecret != null) {
                        val tumblrAccount = AppData.addAccount(
                            tumblrApp.apiKey, tumblrApp.apiSecret, oauthToken, oauthTokenSecret
                        )
                        if (type == NEW_ROUTE || type == ROUTE_SWITCH) {
                            AppData.switchToAccount(tumblrAccount)
                        }
                        emit(Resource.Success(OauthToken(oauthToken, oauthTokenSecret)))
                    } else {
                        emit(Resource.Error(0, "unknown error"))
                    }
                } else {
                    emit(Resource.Error(response.code, response.message))
                }
                response.close()
            }.onFailure {
                emit(Resource.Error(0, it.message ?: it.toString()))
            }
        }
    }

    private fun convert(body: String?): HashMap<String, String> {
        val hashMap = HashMap<String, String>()
        body?.let {
            val strings = body.split("&").toTypedArray()
            for (string in strings) {
                val pair = string.split("=").toTypedArray()
                if (pair.size == 2) {
                    hashMap[pair[0]] = pair[1]
                }
            }
        }
        return hashMap
    }

    data class OauthToken(val token: String, val secret: String)
}