package com.nutrition.express.common

import android.net.Uri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.nutrition.express.R
import com.nutrition.express.application.TumbApp
import okhttp3.OkHttpClient

object MyExoPlayer : LifecycleObserver {
    private val context = TumbApp.app
    private val dataSourceFactor = DefaultDataSourceFactory(
        context,
        OkHttpDataSourceFactory(
            OkHttpClient(),
            Util.getUserAgent(context, context.getString(R.string.app_name))
        )
    )
    private var player: SimpleExoPlayer? = null
    private var listener: (() -> Unit)? = null

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onAppPause() {
        pause()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppStop() {
        release()
    }

    fun preparePlayer(uri: Uri, disconnect: (() -> Unit)?): SimpleExoPlayer {
        val player = player ?: SimpleExoPlayer.Builder(context).build()
        this.player = player
        listener?.invoke()
        listener = disconnect

        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactor)
            .createMediaSource(MediaItem.Builder().setUri(uri).build())
        player.setMediaSource(mediaSource)
        player.prepare()
        player.playWhenReady = false
        return player
    }

    private fun release() {
        player?.release()
        player = null
    }

    fun resume() {
        player?.playWhenReady = true
    }

    fun pause() {
        player?.playWhenReady = false
    }

    fun stop() {
        player?.stop()
    }

}