package com.nutrition.express.common

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.widget.*
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder
import com.facebook.drawee.view.SimpleDraweeView
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.video.VideoListener
import com.nutrition.express.R
import com.nutrition.express.application.toast
import com.nutrition.express.databinding.ItemVideoControlBinding
import com.nutrition.express.model.api.bean.BaseVideoBean
import com.nutrition.express.ui.video.VideoPlayerActivity
import com.nutrition.express.util.dp2Pixels
import java.util.*

class MyExoPlayerView : FrameLayout {
    private val formatBuilder: StringBuilder = StringBuilder()
    private val formatter: Formatter = Formatter(formatBuilder, Locale.getDefault())
    private lateinit var uri: Uri
    private var player: SimpleExoPlayer? = null

    private var dragging = false
    private var isConnected = false
    private val showTimeoutMs = 3000L
    private val progressBarMax = 1000

    private var controlBinding: ItemVideoControlBinding =
        ItemVideoControlBinding.inflate(LayoutInflater.from(context), this, true)
    private var videoView: TextureView
    private lateinit var thumbnailView: SimpleDraweeView
    private lateinit var loadingBar: ProgressBar
    private var playView: ImageView
    private var leftTime: TextView

    private val videoListener = object : VideoListener {
        override fun onRenderedFirstFrame() {
            thumbnailView.visibility = View.GONE
        }
    }

    private val eventListener = object : Player.EventListener {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            when (playbackState) {
                Player.STATE_BUFFERING -> loadingBar.visibility = View.VISIBLE
                Player.STATE_ENDED -> {
                    show()
                    loadingBar.visibility = View.GONE
                    keepScreenOn = false
                }
                Player.STATE_READY -> {
                    loadingBar.visibility = View.GONE
                    keepScreenOn = true
                }
                Player.STATE_IDLE -> {
                    loadingBar.visibility = View.GONE
                    keepScreenOn = false
                }
            }
            updatePlayPauseButton()
            updateProgress()
            updateLeftTime()
        }

        override fun onPlayerError(error: ExoPlaybackException) {
            disconnect()
            context.toast(R.string.video_play_error)
        }
    }

    private val updateProgressAction = Runnable {
        updateProgress()
    }

    private val hideAction = Runnable {
        hide()
    }

    private val updateTimeAction = Runnable {
        updateLeftTime()
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    @TargetApi(21)
    constructor(
        context: Context,
        attrs: AttributeSet?,
        @AttrRes defStyleAttr: Int,
        @StyleRes defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    init {
        controlBinding.videoControllerProgress.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        controlBinding.timeCurrent.text = stringForTime(positionValue(progress))
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                    removeCallbacks(hideAction)
                    dragging = true
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    player?.seekTo(positionValue(seekBar.progress))
                    hideAfterTimeout()
                    dragging = false
                }
            })
        controlBinding.videoControllerProgress.max = progressBarMax
        controlBinding.videoFullscreen.setOnClickListener {
            val playerIntent = Intent(context, VideoPlayerActivity::class.java)
            playerIntent.putExtra("uri", uri)
            player?.let {
                playerIntent.putExtra("position", it.currentPosition)
                playerIntent.putExtra("windowIndex", it.currentWindowIndex)
            }
            playerIntent.putExtra("rotation", width > height)
            context.startActivity(playerIntent)
            disconnect()
        }

        videoView = TextureView(context)
        val videoParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        videoView.layoutParams = videoParams
        videoView.setOnClickListener {
            if (isConnected) {
                show()
            } else {
                connect()
            }
        }

        thumbnailView = SimpleDraweeView(context)
        val thumbParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        val hierarchy = GenericDraweeHierarchyBuilder(resources)
            .setPlaceholderImage(R.color.loading_color)
            .build()
        thumbnailView.hierarchy = hierarchy
        thumbnailView.layoutParams = thumbParams

        loadingBar = ProgressBar(context, null, android.R.attr.progressBarStyle)
        val loadingParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        loadingParams.gravity = Gravity.CENTER
        loadingBar.layoutParams = loadingParams
        loadingBar.visibility = View.GONE

        playView = ImageView(context)
        val playParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        playParams.gravity = Gravity.CENTER
        playView.layoutParams = playParams
        val padding = dp2Pixels(context, 24)
        playView.setPadding(padding, padding, padding, padding)
        playView.setOnClickListener {
            player?.let {
                if (it.playbackState == ExoPlayer.STATE_ENDED) {
                    it.seekTo(0)
                    it.playWhenReady = true
                } else {
                    it.playWhenReady = !it.playWhenReady
                }
            }
        }

        leftTime = TextView(context)
        val leftParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        leftParams.gravity = Gravity.BOTTOM
        leftParams.bottomMargin = padding / 2
        leftParams.leftMargin = padding / 2
        leftTime.layoutParams = leftParams
        leftTime.setTextColor(Color.WHITE)
        leftTime.visibility = View.GONE

        addView(videoView, 0)
        addView(thumbnailView, 1)
        addView(loadingBar, 2)
        addView(playView, 3)
        addView(leftTime, 4)
    }

    fun bindVideo(video: BaseVideoBean) {
        uri = video.sourceUri
        var params = layoutParams
        if (params == null) {
            params = LayoutParams(video.getWidth(), video.getHeight())
        }
        params.width = video.getWidth()
        params.height = video.getHeight()
        layoutParams = params
        thumbnailView.setImageURI(video.getThumbnailUri(), context)
        thumbnailView.visibility = View.VISIBLE
        hide()
        disconnect()
    }

    fun setPlayerClickable(enable: Boolean) {
        videoView.isClickable = enable
    }

    fun performPlayerClick() {
        if (isConnected) {
            show()
        } else {
            connect()
        }
    }

    private fun connect() {
        val player = MyExoPlayer.preparePlayer(uri) {
            disconnect()
        }
        player.setVideoTextureView(videoView)
        player.addListener(eventListener)
        player.addVideoListener(videoListener)
        player.playWhenReady = true
        this.player = player
        isConnected = true
    }

    private fun disconnect() {
        player?.let {
            it.removeListener(eventListener)
            it.removeVideoListener(videoListener)
            it.stop()
            player = null
        }
        thumbnailView.visibility = View.VISIBLE
        loadingBar.visibility = View.GONE
        isConnected = false
    }

    /**
     * Shows the controller
     */
    private fun show() {
        if (isControllerVisible()) {
            hide()
        } else {
            controlBinding.videoControlLayout.visibility = View.VISIBLE
            playView.visibility = View.VISIBLE
            leftTime.visibility = View.GONE
            updateAll()
        }
        // Call hideAfterTimeout even if already visible to reset the timeout.
        hideAfterTimeout()
    }

    private fun hide() {
        if (isControllerVisible()) {
            controlBinding.videoControlLayout.visibility = View.GONE
            playView.visibility = View.GONE
            removeCallbacks(updateProgressAction)
            removeCallbacks(hideAction)
            updateLeftTime()
        }
    }

    private fun updateLeftTime() {
        if (!isConnected || !isAttachedToWindow) {
            leftTime.visibility = View.GONE
            return
        }
        if (isControllerVisible()) {
            return
        }
        val duration: Long = player?.duration ?: 0L
        val position: Long = player?.currentPosition ?: 0L
        if (duration == C.TIME_UNSET) {
            return
        }
        leftTime.visibility = View.VISIBLE
        leftTime.text = stringForTime(duration - position)
        leftTime.postDelayed(updateTimeAction, 1000)
    }

    private fun hideAfterTimeout() {
        removeCallbacks(hideAction)
        if (isAttachedToWindow) {
            postDelayed(hideAction, showTimeoutMs)
        }
    }

    private fun updateAll() {
        updatePlayPauseButton()
        updateProgress()
    }

    private fun updatePlayPauseButton() {
        if (!isControllerVisible() || !isAttachedToWindow) {
            return
        }
        val playing: Boolean =
            player?.playWhenReady == true && player?.playbackState != ExoPlayer.STATE_ENDED
        val contentDescription = resources.getString(
            if (playing) R.string.exo_controls_pause_description else R.string.exo_controls_play_description
        )
        playView.contentDescription = contentDescription
        playView.setImageResource(if (playing) R.drawable.exo_controls_pause else R.drawable.exo_controls_play)
    }

    private fun updateProgress() {
        if (!isControllerVisible() || !isAttachedToWindow) {
            return
        }
        val duration = player?.duration ?: 0
        val position = player?.currentPosition ?: 0
        controlBinding.time.text = stringForTime(duration)
        if (!dragging) {
            controlBinding.timeCurrent.text = stringForTime(position)
        }
        if (!dragging) {
            controlBinding.videoControllerProgress.progress = progressBarValue(position)
        }
        val bufferedPosition = player?.bufferedPosition ?: 0
        controlBinding.videoControllerProgress.secondaryProgress =
            progressBarValue(bufferedPosition)
        // Remove scheduled updates.
        removeCallbacks(updateProgressAction)
        // Schedule an update if necessary.
        val playbackState = player?.playbackState ?: Player.STATE_IDLE
        if (playbackState != Player.STATE_IDLE && playbackState != Player.STATE_ENDED) {
            var delayMs: Long
            if (player?.playWhenReady == true && playbackState == Player.STATE_READY) {
                delayMs = 1000 - position % 1000
                if (delayMs < 200) {
                    delayMs += 1000
                }
            } else {
                delayMs = 1000
            }
            postDelayed(updateProgressAction, delayMs)
        }
    }

    private fun isControllerVisible(): Boolean {
        return controlBinding.videoControlLayout.visibility == View.VISIBLE
    }

    private fun stringForTime(timeMs: Long): String? {
        var time = timeMs
        if (time == C.TIME_UNSET) {
            time = 0
        }
        val totalSeconds = (time + 500) / 1000
        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60 % 60
        val hours = totalSeconds / 3600
        formatBuilder.setLength(0)
        return if (hours > 0)
            formatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
        else
            formatter.format("%02d:%02d", minutes, seconds).toString()
    }

    private fun progressBarValue(position: Long): Int {
        val duration = player?.duration ?: C.TIME_UNSET
        return if (duration == C.TIME_UNSET || duration == 0L) 0 else (position * progressBarMax / duration).toInt()
    }

    private fun positionValue(progress: Int): Long {
        val duration = player?.duration ?: C.TIME_UNSET
        return if (duration == C.TIME_UNSET || duration == 0L) 0 else (duration * progress / progressBarMax)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        hide()
        disconnect()
    }


}