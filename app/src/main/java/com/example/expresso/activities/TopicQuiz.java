package com.example.expresso.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;

import com.bumptech.glide.Glide;
import com.example.expresso.R;
import com.example.expresso.interfaces.VolleyResponseListener;
import com.example.expresso.models.TopicQuizChoice;
import com.example.expresso.utils.Constants;
import com.example.expresso.utils.ExerciseHandler;
import com.example.expresso.utils.Generals;
import com.example.expresso.utils.ModuleHandler;
import com.example.expresso.utils.QuizHandler;
import com.example.expresso.utils.TopicHandler;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public class TopicQuiz extends AppCompatActivity {
    // constants
    private static final String TAG = "TopicQuiz";
    private final Context thisContext = TopicQuiz.this;

    // views
    private Toolbar tbTopicQuiz;
    private LinearLayout.LayoutParams lpMarginTop60dp, lpMarginTop12dp;
    private LinearLayout llTopicQuizQuiz, llTopicQuizLoading;
    private Button btnTopicQuizPreviousModule, btnTopicQuizNextModule;

    // variables
    private ArrayList<TopicQuizChoice> allTopicQuizChoices;
    private String moduleID, topicID, moduleTopicTitle, pathIndex, moduleContent, prevChoiceID, chosenChoice, chosenChoiceID, nextPathIndex, previousPathIndex, moduleSlug, topicSlug;
    private Intent intent;
    private ArrayList<String> chosenChoiceIDs, chosenChoiceIDsPerContent, topicsPathIndexes, rightTopicQuizChoiceIDs;
    private SharedPreferences spTopicsInfo, spTopicsPathIndexes, spModulesInfo;
    private Gson gson;
    private int numberOfTopicQuestions, score;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic_quiz);

        initAll();

        // get path indexes
        String topicsPathIndexesJson = spTopicsPathIndexes.getString(moduleID, null);
        String[] topicsPathIndexesArray = gson.fromJson(topicsPathIndexesJson, String[].class);

        Collections.addAll(topicsPathIndexes, topicsPathIndexesArray);

        if (topicsPathIndexes.get(0).equals(pathIndex)) {
            btnTopicQuizPreviousModule.setVisibility(View.GONE);
            nextPathIndex = topicsPathIndexes.get(topicsPathIndexes.indexOf(pathIndex) + 1);

            Map<String,?> keys = spTopicsInfo.getAll();
            for(Map.Entry<String,?> entry : keys.entrySet()){
                gson = new Gson();

                String json_ = spTopicsInfo.getString(entry.getKey(), null);
                String[] topicInfo = gson.fromJson(json_, String[].class);

                if (topicInfo[4].equals(nextPathIndex) && topicInfo[2].equals(moduleID)) {
                    btnTopicQuizNextModule.setText(topicInfo[1]);
                }
            }
        } else if (topicsPathIndexes.get(topicsPathIndexes.size() - 1).equals(pathIndex)) {
            ModuleHandler.getInstance(thisContext).checkDoneModuleID(moduleID, new VolleyResponseListener() {
                @Override
                public void onError(String response) {
                    Log.e(TAG, "onError: checkDoneModuleID: " + response);
                }

                @Override
                public void onResponse(String response) {
                    if (response.equals("Success")) {
                        btnTopicQuizNextModule.setText("Return to Topics");
                    } else {
                        btnTopicQuizNextModule.setText("Unlock Quiz and Exercises");
                    }

                    previousPathIndex = topicsPathIndexes.get(topicsPathIndexes.indexOf(pathIndex) - 1);

                    Map<String,?> keys = spTopicsInfo.getAll();
                    for(Map.Entry<String,?> entry : keys.entrySet()){
                        gson = new Gson();

                        String json_ = spTopicsInfo.getString(entry.getKey(), null);
                        String[] topicInfo = gson.fromJson(json_, String[].class);

                        if (topicInfo[4].equals(previousPathIndex) && topicInfo[2].equals(moduleID)) {
                            btnTopicQuizPreviousModule.setText(topicInfo[1]);
                        }
                    }
                }
            });
        } else {
            btnTopicQuizPreviousModule.setVisibility(View.VISIBLE);
            previousPathIndex = topicsPathIndexes.get(topicsPathIndexes.indexOf(pathIndex) - 1);
            nextPathIndex = topicsPathIndexes.get(topicsPathIndexes.indexOf(pathIndex) + 1);

            Map<String,?> keys = spTopicsInfo.getAll();
            for(Map.Entry<String,?> entry : keys.entrySet()){
                gson = new Gson();

                String json_ = spTopicsInfo.getString(entry.getKey(), null);
                String[] topicInfo = gson.fromJson(json_, String[].class);

                if (topicInfo[4].equals(previousPathIndex) && topicInfo[2].equals(moduleID)) {
                    btnTopicQuizPreviousModule.setText(topicInfo[1]);
                }

                if (topicInfo[4].equals(nextPathIndex) && topicInfo[2].equals(moduleID)) {
                    btnTopicQuizNextModule.setText(topicInfo[1]);
                }
            }
        }

        // =======

        QuizHandler.getInstance(thisContext).getNumberOfTopicQuizQuestions(topicID, new VolleyResponseListener() {
            @Override
            public void onError(String response) {
                Log.e(TAG, "onError: getNumberOfTopicQuestions: " + response);
            }

            @Override
            public void onResponse(String response) {
                numberOfTopicQuestions = Integer.parseInt(response);
            }
        });

        btnTopicQuizPreviousModule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String,?> keys = spTopicsInfo.getAll();
                for(Map.Entry<String,?> entry : keys.entrySet()){
                    gson = new Gson();

                    String json_ = spTopicsInfo.getString(entry.getKey(), null);
                    String[] topicInfo = gson.fromJson(json_, String[].class);

                    if (topicInfo[4].equals(previousPathIndex) && topicInfo[2].equals(moduleID)) {
                        goToNextTopic(entry.getKey(), topicInfo[1], topicInfo[5], topicInfo[4]);
                    }
                }

                if (String.valueOf(numberOfTopicQuestions).equals(String.valueOf(chosenChoiceIDsPerContent.size()))) {
                    QuizHandler.getInstance(thisContext).logAnsweredTopicQuiz(topicSlug);
                }
            }
        });

        btnTopicQuizNextModule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (String.valueOf(numberOfTopicQuestions).equals(String.valueOf(chosenChoiceIDsPerContent.size()))) {
                    disableChoices();
                    disableTopicQuizNavigationButtons();

                    for (int i = 0; i < chosenChoiceIDsPerContent.size(); i ++) {
                        if (rightTopicQuizChoiceIDs.contains(chosenChoiceIDsPerContent.get(i))) {
                            score += 1;
                        }

                        if (i == chosenChoiceIDsPerContent.size() - 1) {
                            QuizHandler.getInstance(thisContext).updateTopicScore(topicID, String.valueOf(score), new VolleyResponseListener() {
                                @Override
                                public void onError(String response) {
                                    Log.e(TAG, "onError: updateTopicScore: " + response);
                                }

                                @Override
                                public void onResponse(String response) {
                                    if (btnTopicQuizNextModule.getText().toString().equals("Return to Topics")) {
                                        Intent intent = new Intent(thisContext, Topics.class);
                                        intent.putExtra("FROM_ACTIVITY", "TopicQuiz");
                                        intent.putExtra("module_id", moduleID);

                                        startActivity(intent);
                                        QuizHandler.getInstance(thisContext).logAnsweredTopicQuiz(topicSlug);
                                    } else if (btnTopicQuizNextModule.getText().toString().equals("Unlock Quiz and Exercises")) {
                                        ModuleHandler.getInstance(thisContext).getModuleSummativeID(moduleID, new VolleyResponseListener() {
                                            @Override
                                            public void onError(String response) {
                                                Log.e(TAG, "onError: getModuleSummativeID: " + response);
                                            }

                                            @Override
                                            public void onResponse(String response) {
                                                String summativeID = response;

                                                QuizHandler.getInstance(thisContext).addQuiz(summativeID, new VolleyResponseListener() {
                                                    @Override
                                                    public void onError(String response) {
                                                        Log.e(TAG, "onError: addQuiz: " + response);
                                                    }

                                                    @Override
                                                    public void onResponse(String response) {
                                                        if (response.equals("Success")) {
                                                            String moduleInfoJson = spModulesInfo.getString(moduleID, null);

                                                            if (moduleInfoJson != null) {
                                                                String[] moduleInfo = gson.fromJson(moduleInfoJson, String[].class);
                                                                ExerciseHandler.generateExercises(thisContext, moduleInfo[2]);
                                                                showExpressoDialog();
                                                            }
                                                        }

                                                        QuizHandler.getInstance(thisContext).logAnsweredTopicQuiz(topicSlug);
                                                        QuizHandler.getInstance(thisContext).logGeneratedExercisesFromModule(moduleSlug);
                                                    }
                                                });
                                            }
                                        });
                                    } else {
                                        Map<String,?> keys = spTopicsInfo.getAll();
                                        for(Map.Entry<String,?> entry : keys.entrySet()){
                                            gson = new Gson();

                                            String json_ = spTopicsInfo.getString(entry.getKey(), null);
                                            String[] topicInfo = gson.fromJson(json_, String[].class);

                                            if (topicInfo[4].equals(nextPathIndex) && topicInfo[2].equals(moduleID)) {
                                                TopicHandler.getInstance(thisContext).checkTopicID(entry.getKey(), new VolleyResponseListener() {
                                                    @Override
                                                    public void onError(String response) {
                                                        Log.e(TAG, "onError: checkTopicID: " + response);
                                                    }

                                                    @Override
                                                    public void onResponse(String response) {
                                                        if (response.equals("Failed")) {
                                                            TopicHandler.getInstance(thisContext).addTopic(entry.getKey(), new VolleyResponseListener() {
                                                                @Override
                                                                public void onError(String response) {
                                                                    Log.e(TAG, "onError: addTopic: " + response);
                                                                }

                                                                @Override
                                                                public void onResponse(String response) {
                                                                    goToNextTopic(entry.getKey(), topicInfo[1], topicInfo[5], topicInfo[4]);
                                                                }
                                                            });
                                                        } else {
                                                            goToNextTopic(entry.getKey(), topicInfo[1], topicInfo[5], topicInfo[4]);
                                                        }
                                                    }
                                                });
                                            }
                                        }

                                        QuizHandler.getInstance(thisContext).logAnsweredTopicQuiz(topicSlug);
                                    }
                                }
                            });
                        }
                    }
                } else {
                    Generals.makeToast(thisContext, "There are unanswered questions");
                }
            }
        });
    }

    private void goToNextTopic(String topicID, String moduleTopicTitle, String moduleContent, String pathIndex) {
        Intent intent = new Intent(thisContext, Topic.class);
        intent.putExtra("module_id", moduleID);
        intent.putExtra("topic_id", topicID);
        intent.putExtra("module_topic_title", moduleTopicTitle);
        intent.putExtra("module_content", moduleContent);
        intent.putExtra("path_index", pathIndex);
        startActivity(intent);
    }

    private void initAll() {
        intent = getIntent();
        moduleID = intent.getStringExtra("module_id");
        topicID = intent.getStringExtra("topic_id");
        moduleTopicTitle = intent.getStringExtra("module_topic_title");
        pathIndex = intent.getStringExtra("path_index");
        moduleContent = intent.getStringExtra("module_content");
        chosenChoiceIDs = new ArrayList<>();
        chosenChoiceIDsPerContent = new ArrayList<>();
        topicsPathIndexes = new ArrayList<>();
        allTopicQuizChoices = new ArrayList<>();
        rightTopicQuizChoiceIDs = new ArrayList<>();
        score = 0;

        spTopicsInfo = thisContext.getSharedPreferences(Constants.SP_TOPICS_INFO, MODE_PRIVATE);
        spTopicsPathIndexes = thisContext.getSharedPreferences(Constants.SP_TOPICS_PATH_INDEXES, MODE_PRIVATE);
        spModulesInfo = thisContext.getSharedPreferences(Constants.SP_MODULES_INFO, MODE_PRIVATE);
        gson = new Gson();

        btnTopicQuizPreviousModule = findViewById(R.id.btn_topic_quiz_previous_module);
        btnTopicQuizNextModule = findViewById(R.id.btn_topic_quiz_next_module);
        llTopicQuizQuiz = findViewById(R.id.ll_topic_quiz_quiz);
        llTopicQuizLoading = findViewById(R.id.ll_topic_quiz_loading);
        tbTopicQuiz = findViewById(R.id.mtb_topic_quiz);
        tbTopicQuiz.setTitle(moduleTopicTitle);

        llTopicQuizLoading.setVisibility(View.VISIBLE);

        // listeners
        tbTopicQuiz.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (llTopicQuizLoading.getVisibility() == View.GONE) {
                    Intent intent = new Intent(thisContext, Topic.class);
                    intent.putExtra("module_id", moduleID);
                    intent.putExtra("topic_id", topicID);
                    intent.putExtra("module_topic_title", moduleTopicTitle);
                    intent.putExtra("module_content", moduleContent);
                    intent.putExtra("path_index", pathIndex);
                    startActivity(intent);
                }
            }
        });

        String moduleInfoJson = spModulesInfo.getString(moduleID, null);
        String[] moduleInfo = gson.fromJson(moduleInfoJson, String[].class);
        moduleSlug = moduleInfo[2];

        String topicInfoJson = spTopicsInfo.getString(topicID, null);
        String[] topicInfo = gson.fromJson(topicInfoJson, String[].class);
        topicSlug = topicInfo[3];

        QuizHandler.getInstance(thisContext).getAllTopicQuizChoices(new VolleyResponseListener() {
            @Override
            public void onError(String response) {
                Log.e(TAG, "onError: getAllTopicQuizChoices: " + response);
            }

            @Override
            public void onResponse(String response) {
                try {
                    JSONArray array = new JSONArray(response);

                    for (int i = 0; i < array.length(); i ++) {
                        JSONObject object = array.getJSONObject(i);

                        allTopicQuizChoices.add(new TopicQuizChoice(object.getString("id"),
                                object.getString("choice"),
                                object.getString("is_correct"),
                                object.getString("question_id")));
                    }

                    QuizHandler.getInstance(thisContext).saveTopicQuizChoicesToSP(allTopicQuizChoices);

                    // ====

                    QuizHandler.getInstance(thisContext).getTopicQuizAnsweredChoiceIDs(new VolleyResponseListener() {
                        @Override
                        public void onError(String response) {
                            Log.e(TAG, "onError: getTopicQuizAnsweredChoiceIDs: " + response);
                        }

                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONArray arrayChoiceIDs = new JSONArray(response);

                                for (int i = 0; i < arrayChoiceIDs.length(); i ++) {
                                    JSONObject objectChoiceID = arrayChoiceIDs.getJSONObject(i);

                                    chosenChoiceIDs.add(objectChoiceID.getString("choice_id"));
                                }

                                QuizHandler.getInstance(thisContext).getRightTopicQuizChoiceIDs(new VolleyResponseListener() {
                                    @Override
                                    public void onError(String response) {
                                        Log.e(TAG, "onError: getRightTopicQuizChoiceIDs: " + response);
                                    }

                                    @Override
                                    public void onResponse(String response) {
                                        try {
                                            JSONArray arrayChoiceIDs = new JSONArray(response);

                                            for (int i = 0; i < arrayChoiceIDs.length(); i ++) {
                                                JSONObject objectChoiceID = arrayChoiceIDs.getJSONObject(i);

                                                String choiceID = objectChoiceID.getString("id");
                                                rightTopicQuizChoiceIDs.add(choiceID);
                                            }

                                            // setting up quiz
                                            QuizHandler.getInstance(thisContext).getTopicQuizQuestions(topicID, new VolleyResponseListener() {
                                                @Override
                                                public void onError(String response) {
                                                    Log.e(TAG, "onError: getTopicQuizQuestions: " + response);
                                                }

                                                @Override
                                                public void onResponse(String response) {
                                                    if (btnTopicQuizNextModule.getText().toString().equals("Unlock Quiz and Exercises")) {
                                                        ModuleHandler.getInstance(thisContext).getModuleSummativeID(moduleID, new VolleyResponseListener() {
                                                            @Override
                                                            public void onError(String response) {
                                                                Log.e(TAG, "onError: getModuleSummativeID: " + response);
                                                            }

                                                            @Override
                                                            public void onResponse(String response) {
                                                                String summativeID = response;

                                                                QuizHandler.getInstance(thisContext).checkQuizID(summativeID, new VolleyResponseListener() {
                                                                    @Override
                                                                    public void onError(String response) {
                                                                        Log.e(TAG, "onError: checkQuizID: " + response);
                                                                    }

                                                                    @Override
                                                                    public void onResponse(String response) {
                                                                        if (response.equals("Success")) {
                                                                            btnTopicQuizNextModule.setVisibility(View.GONE);
                                                                        }
                                                                    }
                                                                });
                                                            }
                                                        });
                                                    }

                                                    Typeface tfOpenSansRegular = ResourcesCompat.getFont(thisContext, R.font.open_sans_regular);

                                                    lpMarginTop12dp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                                    lpMarginTop12dp.setMargins(0, 12, 0, 0);

                                                    lpMarginTop60dp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                                    lpMarginTop60dp.setMargins(0, 60, 0, 0);

                                                    try {
                                                        JSONArray arrayQuestions = new JSONArray(response);

                                                        for (int i = 0; i < arrayQuestions.length(); i ++) {
                                                            JSONObject objectQuestion = arrayQuestions.getJSONObject(i);

                                                            TextView tvQuestion = new TextView(thisContext);
                                                            tvQuestion.setLayoutParams(lpMarginTop60dp);
                                                            tvQuestion.setText(Integer.toString(i + 1) + ". " + objectQuestion.getString("question"));
                                                            tvQuestion.setTextColor(getResources().getColor(R.color.cb_on_background_black));
                                                            tvQuestion.setAlpha(0.87f);
                                                            tvQuestion.setTextSize(16);
                                                            tvQuestion.setTypeface(tfOpenSansRegular);

                                                            // =======

                                                            ImageView ivCodeImage = new ImageView(thisContext);
                                                            ivCodeImage.setLayoutParams(lpMarginTop12dp);

                                                            // =======

                                                            LinearLayout llChoices = new LinearLayout(thisContext);
                                                            llChoices.setOrientation(LinearLayout.VERTICAL);
                                                            llChoices.setLayoutParams(lpMarginTop12dp);

                                                            // =======

                                                            Glide.with(thisContext)
                                                                    .load(objectQuestion.getString("photo"))
                                                                    .into(ivCodeImage);

                                                            // =======

                                                            QuizHandler.getInstance(thisContext).getTopicQuizQuestionChoices(objectQuestion.getString("id"), new VolleyResponseListener() {
                                                                @Override
                                                                public void onError(String response) {
                                                                    Log.d(TAG, "onError: getTopicQuizQuestionChoices: " + response);
                                                                }

                                                                @Override
                                                                public void onResponse(String response) {
                                                                    try {
                                                                        JSONArray arrayChoices = new JSONArray(response);

                                                                        for (int j = 0; j < arrayChoices.length(); j ++) {
                                                                            JSONObject objectChoice = arrayChoices.getJSONObject(j);

                                                                            Button choice = new Button(thisContext);
                                                                            choice.setLayoutParams(lpMarginTop12dp);
                                                                            choice.setAllCaps(false);
                                                                            choice.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                                                                            choice.setPadding(42, 42, 42, 42);
                                                                            choice.setText(objectChoice.getString("choice"));
                                                                            choice.setId(j);

                                                                            if (chosenChoiceIDs.contains(objectChoice.getString("id"))) {
                                                                                choice.setTextColor(getResources().getColor(R.color.cb_on_surface_white));
                                                                                choice.setBackgroundResource(R.drawable.rounded_corners_background_black);
                                                                                chosenChoiceIDsPerContent.add(objectChoice.getString("id"));
                                                                            } else {
                                                                                choice.setTextColor(getResources().getColor(R.color.cb_surface));
                                                                                choice.setBackgroundResource(R.drawable.rounded_corners_border_gray);
                                                                            }

                                                                            llChoices.addView(choice);

                                                                            choice.setOnClickListener(new View.OnClickListener() {
                                                                                @Override
                                                                                public void onClick(View view) {
                                                                                    String prevChoice = "";
                                                                                    chosenChoice = "";
                                                                                    prevChoiceID = "";

                                                                                    disableChoices();
                                                                                    disableTopicQuizNavigationButtons();

                                                                                    // =====

                                                                                    for (int j = 0; j < arrayChoices.length(); j ++) {
                                                                                        View v = llChoices.getChildAt(j);

                                                                                        if (view instanceof Button) {
                                                                                            Button button = (Button) v;

                                                                                            if (Objects.equals(button.getBackground().getConstantState(), getResources().getDrawable(R.drawable.rounded_corners_background_black).getConstantState())) {
                                                                                                prevChoice = button.getText().toString();
                                                                                            }
                                                                                        }
                                                                                    }

                                                                                    for (int j = 0; j < arrayChoices.length(); j ++) {
                                                                                        View v = llChoices.getChildAt(j);

                                                                                        if (v instanceof Button) {
                                                                                            Button button = (Button) v;
                                                                                            button.setTextColor(getResources().getColor(R.color.cb_surface));
                                                                                            button.setLayoutParams(lpMarginTop12dp);
                                                                                            button.setBackgroundResource(R.drawable.rounded_corners_border_gray);
                                                                                        }
                                                                                    }

                                                                                    choice.setTextColor(getResources().getColor(R.color.white));
                                                                                    choice.setLayoutParams(lpMarginTop12dp);
                                                                                    choice.setBackgroundResource(R.drawable.rounded_corners_background_black);
                                                                                    chosenChoice = choice.getText().toString();

                                                                                    try {
                                                                                        if (!prevChoice.equals("")) {
                                                                                            prevChoiceID = QuizHandler.getInstance(thisContext).getTopicQuizChoiceID(prevChoice, objectQuestion.getString("id"));
                                                                                        }
                                                                                    } catch (JSONException e) {
                                                                                        e.printStackTrace();
                                                                                    }

                                                                                    try {
                                                                                        chosenChoiceID = QuizHandler.getInstance(thisContext).getTopicQuizChoiceID(chosenChoice, objectQuestion.getString("id"));

                                                                                        if (chosenChoiceIDs.contains(prevChoiceID)) {
                                                                                            chosenChoiceIDs.remove(prevChoiceID);
                                                                                            chosenChoiceIDs.add(chosenChoiceID);
                                                                                        } else {
                                                                                            chosenChoiceIDs.add(chosenChoiceID);
                                                                                        }

                                                                                        if (chosenChoiceIDsPerContent.contains(prevChoiceID)) {
                                                                                            chosenChoiceIDsPerContent.remove(prevChoiceID);
                                                                                            chosenChoiceIDsPerContent.add(chosenChoiceID);
                                                                                        } else {
                                                                                            chosenChoiceIDsPerContent.add(chosenChoiceID);
                                                                                        }

                                                                                        QuizHandler.getInstance(thisContext).checkTopicQuizAnswer(prevChoiceID, new VolleyResponseListener() {
                                                                                            @Override
                                                                                            public void onError(String response) {
                                                                                                Log.e(TAG, "onError: checkTopicQuizAnswer: " + response);
                                                                                            }

                                                                                            @Override
                                                                                            public void onResponse(String response) {
                                                                                                if (response.equals("Success")) {
                                                                                                    QuizHandler.getInstance(thisContext).updateTopicQuizAnswer(chosenChoiceID, prevChoiceID, new VolleyResponseListener() {
                                                                                                        @Override
                                                                                                        public void onError(String response) {
                                                                                                            Log.e(TAG, "onError: updateTopicQuizAnswer: " + response);
                                                                                                        }

                                                                                                        @Override
                                                                                                        public void onResponse(String response) {
                                                                                                            enableChoices();
                                                                                                            enableTopicQuizNavigationButtons();
                                                                                                        }
                                                                                                    });
                                                                                                } else if (response.equals("Failed")) {
                                                                                                    try {
                                                                                                        QuizHandler.getInstance(thisContext).addTopicQuizAnswer(objectQuestion.getString("id"), chosenChoiceID, new VolleyResponseListener() {
                                                                                                            @Override
                                                                                                            public void onError(String response) {
                                                                                                                Log.e(TAG, "onError: addTopicQuizAnswer: " + response);
                                                                                                            }

                                                                                                            @Override
                                                                                                            public void onResponse(String response) {
                                                                                                                enableChoices();
                                                                                                                enableTopicQuizNavigationButtons();
                                                                                                            }
                                                                                                        });
                                                                                                    } catch (JSONException e) {
                                                                                                        e.printStackTrace();
                                                                                                    }
                                                                                                }
                                                                                            }
                                                                                        });
                                                                                    } catch (JSONException e) {
                                                                                        e.printStackTrace();
                                                                                    }
                                                                                }
                                                                            });
                                                                        }

                                                                        llTopicQuizLoading.setVisibility(View.GONE);
                                                                    } catch (JSONException e) {
                                                                        e.printStackTrace();
                                                                    }
                                                                }
                                                            });

                                                            llTopicQuizQuiz.addView(tvQuestion);
                                                            llTopicQuizQuiz.addView(ivCodeImage);
                                                            llTopicQuizQuiz.addView(llChoices);
                                                        }
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            });
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void enableChoices() {
        for (int i = 0; i < llTopicQuizQuiz.getChildCount(); i++) {
            View v = llTopicQuizQuiz.getChildAt(i);

            if (v instanceof LinearLayout) {

                for (int j = 0; j < ((LinearLayout) v).getChildCount(); j ++) {
                    View v_ = ((LinearLayout) v).getChildAt(j);

                    if (v_ instanceof Button) {
                        v_.setClickable(true);
                    }
                }
            }
        }
    }

    private void disableChoices() {
        for (int i = 0; i < llTopicQuizQuiz.getChildCount(); i++) {
            View v = llTopicQuizQuiz.getChildAt(i);

            if (v instanceof LinearLayout) {

                for (int j = 0; j < ((LinearLayout) v).getChildCount(); j ++) {
                    View v_ = ((LinearLayout) v).getChildAt(j);

                    if (v_ instanceof Button) {
                        v_.setClickable(false);
                    }
                }
            }
        }
    }

    private void disableTopicQuizNavigationButtons() {
        btnTopicQuizPreviousModule.setClickable(false);
        btnTopicQuizNextModule.setClickable(false);
    }

    private void enableTopicQuizNavigationButtons() {
        btnTopicQuizPreviousModule.setClickable(true);
        btnTopicQuizNextModule.setClickable(true);
    }

    private void showExpressoDialog() {
        Dialog expressoDialog = new Dialog(thisContext);
        expressoDialog.setContentView(R.layout.alert_box_expresso);
        expressoDialog.setCancelable(false);
        expressoDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        TextView tvAbRobotTalkingMessage = (TextView) expressoDialog.findViewById(R.id.tv_ab_expresso_message);
        Button btnAbRobotTalkingProceed = (Button) expressoDialog.findViewById(R.id.btn_ab_expresso_proceed);

        btnAbRobotTalkingProceed.setVisibility(View.GONE);
        tvAbRobotTalkingMessage.setText("Please wait while I asses your performance.");

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                Generals.fadeOutView(tvAbRobotTalkingMessage);
                tvAbRobotTalkingMessage.setText("Yay! I’m done assessing your performance.\n" + "You can now take the quiz and exercises.");
                Generals.fadeInView(tvAbRobotTalkingMessage);
                btnAbRobotTalkingProceed.setVisibility(View.VISIBLE);
            }
        }, 3000);

        btnAbRobotTalkingProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(thisContext, Topics.class);
                intent.putExtra("FROM_ACTIVITY", "TopicQuiz");
                intent.putExtra("module_id", moduleID);
                startActivity(intent);
            }
        });

        expressoDialog.show();
    }
}