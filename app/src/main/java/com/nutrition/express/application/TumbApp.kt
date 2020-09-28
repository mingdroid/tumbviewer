package com.nutrition.express.application

import android.app.ActivityManager
import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import com.facebook.cache.disk.DiskCacheConfig
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.core.ImagePipelineConfig
import com.nutrition.express.BuildConfig

class TumbApp : Application() {
    lateinit var imagePipelineConfig: ImagePipelineConfig
        private set
    var width = 0
        private set
    var height = 0
        private set

    companion object {
        lateinit var app: TumbApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        app = this
        val cacheConfig = DiskCacheConfig.newBuilder(this)
            .setMaxCacheSize(300 * 1024 * 1024.toLong())
            .build()
        val smallCacheConfig = DiskCacheConfig.newBuilder(this)
            .setMaxCacheSize(10 * 1024 * 1024.toLong())
            .build()
        imagePipelineConfig = ImagePipelineConfig.newBuilder(this)
            .setMainDiskCacheConfig(cacheConfig)
            .setSmallImageDiskCacheConfig(smallCacheConfig)
            .setDownsampleEnabled(true) //                .setResizeAndRotateEnabledForNetwork(true)
            .setBitmapsConfig(Bitmap.Config.RGB_565)
            .build()
        Fresco.initialize(this, imagePipelineConfig)

        //init width and height
        val dm = DisplayMetrics()
        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.defaultDisplay.getMetrics(dm)
        width = dm.widthPixels
        height = dm.heightPixels

        //information
        if (BuildConfig.DEBUG) {
            val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            Log.d(ContentValues.TAG, "onCreate: " + am.memoryClass)
            Log.d(ContentValues.TAG, "onCreate: " + am.largeMemoryClass)
            Log.d(ContentValues.TAG, "onCreate: " + Runtime.getRuntime().maxMemory())
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectCustomSlowCalls()
                    .detectNetwork() //                    .detectResourceMismatches()
                    .penaltyLog()
                    .build()
            )
            val vmPolicy = VmPolicy.Builder().run {
                detectActivityLeaks()
                detectLeakedClosableObjects()
                detectLeakedRegistrationObjects()
                detectFileUriExposure()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    detectContentUriWithoutPermission()
//                    detectUntaggedSockets()
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    detectCredentialProtectedWhileLocked()
                }
                penaltyDeath()
                penaltyLog()
                build()
            }
            StrictMode.setVmPolicy(vmPolicy)
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Fresco.getImagePipeline().clearCaches()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        Fresco.getImagePipeline().clearCaches()
    }

}