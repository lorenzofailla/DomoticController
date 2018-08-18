package com.apps.lore_f.domoticcontroller;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.apps.lore_f.domoticcontroller.Developer_Keys.YOUTUBE;

public class YouTubeLiveViewActivity extends YouTubeBaseActivity {

    private static final String TAG = YouTubeLiveViewActivity.class.getName();
    private String liveBroadcastID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_you_tube_live_view);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        if (intent.hasExtra("__live_broadcast_ID")) {

            liveBroadcastID = extras.getString("__live_broadcast_ID");
            startLiveBroadcastView();

        } else {

            finish();
            return;

        }

    }

    private void startLiveBroadcastView() {

        YouTubePlayerView yt = (YouTubePlayerView) findViewById(R.id.YTV___VSCAMERAVIEW___LIVEBROADCASTVIEW);

        yt.initialize(YOUTUBE, new YouTubePlayer.OnInitializedListener() {

            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, final YouTubePlayer youTubePlayer, boolean wasRestored) {


                youTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);

                youTubePlayer.setPlayerStateChangeListener(new YouTubePlayer.PlayerStateChangeListener() {
                    @Override
                    public void onLoading() {

                        Log.d(TAG, "onLoading");

                    }

                    @Override
                    public void onLoaded(String s) {
                        Log.d(TAG, "onLoaded: " +s);
                        youTubePlayer.play();
                    }

                    @Override
                    public void onAdStarted() {

                    }

                    @Override
                    public void onVideoStarted() {
                        Log.d(TAG, "onVideoStarted");
                    }

                    @Override
                    public void onVideoEnded() {
                        Log.d(TAG, "onVideoEnded");
                    }

                    @Override
                    public void onError(YouTubePlayer.ErrorReason errorReason) {

                        Log.d(TAG, "onError: " + errorReason.toString());

                    }

                });

                youTubePlayer.setPlaybackEventListener(new YouTubePlayer.PlaybackEventListener() {
                    @Override
                    public void onPlaying() {
                        Log.d(TAG, "onPlaying");
                    }

                    @Override
                    public void onPaused() {
                        Log.d(TAG, "onPaused");
                    }

                    @Override
                    public void onStopped() {
                        Log.d(TAG, "onStopped");
                    }

                    @Override
                    public void onBuffering(boolean b) {
                        Log.d(TAG, "onBuffering: "+b);
                    }

                    @Override
                    public void onSeekTo(int i) {
                        Log.d(TAG, "onSeekTo: " + i);
                    }
                });

                youTubePlayer.cueVideo(liveBroadcastID);

            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

            }

        });

    }

}
