package com.nutrition.express.util

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.util.TypedValue
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.nutrition.express.BuildConfig
import com.nutrition.express.R
import com.nutrition.express.application.BaseActivity
import com.nutrition.express.application.TumbApp
import com.nutrition.express.application.toast
import okio.buffer
import okio.sink
import okio.source
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.lang.reflect.Type
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

fun dp2Pixels(context: Context, dp: Int): Int {
    val pixels = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(),
        context.resources.displayMetrics
    )
    return (pixels + 0.5f).toInt()
}

fun copy2Clipboard(context: Context, text: String) {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clipData = ClipData.newPlainText("Tumblr", text)
    clipboardManager.setPrimaryClip(clipData)
    context.toast(R.string.copy_to_clipboard)
}

fun canWrite2Storage(context: Context): Boolean {
    if (ContextCompat.checkSelfPermission(
            TumbApp.app,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        if (context is BaseActivity) {
            context.requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        return false
    }
    return true
}

fun store(file: File, content: ByteArray): Boolean {
    if (content.isEmpty()) return false
    return try {
        val input = ByteArrayInputStream(content)
        val source = input.source()
        val sink = file.sink()
        val bufferedSink = sink.buffer()
        bufferedSink.writeAll(source)
        source.close()
        bufferedSink.close()
        true
    } catch (e: IOException) {
        false
    }
}

fun read(file: File): ByteArray? {
    if (!file.exists()) return null
    return try {
        val source = file.source()
        val bytes = ByteArray(file.length().toInt())
        val bufferedSource = source.buffer()
        bufferedSource.read(bytes)
        bufferedSource.close()
        bytes
    } catch (e: IOException) {
        null
    }
}

fun store(name: String, any: Any?) {
    val file = File(TumbApp.app.filesDir, name)
    if (any == null) {
        file.delete()
        return
    }
    val gson = Gson()
    val content = gson.toJson(any)
    store(file, content.toByteArray())
}

fun <T> read(name: String, typeOfT: Type): T? {
    val file = File(TumbApp.app.filesDir, name)
    val content = read(file) ?: return null
    val string = String(content)
    if (BuildConfig.DEBUG) {
        Log.d("TAG", "getShortContent: $string")
    }
    return try {
        val gson = Gson()
        gson.fromJson(string, typeOfT)
    } catch (e: JsonParseException) {
        e.printStackTrace()
        null
    }
}

fun md5sum(input: String): String {
    try {
        val mdEnc = MessageDigest.getInstance("MD5")
        mdEnc.update(input.toByteArray(), 0, input.length)
        var md5 = BigInteger(1, mdEnc.digest()).toString(16)
        while (md5.length < 32) {
            md5 = "0$md5"
        }
        return md5
    } catch (e: NoSuchAlgorithmException) {
        println("Exception while encrypting to md5")
        e.printStackTrace()
    }
    return input
}


