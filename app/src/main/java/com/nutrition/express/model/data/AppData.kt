package com.nutrition.express.model.data

import android.webkit.CookieManager
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nutrition.express.application.Constant
import com.nutrition.express.model.api.bean.UserInfoItem
import com.nutrition.express.model.data.bean.TumblrAccount
import com.nutrition.express.model.data.bean.TumblrApp
import com.nutrition.express.model.helper.LocalPersistenceHelper
import com.nutrition.express.util.getBoolean

object AppData {
    private const val TUMBLR_APP = "tumblr_app"
    private const val TUMBLR_ACCOUNT = "tumblr_account"
    const val POST_SIMPLE_MODE = "post_simple_mode"

    private val tumblrAccountList: MutableList<TumblrAccount> = ArrayList()
    private var positiveAccount: TumblrAccount? = null
    private var referenceBlog: MutableList<String> = ArrayList()
    private var referenceBlogSet: HashSet<String> = HashSet()
    private var followingSet: HashSet<String> = HashSet()

    var users: UserInfoItem? = null
    var photoIndex: Int = 0
    var dayLimit: Long = 0
    var dayRemaining: Long = 0
    var dayReset: Long = 0
    var hourLimit: Long = 0
    var hourRemaining: Long = 0
    var hourReset: Long = 0
    val modeData = MutableLiveData<Boolean>()

    init {
        val list: List<TumblrAccount>? = LocalPersistenceHelper.getShortContent(
            TUMBLR_ACCOUNT,
            object : TypeToken<ArrayList<TumblrAccount>>() {}.type
        )
        if (list != null) {
            tumblrAccountList.addAll(list)
        }
        ensurePositiveAccount()

        modeData.postValue(getBoolean(POST_SIMPLE_MODE, false))
    }

    private fun ensurePositiveAccount() {
        for (account in tumblrAccountList) {
            if (account.isUsing) {
                positiveAccount = account
                break
            }
        }
        if (positiveAccount == null && tumblrAccountList.isNotEmpty()) {
            positiveAccount = tumblrAccountList[0]
            positiveAccount?.isUsing = true
            LocalPersistenceHelper.storeShortContent(TUMBLR_ACCOUNT, tumblrAccountList)
        }
    }

    fun getPositiveAccount(): TumblrAccount? {
        return positiveAccount
    }

    fun getTumblrAccounts(): List<TumblrAccount> {
        return tumblrAccountList
    }

    fun addAccount(
        apiKey: String,
        apiSecret: String,
        token: String,
        secret: String
    ): TumblrAccount {
        val account = TumblrAccount(apiKey, apiSecret, token, secret)
        if (positiveAccount == null) {
            account.isUsing = true
            positiveAccount = account
        }
        tumblrAccountList.add(account)
        LocalPersistenceHelper.storeShortContent(TUMBLR_ACCOUNT, tumblrAccountList)
        return account
    }

    fun removeAccount(account: TumblrAccount?) {
        tumblrAccountList.remove(account)
        if (positiveAccount == account) {
            positiveAccount = null
            ensurePositiveAccount()
        }
        LocalPersistenceHelper.storeShortContent(TUMBLR_ACCOUNT, tumblrAccountList)
    }

    fun switchToAccount(account: TumblrAccount) {
        for (item in tumblrAccountList) {
            if (item.token == account.token) {
                positiveAccount?.isUsing = false
                item.isUsing = true
                positiveAccount = item
                break
            }
        }
        LocalPersistenceHelper.storeShortContent(TUMBLR_ACCOUNT, tumblrAccountList)
        clearReferenceBlog()
    }

    fun switchToNextRoute(): Boolean {
        if (positiveAccount?.name == null) {
            return false
        }
        val list: ArrayList<TumblrAccount> = ArrayList()
        tumblrAccountList.filterTo(list) { it.name == positiveAccount?.name }
        positiveAccount?.isLimitExceeded = true
        for (item in list) {
            if (item != positiveAccount && !item.isLimitExceeded) {
                switchToAccount(item)
                return true
            }
        }
        return false
    }

    fun getAccountCount(): Int {
        val hashSet: HashSet<String> = HashSet()
        tumblrAccountList.mapTo(hashSet) { it.name }
        return hashSet.size
    }

    fun clearCookies() {
        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()
    }


    fun getTumblrApp(): TumblrApp? {
        val tumblrAppList = LocalPersistenceHelper.getShortContent<ArrayList<TumblrApp>>(
            TUMBLR_APP,
            object : TypeToken<ArrayList<TumblrApp>>() {}.type
        )
        if (!tumblrAppList.isNullOrEmpty()) {
            return tumblrAppList[0]
        }
        return null
    }

    fun saveTumblrApp(key: String, secret: String) {
        var tumblrAppList = LocalPersistenceHelper.getShortContent<ArrayList<TumblrApp>>(
            TUMBLR_APP,
            object : TypeToken<ArrayList<TumblrApp>>() {}.type
        )
        if (tumblrAppList == null) {
            tumblrAppList = ArrayList()
        }
        tumblrAppList.add(TumblrApp(key, secret))
        LocalPersistenceHelper.storeShortContent(TUMBLR_APP, tumblrAppList)
    }

    fun getDefaultTumplrApps(): HashMap<String, String> {
        return Gson().fromJson(
            Constant.API_KEYS,
            object : TypeToken<HashMap<String, String>>() {}.type
        )
    }

    fun getReferenceBlog(): List<String> {
        return referenceBlog
    }

    fun addReferenceBlog(blog: String) {
        if (!followingSet.contains(blog)) {
            if (referenceBlogSet.add(blog)) {
                referenceBlog.add(blog)
            }
        }
    }

    fun addFollowingBlog(blog: String?) {
        followingSet.add(blog!!)
    }

    private fun clearReferenceBlog() {
        referenceBlog.clear()
        referenceBlogSet.clear()
        followingSet.clear()
    }

    fun isLogin(): Boolean {
        return positiveAccount != null
    }
}