package com.nutrition.express.ui.settings

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.text.format.DateUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.facebook.drawee.backends.pipeline.Fresco
import com.nutrition.express.R
import com.nutrition.express.application.BaseActivity
import com.nutrition.express.application.toast
import com.nutrition.express.common.CommonRVAdapter
import com.nutrition.express.common.CommonViewHolder
import com.nutrition.express.databinding.ActivitySettingsBinding
import com.nutrition.express.databinding.ItemSettingsAccountBinding
import com.nutrition.express.model.data.AppData
import com.nutrition.express.model.data.AppData.POST_SIMPLE_MODE
import com.nutrition.express.model.data.bean.TumblrAccount
import com.nutrition.express.ui.login.LoginActivity
import com.nutrition.express.ui.login.LoginType.NEW_ACCOUNT
import com.nutrition.express.ui.register.RegisterActivity
import com.nutrition.express.util.getBoolean
import com.nutrition.express.util.putBoolean
import com.nutrition.express.util.setTumblrAvatarUri

class SettingsActivity : BaseActivity() {
    private val REQUEST_LOGIN = 1
    private lateinit var binding: ActivitySettingsBinding


    private lateinit var adapter: CommonRVAdapter
    private var accounts = ArrayList<Any>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val mode = getBoolean(POST_SIMPLE_MODE, false)
        binding.settingsOptionSimpleCheckbox.isChecked = mode
        binding.settingsOptionSimpleCheckbox.setOnCheckedChangeListener { _, isChecked ->
            putBoolean(POST_SIMPLE_MODE, isChecked)
            AppData.modeData.value = isChecked
        }

        binding.settingsRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        binding.settingsClearCache.setOnClickListener {
            showClearCacheDialog()
        }
        binding.settingsAddAccount.setOnClickListener {
            val loginIntent = Intent(this, LoginActivity::class.java)
            loginIntent.putExtra("type", NEW_ACCOUNT)
            startActivityForResult(loginIntent, REQUEST_LOGIN)
        }
        binding.settingsOptionSimple.setOnClickListener {
            binding.settingsOptionSimpleCheckbox.isChecked =
                !binding.settingsOptionSimpleCheckbox.isChecked
        }

        val tumblrAccounts = AppData.getTumblrAccounts()
        accounts.clear()
        accounts.addAll(tumblrAccounts)

        adapter = CommonRVAdapter.adapter {
            addViewType(TumblrAccount::class, R.layout.item_settings_account) { AccountVH(it) }
            data = accounts
        }

        binding.settingsAccounts.isNestedScrollingEnabled = false
        binding.settingsAccounts.layoutManager = LinearLayoutManager(this)
        binding.settingsAccounts.adapter = adapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_LOGIN && resultCode == Activity.RESULT_OK) {
            updateAccountsContent()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_settings, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settings_tumblr_limit -> {
                showTumblrLimitInfo()
            }
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showClearCacheDialog() {
        AlertDialog.Builder(this).run {
            setPositiveButton(R.string.settings_clear_cache) { _, _ ->
                Fresco.getImagePipeline().clearDiskCaches()
                toast(R.string.settings_clear_ok)
            }
            setNegativeButton(R.string.pic_cancel, null)
            setTitle(R.string.settings_clear_cache)
            show()
        }
    }

    private fun showTumblrLimitInfo() {
        AlertDialog.Builder(this).run {
            setMessage(
                getString(
                    R.string.settings_limit_info,
                    AppData.dayLimit,
                    AppData.dayRemaining,
                    DateUtils.formatElapsedTime(AppData.dayReset),
                    AppData.hourLimit,
                    AppData.hourRemaining,
                    DateUtils.formatElapsedTime(AppData.hourReset)
                )
            )
            show()
        }
    }

    private fun updateAccountsContent() {
        accounts.clear()
        accounts.addAll(AppData.getTumblrAccounts())
        adapter.notifyDataSetChanged()
    }

    private fun showDeleteAccountDialog(account: TumblrAccount, accountName: String) {
        AlertDialog.Builder(this).run {
            setPositiveButton(R.string.delete_positive) { _, _ ->
                AppData.removeAccount(account)
                updateAccountsContent()
                if (!AppData.isLogin()) {
                    gotoLogin()
                }
            }
            setNegativeButton(R.string.pic_cancel, null)
            setTitle(resources.getString(R.string.settings_delete_account, accountName))
            show()
        }
    }

    private fun gotoLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun switchToAccount(account: TumblrAccount) {
        AppData.switchToAccount(account)
        finish()
    }

    private fun showSwitchDialog(account: TumblrAccount, accountName: String) {
        AlertDialog.Builder(this).run {
            setPositiveButton(R.string.settings_switch) { _, _ ->
                switchToAccount(account)
            }
            setNegativeButton(R.string.pic_cancel, null)
            setTitle(resources.getString(R.string.settings_accounts_switch, accountName))
            show()
        }
    }

    private fun showRoute() {

    }

    private inner class AccountVH(view: View) : CommonViewHolder<TumblrAccount>(view) {
        private val binding = ItemSettingsAccountBinding.bind(itemView)
        private lateinit var account: TumblrAccount

        init {
            itemView.setOnClickListener {
                if (account.isUsing) {
                    showRoute()
                } else {
                    showSwitchDialog(account, binding.accountName.text.toString())
                }
            }
            itemView.setOnLongClickListener {
                showDeleteAccountDialog(account, binding.accountName.text.toString())
                return@setOnLongClickListener true
            }
        }

        override fun bindView(any: TumblrAccount) {
            this.account = any
            if (any.isUsing) {
                binding.accountChecked.visibility = View.VISIBLE
            } else {
                binding.accountChecked.visibility = View.GONE
            }
            if (!TextUtils.isEmpty(any.name)) {
                binding.accountName.text = any.name
                setTumblrAvatarUri(binding.accountAvatar, any.name, 128)
            } else {
                binding.accountName.text = resources.getString(
                    R.string.settings_accounts_title,
                    adapterPosition + 1
                )
                binding.accountAvatar.setActualImageResource(R.mipmap.ic_account_default)
            }
            binding.accountKey.text = any.apiKey
        }
    }

}
