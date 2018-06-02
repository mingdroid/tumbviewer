package com.nutrition.express.videoplayer;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.util.Util;
import com.nutrition.express.R;
import com.nutrition.express.common.ExoPlayerInstance;

/**
 * Created by huang on 11/9/16.
 */

public class VideoPlayerActivity extends AppCompatActivity {
    private ExoPlayerInstance playerInstance;
    private PlayerView playerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Uri uri = intent.getParcelableExtra("uri");
        long playerPosition = intent.getLongExtra("position", C.TIME_UNSET);
        int playerWindow = intent.getIntExtra("windowIndex", 0);
        boolean rotation = intent.getBooleanExtra("rotation", false);
        if (rotation) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }
        playerInstance = ExoPlayerInstance.getInstance();
        SimpleExoPlayer player = playerInstance.getPlayer();

        setContentView(R.layout.activity_video_player);
        playerView = findViewById(R.id.player_view);
        playerView.setPlayer(player);
        if (playerPosition == C.TIME_UNSET) {
            player.seekToDefaultPosition(playerWindow);
        } else {
            player.seekTo(playerWindow, playerPosition);
        }
        playerInstance.prepare(uri, null);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
            playerInstance.resumePlayer();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Util.SDK_INT <= 23) {
            playerInstance.resumePlayer();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            playerInstance.pausePlayer();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        playerInstance.releasePlayer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //remove all callbacks, avoiding memory leak
        playerView.setPlayer(null);
    }
}
