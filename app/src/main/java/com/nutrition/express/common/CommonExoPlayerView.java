package com.nutrition.express.common;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.SystemClock;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.video.VideoListener;
import com.nutrition.express.R;
import com.nutrition.express.videoplayer.VideoPlayerActivity;

import java.util.Formatter;
import java.util.Locale;

/**
 * Created by huang on 2/20/17.
 */

public class CommonExoPlayerView extends FrameLayout {

    public static final int DEFAULT_SHOW_TIMEOUT_MS = 3000;

    private static final int PROGRESS_BAR_MAX = 1000;

    private ComponentListener componentListener;
    private TextureView videoView;
    private ImageView playView;
    private SimpleDraweeView thumbnailView;
    private LinearLayout controlLayout;
    private TextView time;
    private TextView timeCurrent;
    private TextView leftTime;
    private ImageView fullscreen;
    private SeekBar progressBar;
    private ProgressBar loadingBar;

    private StringBuilder formatBuilder;
    private Formatter formatter;
    private Timeline.Window currentWindow;

    private ExoPlayerInstance playerInstance;
    private SimpleExoPlayer player;
    private Uri uri;

    private boolean isAttachedToWindow;
    private boolean dragging;
    private boolean isConnected;
    private int showTimeoutMs;
    private long hideAtMs;

    private final Runnable updateProgressAction = new Runnable() {
        @Override
        public void run() {
            updateProgress();
//            Log.d("run", "update progress action");
        }
    };

    private final Runnable hideAction = new Runnable() {
        @Override
        public void run() {
            hide();
//            Log.d("run", "hide action");
        }
    };

    private final Runnable updateTimeAction = new Runnable() {
        @Override
        public void run() {
            updateLeftTime();
        }
    };


    public CommonExoPlayerView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public CommonExoPlayerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CommonExoPlayerView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(21)
    public CommonExoPlayerView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        showTimeoutMs = DEFAULT_SHOW_TIMEOUT_MS;

        formatBuilder = new StringBuilder();
        formatter = new Formatter(formatBuilder, Locale.getDefault());
        currentWindow = new Timeline.Window();
        componentListener = new ComponentListener();

        LayoutInflater.from(context).inflate(R.layout.item_video_control, this);
        controlLayout = findViewById(R.id.video_control_layout);
        time = findViewById(R.id.time);
        timeCurrent = findViewById(R.id.time_current);
        fullscreen = findViewById(R.id.video_fullscreen);
        fullscreen.setOnClickListener(componentListener);
        progressBar = findViewById(R.id.video_controller_progress);
        progressBar.setOnSeekBarChangeListener(componentListener);
        progressBar.setMax(PROGRESS_BAR_MAX);

        videoView = new TextureView(context);
        FrameLayout.LayoutParams videoParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        videoView.setLayoutParams(videoParams);
        videoView.setOnClickListener(componentListener);

        thumbnailView = new SimpleDraweeView(context);
        FrameLayout.LayoutParams thumbParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        GenericDraweeHierarchy hierarchy = new GenericDraweeHierarchyBuilder(getResources())
                .setPlaceholderImage(R.color.loading_color)
                .build();
        thumbnailView.setHierarchy(hierarchy);
        thumbnailView.setLayoutParams(thumbParams);

        loadingBar = new ProgressBar(context, null, android.R.attr.progressBarStyle);
        FrameLayout.LayoutParams loadingParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        loadingParams.gravity = Gravity.CENTER;
        loadingBar.setLayoutParams(loadingParams);
        loadingBar.setVisibility(GONE);

        playView = new ImageView(context);
        FrameLayout.LayoutParams playParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        playParams.gravity = Gravity.CENTER;
        playView.setLayoutParams(playParams);
        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24,
                context.getResources().getDisplayMetrics());
        playView.setPadding(padding, padding, padding, padding);
        playView.setOnClickListener(componentListener);

//        setOnClickListener(componentListener);
        leftTime = new TextView(context);
        FrameLayout.LayoutParams leftParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        leftParams.gravity = Gravity.BOTTOM;
        leftParams.bottomMargin = padding / 2;
        leftParams.leftMargin = padding / 2;
        leftTime.setLayoutParams(leftParams);
        leftTime.setTextColor(Color.WHITE);
        leftTime.setVisibility(GONE);

        addView(videoView, 0);
        addView(thumbnailView, 1);
        addView(loadingBar, 2);
        addView(playView, 3);
        addView(leftTime, 4);
    }

    /**
     * should re-init every state
     * @param video
     */
    public void bindVideo(BaseVideoBean video) {
        this.uri = video.getSourceUri();
        ViewGroup.LayoutParams params = getLayoutParams();
        if (params == null) {
            params = new ViewGroup.LayoutParams(video.getWidth(), video.getHeight());
        }
        params.width = video.getWidth();
        params.height = video.getHeight();
        setLayoutParams(params);
        thumbnailView.setImageURI(video.getThumbnailUri());
        thumbnailView.setVisibility(VISIBLE);
        hide();
        disconnect();
    }

    public void setPlayerClickable(boolean enable) {
        if (enable) {
            videoView.setClickable(true);
        } else {
            videoView.setClickable(false);
        }
    }

    public void performPlayerClick() {
        if (isConnected) {
            show();
        } else {
            connect();
        }
    }

    public void setPlayerInstance(ExoPlayerInstance playerInstance) {
        this.playerInstance = playerInstance;
    }

    private void connect() {
        playerInstance.disconnectPrevious();
        player = playerInstance.getPlayer();
        player.setVideoTextureView(videoView);
        player.addListener(componentListener);
        player.addVideoListener(componentListener);
        playerInstance.prepare(uri, componentListener);
        isConnected = true;
    }

    private void disconnect() {
        if (player != null) {
            player.removeListener(componentListener);
            player.removeVideoListener(componentListener);
            player = null;
            playerInstance.stopPlayer();
            playerInstance.abandonAudioFocus();
        }
        thumbnailView.setVisibility(VISIBLE);
        loadingBar.setVisibility(GONE);
        isConnected = false;
    }

    /**
     * Shows the controller
     */
    private void show() {
        if (isControllerVisible()) {
            hide();
        } else {
            controlLayout.setVisibility(VISIBLE);
            playView.setVisibility(VISIBLE);
            leftTime.setVisibility(GONE);
            updateAll();
        }
        // Call hideAfterTimeout even if already visible to reset the timeout.
        hideAfterTimeout();
    }

    /**
     * Hides the controller.
     */
    private void hide() {
        if (isControllerVisible()) {
            controlLayout.setVisibility(GONE);
            playView.setVisibility(GONE);
            removeCallbacks(updateProgressAction);
            removeCallbacks(hideAction);
            hideAtMs = C.TIME_UNSET;
            updateLeftTime();
        }
    }

    private void updateLeftTime() {
        if (!isConnected || !isAttachedToWindow) {
            leftTime.setVisibility(GONE);
            return;
        }
        if (isControllerVisible()) {
            return;
        }
        long duration = player == null ? 0 : player.getDuration();
        long position = player == null ? 0 : player.getCurrentPosition();
        if (duration == C.TIME_UNSET) {
            return;
        }
        leftTime.setVisibility(VISIBLE);
        leftTime.setText(stringForTime(duration - position));
        leftTime.postDelayed(updateTimeAction, 1000);
    }

    /**
     *
     * @return true if controller is visible.
     */
    private boolean isControllerVisible() {
        return controlLayout.getVisibility() == VISIBLE;
    }

    private void hideAfterTimeout() {
        removeCallbacks(hideAction);
        if (showTimeoutMs > 0) {
            hideAtMs = SystemClock.uptimeMillis() + showTimeoutMs;
            if (isAttachedToWindow) {
                postDelayed(hideAction, showTimeoutMs);
            }
        } else {
            hideAtMs = C.TIME_UNSET;
        }
    }

    private void updateAll() {
        updatePlayPauseButton();
        updateProgress();
    }

    private void updatePlayPauseButton() {
        if (!isControllerVisible() || !isAttachedToWindow) {
            return;
        }
        boolean playing = player != null && player.getPlayWhenReady()
                && player.getPlaybackState() != ExoPlayer.STATE_ENDED;
        String contentDescription = getResources().getString(
                playing ? R.string.exo_controls_pause_description : R.string.exo_controls_play_description);
        playView.setContentDescription(contentDescription);
        playView.setImageResource(
                playing ? R.drawable.exo_controls_pause : R.drawable.exo_controls_play);
    }

    private void updateProgress() {
        if (!isControllerVisible() || !isAttachedToWindow) {
            return;
        }
        long duration = player == null ? 0 : player.getDuration();
        long position = player == null ? 0 : player.getCurrentPosition();
        time.setText(stringForTime(duration));
        if (!dragging) {
            timeCurrent.setText(stringForTime(position));
        }
        if (!dragging) {
            progressBar.setProgress(progressBarValue(position));
        }
        long bufferedPosition = player == null ? 0 : player.getBufferedPosition();
        progressBar.setSecondaryProgress(progressBarValue(bufferedPosition));
        // Remove scheduled updates.
        removeCallbacks(updateProgressAction);
        // Schedule an update if necessary.
        int playbackState = player == null ? Player.STATE_IDLE : player.getPlaybackState();
        if (playbackState != Player.STATE_IDLE && playbackState != Player.STATE_ENDED) {
            long delayMs;
            if (player.getPlayWhenReady() && playbackState == Player.STATE_READY) {
                delayMs = 1000 - (position % 1000);
                if (delayMs < 200) {
                    delayMs += 1000;
                }
            } else {
                delayMs = 1000;
            }
            postDelayed(updateProgressAction, delayMs);
        }
    }

    private String stringForTime(long timeMs) {
        if (timeMs == C.TIME_UNSET) {
            timeMs = 0;
        }
        long totalSeconds = (timeMs + 500) / 1000;
        long seconds = totalSeconds % 60;
        long minutes = (totalSeconds / 60) % 60;
        long hours = totalSeconds / 3600;
        formatBuilder.setLength(0);
        return hours > 0 ? formatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
                : formatter.format("%02d:%02d", minutes, seconds).toString();
    }

    private int progressBarValue(long position) {
        long duration = player == null ? C.TIME_UNSET : player.getDuration();
        return duration == C.TIME_UNSET || duration == 0 ? 0
                : (int) ((position * PROGRESS_BAR_MAX) / duration);
    }

    private long positionValue(int progress) {
        long duration = player == null ? C.TIME_UNSET : player.getDuration();
        return duration == C.TIME_UNSET ? 0 : ((duration * progress) / PROGRESS_BAR_MAX);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        isAttachedToWindow = true;
//        Log.d("onAttachedToWindow", "true");
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isAttachedToWindow = false;
        hide();
        disconnect();
//        Log.d("onDetachedFromWindow", "true");
    }

    private final class ComponentListener implements Player.EventListener,
            VideoListener, SeekBar.OnSeekBarChangeListener, OnClickListener,
            ExoPlayerInstance.OnDisconnectListener {

        //ExoPlayerInstance.OnDisconnectListener
        @Override
        public void onDisconnectListener() {
            disconnect();
        }

        //Override SimpleExoPlayer.VideoListener
        @Override
        public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {

        }

        @Override
        public void onRenderedFirstFrame() {
            thumbnailView.setVisibility(GONE);
//            Log.d("onRenderedFirstFrame", "--");
        }

        //Override OnClickListener
        @Override
        public void onClick(View v) {
            if (v == playView && player != null) {
                if (player.getPlaybackState() == ExoPlayer.STATE_ENDED) {
                    player.seekTo(0);
                    player.setPlayWhenReady(true);
                } else {
                    player.setPlayWhenReady(!player.getPlayWhenReady());
                }
            } else if (v == fullscreen) {
                Intent playerIntent = new Intent(getContext(), VideoPlayerActivity.class);
                playerIntent.putExtra("uri", uri);
                playerIntent.putExtra("position", player.getCurrentPosition());
                playerIntent.putExtra("windowIndex", player.getCurrentWindowIndex());
                playerIntent.putExtra("rotation", getWidth() > getHeight());
                getContext().startActivity(playerIntent);
                disconnect();
                playerInstance.startFullScreenMode();
            } else if (v == videoView) {
                if (isConnected) {
                    show();
                } else {
                    connect();
                }
            }
        }

        //Override SeekBar.OnSeekBarChangeListener
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                timeCurrent.setText(stringForTime(positionValue(progress)));
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            removeCallbacks(hideAction);
            dragging = true;
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            dragging = false;
            if (player != null) {
                player.seekTo(positionValue(seekBar.getProgress()));
            }
            hideAfterTimeout();
        }

        //Override ExoPlayer.EventListener

        @Override
        public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {

        }

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

        }

        @Override
        public void onLoadingChanged(boolean isLoading) {

        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            Log.d("onPlayerStateChanged", playWhenReady + "-" + playbackState);
            if (playbackState == Player.STATE_BUFFERING) {
                loadingBar.setVisibility(VISIBLE);
            } else if (playbackState == Player.STATE_ENDED) {
                show();
                playerInstance.abandonAudioFocus();
                loadingBar.setVisibility(GONE);
                setKeepScreenOn(true);
            } else {
                loadingBar.setVisibility(GONE);
                setKeepScreenOn(false);
            }
            updatePlayPauseButton();
            updateProgress();
            updateLeftTime();
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            disconnect();
            Toast.makeText(getContext(), R.string.video_play_error, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

        }

        @Override
        public void onPositionDiscontinuity(int reason) {

        }

        @Override
        public void onSeekProcessed() {

        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {

        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

        }
    }

}
