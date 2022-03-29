package com.example.expresso.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.expresso.R;
import com.example.expresso.activities.Topic;
import com.example.expresso.activities.TopicQuiz;
import com.example.expresso.activities.TopicVideo;
import com.example.expresso.interfaces.VolleyResponseListener;
import com.example.expresso.utils.Constants;
import com.example.expresso.utils.Generals;
import com.example.expresso.utils.ModuleHandler;
import com.example.expresso.utils.QuizHandler;
import com.example.expresso.utils.TopicHandler;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragmentX;
import com.ortiz.touchview.TouchImageView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.Markwon;
import io.noties.markwon.core.MarkwonTheme;


public class TopicContentFragment extends Fragment {
    // constants
    private static final String TAG = "TopicContentFragment";
    private final Context mContext;

    // views
    private Markwon mdvTopicContentFragmentMarkdown;
    private TextView tvTopicContentFragmentContent, tvTopicContentFragmentTopicQuizNote, tvTopicContentFragmentYoutubeLink;
    private Button btnTopicContentFragmentStartTopicQuiz;
    private LinearLayout llTopicContentFragmentImages, llTopicContentFragmentTopicQuizAttempt, llTopicContentFragmentOtherMaterials, llTopicContentFragmentLoading;

    // variables
    private String topicPathIndex, topicModuleID, topicID, topicTitle, topicContent;
    private boolean showImages = false;

    public TopicContentFragment(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_topic_content, container, false);

        initAll(view);

        btnTopicContentFragmentStartTopicQuiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, TopicQuiz.class);
                intent.putExtra("module_id", topicModuleID);
                intent.putExtra("topic_id", topicID);
                intent.putExtra("module_topic_title", topicTitle);
                intent.putExtra("module_content", topicContent);
                intent.putExtra("path_index", topicPathIndex);
                startActivity(intent);
            }
        });

        return view;
    }

    private void initAll(View view) {
        // variables
        Topic activity = (Topic) getActivity();
        topicModuleID = activity.getModuleID();
        topicID = activity.getTopicID();
        topicTitle = activity.getModuleTopicTitle();
        topicContent = activity.getModuleContent();
        topicPathIndex = activity.getPathIndex();

//        Generals.makeToast(mContext, topicContent);

        // views
        btnTopicContentFragmentStartTopicQuiz = view.findViewById(R.id.btn_topic_content_fragment_start_topic_quiz);
        tvTopicContentFragmentContent = view.findViewById(R.id.tv_topic_content_fragment_content);
        tvTopicContentFragmentYoutubeLink = view.findViewById(R.id.tv_topic_content_fragment_youtube_link);
        tvTopicContentFragmentTopicQuizNote = view.findViewById(R.id.tv_topic_content_fragment_topic_quiz_note);
        llTopicContentFragmentTopicQuizAttempt = view.findViewById(R.id.ll_topic_content_fragment_topic_quiz_attempt);
        llTopicContentFragmentOtherMaterials = view.findViewById(R.id.ll_topic_content_fragment_other_materials);
        llTopicContentFragmentLoading = view.findViewById(R.id.ll_topic_content_fragment_loading);
        llTopicContentFragmentImages = view.findViewById(R.id.ll_topic_content_fragment_images);

        mdvTopicContentFragmentMarkdown = Markwon.builder(mContext)
                .usePlugin(new AbstractMarkwonPlugin() {
                    @Override
                    public void configureTheme(@NonNull MarkwonTheme.Builder builder) {
                        builder.linkColor(Color.BLUE);
                    }
                })

                .build();

        if (!Generals.checkInternetConnection(mContext)) {
            hideViews();
            setValues();
        } else {
            hideViews();
            llTopicContentFragmentLoading.setVisibility(View.VISIBLE);

            ModuleHandler.getInstance(mContext).getModuleSummativeID(topicModuleID, new VolleyResponseListener() {
                @Override
                public void onError(String response) {
                    Log.e(TAG, "onError: getModuleSummativeID: " + response);
                }

                @Override
                public void onResponse(String response) {
                    QuizHandler.getInstance(mContext).getFailedQuizAttemptsCount(response, new VolleyResponseListener() {
                        @Override
                        public void onError(String response) {
                            Log.e(TAG, "onError: getFailedQuizAttemptsCount: " + response);
                        }

                        @Override
                        public void onResponse(String response) {
                            int userQuizAttemptsCount = Integer.parseInt(response);

                            TopicHandler.getInstance(mContext).getTopicVideoURLs(topicID, new VolleyResponseListener() {
                                @Override
                                public void onError(String response) {
                                    Log.e(TAG, "onError: getTopicVideoURLs: " + response);
                                }

                                @Override
                                public void onResponse(String response) {
                                    try {
                                        JSONArray arrayVideoURLs = new JSONArray(response);
                                        int arrayLength = arrayVideoURLs.length();

                                        JSONObject objectVideoURL = arrayVideoURLs.getJSONObject(userQuizAttemptsCount % arrayLength);

                                        tvTopicContentFragmentYoutubeLink.setText("Video link: " + objectVideoURL.getString("embed"));

                                        String[] embed = objectVideoURL.getString("embed").split("/");
                                        String embedCode = embed[embed.length - 1];

                                        setUpYoutubePlayer(embedCode);

                                        showViews();
                                        setValues();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    });
                }
            });
        }
    }

    private void hideViews() {
        tvTopicContentFragmentTopicQuizNote.setVisibility(View.GONE);
        llTopicContentFragmentTopicQuizAttempt.setVisibility(View.GONE);
        llTopicContentFragmentOtherMaterials.setVisibility(View.GONE);
    }

    private void showViews() {
        tvTopicContentFragmentTopicQuizNote.setVisibility(View.VISIBLE);
        llTopicContentFragmentTopicQuizAttempt.setVisibility(View.VISIBLE);
        llTopicContentFragmentOtherMaterials.setVisibility(View.VISIBLE);
    }

    private void setUpYoutubePlayer(String topicYoutubeEmbedCode) {
        YouTubePlayerSupportFragmentX youTubePlayerSupportFragmentX = YouTubePlayerSupportFragmentX.newInstance();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_fragment, youTubePlayerSupportFragmentX);
        transaction.commit();

        youTubePlayerSupportFragmentX.initialize(Constants.YOUTUBE_PLAYER_API_KEY, new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                youTubePlayer.loadVideo(topicYoutubeEmbedCode);
                youTubePlayer.setFullscreen(false);
                youTubePlayer.setShowFullscreenButton(false);
                youTubePlayer.pause();

                youTubePlayer.setPlaybackEventListener(new YouTubePlayer.PlaybackEventListener() {
                    @Override
                    public void onPlaying() {
                        Intent intent = new Intent(mContext, TopicVideo.class);
                        intent.putExtra("module_id", topicModuleID);
                        intent.putExtra("topic_id", topicID);
                        intent.putExtra("module_topic_title", topicTitle);
                        intent.putExtra("module_content", topicContent);
                        intent.putExtra("path_index", topicPathIndex);
                        intent.putExtra("topic_youtube_embed_code", topicYoutubeEmbedCode);
                        mContext.startActivity(intent);
                    }

                    @Override
                    public void onPaused() {

                    }

                    @Override
                    public void onStopped() {

                    }

                    @Override
                    public void onBuffering(boolean b) {

                    }

                    @Override
                    public void onSeekTo(int i) {

                    }
                });

                youTubePlayer.cueVideo(topicYoutubeEmbedCode);
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
                Log.e(TAG, "onInitializationFailure: " + youTubeInitializationResult);
            }
        });
    }

    private void setValues() {
        llTopicContentFragmentLoading.setVisibility(View.GONE);
        String modifiedContent = topicContent.replaceAll("ImageViews_start", "Image links:")
                                             .replaceAll("ImageViews_end", "");

        mdvTopicContentFragmentMarkdown.setMarkdown(tvTopicContentFragmentContent, modifiedContent);

        if (topicContent.contains("ImageViews_start")) {
            showImages = true;
        }

        if (showImages) {
            ArrayList<String> imageLinks = new ArrayList<>();

            String[] imageLinksString = topicContent.substring(topicContent.indexOf("ImageViews_start"), topicContent.indexOf("ImageViews_end") + ("ImageViews_end".length())).split("\n");

            for (int i = 0; i < imageLinksString.length; i ++) {
                if (i != 0 && i != (imageLinksString.length - 1)) {
                    imageLinks.add(imageLinksString[i]);
                }
            }

            LinearLayout.LayoutParams lpLlImages = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            LinearLayout.LayoutParams lpIvImages = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            Typeface tfOpenSansItalic = ResourcesCompat.getFont(mContext, R.font.open_sans_italic);

            lpLlImages.setMargins(0, 0, 0 , 42);

            for (int i = 0; i < imageLinks.size(); i ++) {
                // make image views

                System.out.println(imageLinks.get(i));

                LinearLayout llImage = new LinearLayout(mContext);
                llImage.setOrientation(LinearLayout.VERTICAL);
                llImage.setLayoutParams(lpLlImages);

                TextView tvImageText = new TextView(mContext);
                tvImageText.setText("figure " + topicID + "." + (i + 1));
                tvImageText.setTextColor(getResources().getColor(R.color.cb_on_background_black));
                tvImageText.setTypeface(tfOpenSansItalic, Typeface.ITALIC);
                tvImageText.setAlpha(0.87f);

                TouchImageView tivImage = new TouchImageView(mContext);
                tivImage.setLayoutParams(lpIvImages);

                Picasso.get()
                        .load(imageLinks.get(i))
                        .into(tivImage);

                // TODO: fix picasso not loading some images from drive

                llImage.addView(tvImageText);
                llImage.addView(tivImage);
                llTopicContentFragmentImages.addView(llImage);
            }

            System.out.println(imageLinks.size());
        }
    }
}