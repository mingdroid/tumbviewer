package com.nutrition.express.ui.login

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.nutrition.express.R
import com.nutrition.express.application.BaseActivity
import com.nutrition.express.application.Constant
import com.nutrition.express.application.toast
import com.nutrition.express.databinding.ActivityWebBinding
import com.nutrition.express.model.api.Status
import com.nutrition.express.model.data.AppData
import com.nutrition.express.ui.login.LoginType.NEW_ACCOUNT
import com.nutrition.express.ui.login.LoginType.NEW_ROUTE
import com.nutrition.express.ui.login.LoginType.NORMAL
import com.nutrition.express.ui.login.LoginType.ROUTE_SWITCH
import com.nutrition.express.ui.main.MainActivity

class LoginActivity : BaseActivity() {
    private lateinit var binding: ActivityWebBinding
    private val loginViewModel: LoginViewModel by viewModels()

    private var type = NORMAL //login type;

    private var progressDialog: ProgressDialog? = null

    override fun listenNetError() = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        type = intent.getIntExtra("type", NORMAL)

        if (AppData.isLogin() && type == NORMAL) {
            gotoMainActivity()
            return
        }

        if (type == NEW_ROUTE || type == ROUTE_SWITCH) {
            //if there are two different accounts, then clear cookies;
            if (AppData.getAccountCount() > 1) {
                AppData.clearCookies()
            }
        } else if (type == NEW_ACCOUNT) {
            AppData.clearCookies()
        }
        window.setBackgroundDrawableResource(android.R.color.white)
        binding = ActivityWebBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolBar)
        title = "login"
        if (type == NEW_ACCOUNT) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                if (request.url.toString().startsWith(Constant.REDIRECT_URI)) {
                    val oauthVerifier = request.url.getQueryParameter("oauth_verifier")
                    if (!oauthVerifier.isNullOrEmpty()) {
                        loginViewModel.setOauthVerifier(oauthVerifier)
                        return true
                    }
                    return false
                }
                return super.shouldOverrideUrlLoading(view, request)
            }

            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                if (url.startsWith(Constant.REDIRECT_URI)) {
                    val uri = Uri.parse(url)
                    val oauthVerifier = uri.getQueryParameter("oauth_verifier")
                    if (!oauthVerifier.isNullOrEmpty()) {
                        loginViewModel.setOauthVerifier(oauthVerifier)
                        return true
                    }
                    return false
                }
                return super.shouldOverrideUrlLoading(view, url)
            }
        }
        binding.webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                if (newProgress == 100) {
                    binding.progressBar.visibility = View.GONE
                } else {
                    binding.progressBar.progress = newProgress
                }
            }
        }
        loginViewModel.requestToken.observe(this, Observer {
            when (it.status) {
                Status.LOADING -> showProgress()
                Status.ERROR -> {
                    dismissProgress()
                    it.message?.let { msg -> toast(msg) }
                }
                Status.SUCCESS -> {
                    dismissProgress()
                    it.data?.let { data ->
                        binding.webView.loadUrl(Constant.AUTHORIZE_URL + "?oauth_token=" + data.token)
                    }
                }
            }
        })
        loginViewModel.accessToken.observe(this, Observer {
            when (it.status) {
                Status.LOADING -> showProgress()
                Status.ERROR -> {
                    dismissProgress()
                    it.message?.let { msg -> toast(msg) }
                }
                Status.SUCCESS -> {
                    dismissProgress()
                    toast(R.string.login_success)
                    if (type == NEW_ACCOUNT) {
                        setResult(Activity.RESULT_OK)
                        finish()
                    } else {
                        gotoMainActivity()
                    }
                }
            }
        })
        loginViewModel.setType(type)
    }

    override fun onStop() {
        super.onStop()
        dismissProgress()
    }

    private fun showProgress() {
        if (progressDialog != null) {
            progressDialog = ProgressDialog.show(this, null, null)
        }
    }

    private fun dismissProgress() {
        progressDialog?.dismiss()
        progressDialog = null
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun gotoMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

}
