package com.nutrition.express.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.nutrition.express.application.Constant
import com.nutrition.express.model.api.ApiClient
import com.nutrition.express.model.api.Resource
import com.nutrition.express.model.data.AppData
import com.nutrition.express.model.data.bean.TumblrApp
import com.nutrition.express.model.helper.OAuth1SigningHelper
import com.nutrition.express.ui.login.LoginType.NEW_ROUTE
import com.nutrition.express.ui.login.LoginType.NORMAL
import com.nutrition.express.ui.login.LoginType.ROUTE_SWITCH
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.util.*
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
        val result = MutableLiveData<Resource<OauthToken>>()
        result.value = Resource.loading(null)
        val auth = OAuth1SigningHelper(tumblrApp.apiKey, tumblrApp.apiSecret)
                .buildRequestHeader("POST", Constant.REQUEST_TOKEN_URL)
        val request = Request.Builder().run {
            url(Constant.REQUEST_TOKEN_URL)
            method("POST", "".toRequestBody("text/plain; charset=utf-8".toMediaType()))
            header("Authorization", auth)
            build()
        }
        val disposable = Observable.create<Response> {
                    val call = ApiClient.getOkHttpClient().newCall(request)
                    val response = call.execute()
                    it.onNext(response)
                    it.onComplete()
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    val responseBody = response.body
                    if (response.isSuccessful) {
                        val body = responseBody?.string()
                        val hashMap = convert(body)
                        val oauthToken = hashMap["oauth_token"]
                        val oauthTokenSecret = hashMap["oauth_token_secret"]
                        if (oauthToken != null && oauthTokenSecret != null) {
                            this@LoginViewModel.oauthToken = OauthToken(oauthToken, oauthTokenSecret)
                            result.value = Resource.success(this@LoginViewModel.oauthToken)
                        }
                    } else {
                        result.value = Resource.error(response.code, response.message, null)
                    }
                    responseBody?.close()
                }, { error ->
                    result.value = Resource.error(0, error.message ?: "IOException", null)
                })
        return result
    }

    private fun getAccessToken(oauthVerifier: String): LiveData<Resource<OauthToken>> {
        val loginResult = MutableLiveData<Resource<OauthToken>>()
        val tumblrApp = _tumblrApp.value ?: return loginResult
        val oauth = oauthToken ?: return loginResult

        loginResult.value = Resource.loading(null)
        val auth = OAuth1SigningHelper(tumblrApp.apiKey, tumblrApp.apiSecret)
                .buildAccessHeader("POST", Constant.ACCESS_TOKEN_URL,
                        oauth.token, oauthVerifier, oauth.secret)
        val request = Request.Builder()
                .url(Constant.ACCESS_TOKEN_URL)
                .method("POST", "".toRequestBody("text/plain; charset=utf-8".toMediaType()))
                .header("Authorization", auth)
                .build()
        val disposable = Observable.create<Response> {
                    val call: Call = ApiClient.getOkHttpClient().newCall(request)
                    val response = call.execute()
                    it.onNext(response)
                    it.onComplete()
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    val responseBody = response.body
                    if (response.isSuccessful) {
                        val body = responseBody?.string()
                        val hashMap = convert(body)
                        val oauthToken = hashMap["oauth_token"]
                        val oauthTokenSecret = hashMap["oauth_token_secret"]
                        if (oauthToken != null && oauthTokenSecret != null) {
                            val tumblrAccount = AppData.addAccount(
                                    tumblrApp.apiKey, tumblrApp.apiSecret, oauthToken, oauthTokenSecret)
                            if (type == NEW_ROUTE || type == ROUTE_SWITCH) {
                                AppData.switchToAccount(tumblrAccount)
                            }
                            loginResult.value = Resource.success(OauthToken(oauthToken, oauthTokenSecret))
                        }
                    } else {
                        loginResult.value = Resource.error(response.code, response.message, null)
                    }
                    responseBody?.close()
                }, { error ->
                    loginResult.value = Resource.error(0, error.message ?: "IOException", null)
                })
        return loginResult
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