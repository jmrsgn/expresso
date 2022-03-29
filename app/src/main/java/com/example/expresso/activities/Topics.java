package com.example.expresso.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.example.expresso.R;
import com.example.expresso.adapters.ViewPagerAdapter;
import com.example.expresso.fragments.ExercisesFragment;
import com.example.expresso.fragments.SummativeFragment;
import com.example.expresso.fragments.TopicsFragment;
import com.example.expresso.interfaces.VolleyResponseListener;
import com.example.expresso.utils.Constants;
import com.example.expresso.utils.Generals;
import com.example.expresso.utils.ModuleHandler;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;

public class Topics extends AppCompatActivity {
    // constants
    private static final String TAG = "Topics";
    private final Context thisContext = Topics.this;

    // views
    private Toolbar tbTopics;
    private ViewPager vpTopics;
    private TabLayout tlTopics;
    private LinearLayout llTopicsLoading;

    // variables
    private TopicsFragment topicsFragment;
    private SummativeFragment summativeFragment;
    private ExercisesFragment exercisesFragment;
    private static String moduleID;
    private Intent intent;
    private String PREVIOUS_ACTIVITY, moduleSlug;
    private SharedPreferences spModulesInfo;
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topics);

        initAll();
    }

    public static String getModuleID() {
        return moduleID;
    }

    private void initAll() {
        // variables
        spModulesInfo = getSharedPreferences(Constants.SP_MODULES_INFO, MODE_PRIVATE);
        gson = new Gson();

        tbTopics = findViewById(R.id.mtb_topics);
        tbTopics.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (llTopicsLoading.getVisibility() == View.GONE) {
                    startActivity(new Intent(thisContext, Modules.class));
                }
            }
        });

        vpTopics = findViewById(R.id.vp_topics);
        tlTopics = findViewById(R.id.tl_topics);
        llTopicsLoading = findViewById(R.id.ll_topics_loading);

        intent = getIntent();
        moduleID = intent.getStringExtra("module_id");
        PREVIOUS_ACTIVITY = intent.getStringExtra("FROM_ACTIVITY");

        topicsFragment = new TopicsFragment(thisContext);
        summativeFragment = new SummativeFragment(thisContext);
        exercisesFragment = new ExercisesFragment(thisContext);

        tlTopics.setupWithViewPager(vpTopics);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), 0);
        viewPagerAdapter.addFragment(topicsFragment, "Topics");
        viewPagerAdapter.addFragment(summativeFragment, "Summative");
        viewPagerAdapter.addFragment(exercisesFragment, "Exercises");
        vpTopics.setAdapter(viewPagerAdapter);

        TabLayout.Tab topicsTab = tlTopics.getTabAt(0);
        TabLayout.Tab quizzesTab = tlTopics.getTabAt(1);
        TabLayout.Tab exercisesTab = tlTopics.getTabAt(2);

        // ==== start
        tlTopics.setVisibility(View.GONE);
        llTopicsLoading.setVisibility(View.VISIBLE);

        if (!Generals.checkInternetConnection(thisContext)) {
            ((ViewGroup) tlTopics.getChildAt(0)).getChildAt(1).setVisibility(View.GONE);
            ((ViewGroup) tlTopics.getChildAt(0)).getChildAt(2).setVisibility(View.GONE);
            topicsTab.select();
            llTopicsLoading.setVisibility(View.GONE);
        } else {
            if (PREVIOUS_ACTIVITY.equals("Modules")){
                topicsTab.select();
            } else if (PREVIOUS_ACTIVITY.equals("Exercise")){
                exercisesTab.select();
            } else if (PREVIOUS_ACTIVITY.equals("Topic")) {
                topicsTab.select();
            } else if (PREVIOUS_ACTIVITY.equals("TopicQuiz")) {
                topicsTab.select();
            } else if (PREVIOUS_ACTIVITY.equals("Quiz")) {
                quizzesTab.select();
            } else if (PREVIOUS_ACTIVITY.equals("TopicVideo")) {
                topicsTab.select();
            }

            ModuleHandler.getInstance(thisContext).checkDoneModuleID(moduleID, new VolleyResponseListener() {
                @Override
                public void onError(String response) {
                    Log.e(TAG, "onError: checkDoneModuleID: " + response);
                }

                @Override
                public void onResponse(String response) {
                    if (response.equals("Success")) {
                        ((ViewGroup) tlTopics.getChildAt(0)).getChildAt(1).setVisibility(View.GONE);
                        ((ViewGroup) tlTopics.getChildAt(0)).getChildAt(2).setVisibility(View.GONE);
                        topicsTab.select();
                    }

                    tlTopics.setVisibility(View.VISIBLE);
                    llTopicsLoading.setVisibility(View.GONE);
                }
            });
        }

        String moduleInfoJson = spModulesInfo.getString(moduleID, null);
        String[] moduleInfo = gson.fromJson(moduleInfoJson, String[].class);
        moduleSlug = moduleInfo[2];

        ModuleHandler.getInstance(thisContext).logVisitModule(moduleSlug);
    }
}