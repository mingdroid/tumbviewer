package com.nutrition.express.ui.video

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.util.Util
import com.nutrition.express.application.BaseActivity
import com.nutrition.express.common.MyExoPlayer
import com.nutrition.express.databinding.ActivityVideoPlayerBinding

class VideoPlayerActivity : BaseActivity() {
    private lateinit var player: SimpleExoPlayer
    private lateinit var binding: ActivityVideoPlayerBinding

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val uri = intent.getParcelableExtra<Uri>("uri")
        val playPosition = intent.getLongExtra("position", C.TIME_UNSET)
        val playWindow = intent.getIntExtra("windowIndex", 0)
        val rotation = intent.getBooleanExtra("rotation", false)
        if (rotation) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        }
        if (uri == null) {
            finish()
            return
        }
        player = MyExoPlayer.preparePlayer(uri, null)

        binding = ActivityVideoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.playerView.requestFocus()
        binding.playerView.player = player
        if (playPosition == C.TIME_UNSET) {
            player.seekToDefaultPosition(playWindow)
        } else {
            player.seekTo(playWindow, playPosition)
        }

        volumeControlStream = AudioManager.STREAM_MUSIC
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) {
            binding.playerView.onResume()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Util.SDK_INT <= 23) {
            binding.playerView.onResume()
        }
        player.playWhenReady = true
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) {
            binding.playerView.onPause()
        }
        player.playWhenReady = false
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            binding.playerView.onPause()
        }
        player.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        //remove all callbacks, avoiding memory leak
        binding.playerView.player = null
        binding.playerView.overlayFrameLayout?.removeAllViews()
    }

}