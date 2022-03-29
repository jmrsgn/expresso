package com.example.expresso.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.widget.Toolbar;

import com.example.expresso.R;
import com.example.expresso.utils.Constants;
import com.example.expresso.utils.Generals;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

public class TopicVideo extends YouTubeBaseActivity {
    // constants
    private static final String TAG = "TopicVideo";
    private final Context thisContext = TopicVideo.this;

    // views
    private Toolbar tbTopicVideo;

    // variables
    private Intent intent;
    private String topicModuleID, topicID, topicContent, topicTitle, topicPathIndex, topicYoutubeEmbedCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic_video);

        initAll();

        YouTubePlayerView ytpvTopicVideoVideo = findViewById(R.id.ytpv_topic_video_video);

        ytpvTopicVideoVideo.initialize(Constants.YOUTUBE_PLAYER_API_KEY, new YouTubePlayer.OnInitializedListener() {

            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                youTubePlayer.setFullscreen(false);
                youTubePlayer.loadVideo(topicYoutubeEmbedCode);
                youTubePlayer.play();
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
                Log.e(TAG, "onInitializationFailure: " + youTubeInitializationResult);
                Generals.makeToast(thisContext, youTubeInitializationResult.toString());
            }
        });
    }

    private void initAll() {
        // views
        tbTopicVideo = findViewById(R.id.mtb_topic_video);

        // variables
        intent = getIntent();
        topicModuleID = intent.getStringExtra("module_id");
        topicID = intent.getStringExtra("topic_id");
        topicTitle = intent.getStringExtra("module_topic_title");
        topicContent = intent.getStringExtra("module_content");
        topicPathIndex = intent.getStringExtra("path_index");
        topicYoutubeEmbedCode = intent.getStringExtra("topic_youtube_embed_code");


        // listeners
        tbTopicVideo.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(thisContext, Topic.class);
                intent.putExtra("module_id", topicModuleID);
                intent.putExtra("topic_id", topicID);
                intent.putExtra("module_topic_title", topicTitle);
                intent.putExtra("module_content", topicContent);
                intent.putExtra("path_index", topicPathIndex);
                thisContext.startActivity(intent);
            }
        });
    }
}