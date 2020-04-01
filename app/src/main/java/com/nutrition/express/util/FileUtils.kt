package com.nutrition.express.util

import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import com.nutrition.express.application.TumbApp.Companion.app
import com.nutrition.express.model.data.AppData
import com.nutrition.express.model.data.DataManager
import java.io.File

/**
 * Created by huang on 11/6/16.
 */
object FileUtils {
    private val tumblrRootDir: File
        get() {
            val tumblrDir = File(app.getExternalFilesDir(null), "tumblr")
            return if (tumblrDir.exists()) {
                tumblrDir
            } else {
                File(app.getExternalFilesDir(null), "Tumblr")
            }
        }

    val videoDir: File
        get() {
            val accountName = AppData.getPositiveAccount()?.name
            return if (accountName.isNullOrEmpty()) {
                publicVideoDir
            } else {
                File(tumblrRootDir, "video/$accountName")
            }
        }

    val imageDir: File
        get() {
            val accountName = AppData.getPositiveAccount()?.name
            return if (accountName.isNullOrEmpty()) {
                publicImageDir
            } else {
                File(tumblrRootDir, "image/$accountName")
            }
        }

    val publicVideoDir: File
        get() = File(tumblrRootDir, "video")

    val publicImageDir: File
        get() = File(tumblrRootDir, "image")

    fun imageSaved(uri: Uri): Boolean {
        var name = uri.lastPathSegment
        if (null == name) {
            name = uri.toString()
        }
        return File(imageDir, name).isFile
    }

    fun createImageFile(uri: Uri): File {
        val dir = imageDir
        if (!dir.exists()) {
            dir.mkdirs()
        }
        var name = uri.lastPathSegment
        if (null == name) {
            name = uri.toString()
        }
        return File(imageDir, name)
    }

    fun videoSaved(uri: Uri): Boolean {
        var name = uri.lastPathSegment
        if (null == name) {
            name = uri.toString()
        }
        return File(videoDir, name).isFile
    }

    fun createVideoFile(uri: Uri): File {
        val dir = videoDir
        if (!dir.exists()) {
            dir.mkdirs()
        }
        var name = uri.lastPathSegment
        if (null == name) {
            name = uri.toString()
        }
        return File(videoDir, name)
    }

    fun createVideoFile(url: String): File {
        val dir = videoDir
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val name: String
        val index = url.lastIndexOf("/")
        name = if (index > 0) {
            url.substring(url.lastIndexOf("/"))
        } else {
            url
        }
        return File(dir, name)
    }

    fun deleteFile(file: File) {
        if (file.isDirectory) {
            deleteDirFile(file)
        } else {
            if (file.delete()) {
                scanMedia(file)
            }
        }
    }

    private fun deleteDirFile(dir: File) {
        val files = dir.listFiles()
        files?.let {
            for (file in it) {
                if (file.isDirectory) {
                    deleteDirFile(file)
                } else {
                    if (file.delete()) {
                        scanMedia(file)
                    }
                }
            }
        }
        dir.delete()
    }

    private fun scanMedia(f: File) {
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val contentUri = Uri.fromFile(f)
        mediaScanIntent.data = contentUri
        app.sendBroadcast(mediaScanIntent)
    }
}