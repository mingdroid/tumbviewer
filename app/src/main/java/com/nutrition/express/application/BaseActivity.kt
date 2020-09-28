package com.nutrition.express.application

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.nutrition.express.model.data.NetErrorData
import com.nutrition.express.ui.login.LoginActivity
import com.nutrition.express.ui.login.LoginType.ROUTE_SWITCH

fun Context.toast(message: CharSequence) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

fun Context.toast(@StringRes resId: Int) {
    Toast.makeText(this, resources.getText(resId), Toast.LENGTH_LONG).show()
}

open class BaseActivity : AppCompatActivity() {
    private val permissionMap: HashMap<String, MutableLiveData<Boolean>> = HashMap();

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        if (listenNetError()) {
            NetErrorData.apiError401.observe(this, Observer { aBoolean: Boolean ->
                if (aBoolean) {
                    NetErrorData.apiError401.removeObservers(this)
                    toast("Unauthorized, please login")
                    gotoLogin()
                }
            })
            NetErrorData.apiError429.observe(this, Observer { aBoolean: Boolean ->
                if (aBoolean) {
                    NetErrorData.apiError429.removeObservers(this)
                    toast("Unauthorized, please login")
                    gotoLogin()
                }
            })
        }
    }

    protected open fun listenNetError() = true

    private fun gotoLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra("type", ROUTE_SWITCH)
        startActivity(intent)
    }

    fun requestPermission(permission: String): LiveData<Boolean> {
        var liveData: MutableLiveData<Boolean>? = permissionMap[permission]
        if (liveData == null) {
            liveData = MutableLiveData();
            permissionMap[permission] = liveData
        }
        if (ContextCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            liveData.setValue(true)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(permission), 1);
        }
        return liveData
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            1 -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val liveData = permissionMap[permissions[0]]
                liveData?.value = true
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

}