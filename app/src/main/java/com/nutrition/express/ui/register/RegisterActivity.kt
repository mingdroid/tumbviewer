package com.nutrition.express.ui.register

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.nutrition.express.R
import com.nutrition.express.databinding.ActivityWebBinding
import com.nutrition.express.databinding.ItemRegisterTumblrBinding
import com.nutrition.express.model.data.AppData
import com.nutrition.express.ui.login.LoginActivity
import com.nutrition.express.ui.login.LoginType.NEW_ROUTE

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWebBinding
    private var key: String? = null
    private var secret: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolBar)
        title = "register"

        if (AppData.getAccountCount() > 1) {
            AppData.clearCookies()
        }

        binding.webView.settings.javaScriptEnabled = true
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

        binding.webView.loadUrl("https://www.tumblr.com/oauth/apps")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_register, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.tumblr_app -> showSettingAppDialog()
            R.id.home -> finish()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun showSettingAppDialog() {
        val binding = ItemRegisterTumblrBinding.inflate(LayoutInflater.from(this))
        binding.apiKey.setText(key)
        binding.apiSecret.setText(secret)
        AlertDialog.Builder(this).run {
            setTitle(R.string.register_tumblr_app)
            setPositiveButton(R.string.pic_save) { dialog, which ->
                if (!binding.apiKey.text.isNullOrEmpty() && !binding.apiSecret.text.isNullOrEmpty()) {
                    AppData.saveTumblrApp(
                        binding.apiKey.text.toString(),
                        binding.apiSecret.text.toString()
                    )
                    Toast.makeText(this@RegisterActivity, R.string.pic_saved, Toast.LENGTH_SHORT)
                        .show()
                    val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                    intent.putExtra("type", NEW_ROUTE)
                    startActivity(intent)
                    finish()
                }
            }
            setNeutralButton(R.string.register_continue_copy) { dialog, which ->
                key = binding.apiKey.text.toString()
                secret = binding.apiSecret.text.toString()
            }
            setNegativeButton(R.string.pic_cancel, null)
            setView(binding.root)
            show()
        }
    }
}
