package com.example.expresso.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.example.expresso.R;
import com.example.expresso.adapters.ViewPagerAdapter;
import com.example.expresso.fragments.TopicContentFragment;
import com.example.expresso.fragments.TopicPlaygroundFragment;
import com.example.expresso.utils.Constants;
import com.example.expresso.utils.Generals;
import com.example.expresso.utils.TopicHandler;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;

import java.util.Map;

public class Topic extends AppCompatActivity {
    // constants
    private static final String TAG = "Topic";
    private final Context thisContext = Topic.this;

    // views
    private Toolbar tbTopic;
    private ViewPager vpTopic;
    private TabLayout tlTopic;

    // variables
    private TopicContentFragment topicContentFragment;
    private TopicPlaygroundFragment topicPlaygroundFragment;
    private String pathIndex, moduleID, topicID, moduleTopicTitle, moduleContent, topicSlug;
    private Intent intent;
    private SharedPreferences spTopicsInfo;
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic);

        initAll();
    }

    private void initAll() {
        // variables
        intent = getIntent();
        moduleID = intent.getStringExtra("module_id");
        topicID = intent.getStringExtra("topic_id");
        moduleTopicTitle = intent.getStringExtra("module_topic_title");
        pathIndex = intent.getStringExtra("path_index");
        moduleContent = intent.getStringExtra("module_content");
        spTopicsInfo = getSharedPreferences(Constants.SP_TOPICS_INFO, MODE_PRIVATE);
        gson = new Gson();

        tbTopic = findViewById(R.id.mtb_topic);
        tbTopic.setTitle(moduleTopicTitle);

        vpTopic = findViewById(R.id.vp_topic);
        tlTopic = findViewById(R.id.tl_topic);

        topicContentFragment = new TopicContentFragment(thisContext);
        topicPlaygroundFragment = new TopicPlaygroundFragment(thisContext);

        tlTopic.setupWithViewPager(vpTopic);

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), 0);
        viewPagerAdapter.addFragment(topicContentFragment, "Content");
        viewPagerAdapter.addFragment(topicPlaygroundFragment, "Playground");

        vpTopic.setAdapter(viewPagerAdapter);

        // listeners
        tbTopic.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout llTopicContentFragmentLoading = (LinearLayout)findViewById(R.id.ll_topic_content_fragment_loading);

                if (llTopicContentFragmentLoading.getVisibility() == View.GONE) {
                    Intent intent = new Intent(thisContext, Topics.class);
                    intent.putExtra("FROM_ACTIVITY", "Topic");
                    intent.putExtra("module_id", moduleID);
                    startActivity(intent);
                }
            }
        });

        tlTopic = findViewById(R.id.tl_topic);
        vpTopic = findViewById(R.id.vp_topic);

        if (!Generals.checkInternetConnection(thisContext)) {
            ((ViewGroup) tlTopic.getChildAt(0)).getChildAt(1).setVisibility(View.GONE);
            TabLayout.Tab tab = tlTopic.getTabAt(0);
            tab.select();
        }

        // loop

        Map<String,?> keys = spTopicsInfo.getAll();
        for(Map.Entry<String,?> entry : keys.entrySet()){
            gson = new Gson();

            String json_ = spTopicsInfo.getString(entry.getKey(), null);
            String[] topicInfo = gson.fromJson(json_, String[].class);

            if (topicInfo[0].equals(topicID) && topicInfo[2].equals(moduleID)) {
                topicSlug = topicInfo[3];
                TopicHandler.getInstance(thisContext).logVisitTopic(topicSlug);
            }
        }
    }

    public String getModuleID() {
        return moduleID;
    }

    public String getTopicID() {
        return topicID;
    }

    public String getModuleTopicTitle() {
        return moduleTopicTitle;
    }

    public String getModuleContent() {
        return moduleContent;
    }

    public String getPathIndex() {
        return pathIndex;
    }
}