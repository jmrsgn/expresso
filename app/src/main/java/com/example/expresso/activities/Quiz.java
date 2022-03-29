package com.example.expresso.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;

import com.bumptech.glide.Glide;
import com.example.expresso.R;
import com.example.expresso.interfaces.VolleyResponseListener;
import com.example.expresso.models.QuizChoice;
import com.example.expresso.models.QuizItem;
import com.example.expresso.utils.Constants;
import com.example.expresso.utils.Generals;
import com.example.expresso.utils.QuizHandler;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;

public class Quiz extends AppCompatActivity {
    // constants
    private static final String TAG = "Quiz";
    private final Context thisContext = Quiz.this;

    // views
    private Toolbar tbQuizToolbar;
    private TextView tvQuizQuestionTitle, tvQuizQuestion, tvQuizQuestionCounter;
    private Button btnQuizNextQuestion, btnQuizPreviousQuestion, btnQuizSubmitQuiz;
    private LinearLayout llQuizChoices, llQuizItemNavigation, llQuizLoading;
    private LinearLayout.LayoutParams lpQuizChoicesDetails;
    private ImageView ivQuizCodeImage;
    private RelativeLayout rlQuizButtons;

    // variables
    private ArrayList<String> chosenChoiceIDs, doneQuestionNumbers, generatedQuestionIDs;
    private ArrayList<QuizChoice> allQuizChoices;
    private ArrayList<QuizItem> quizItems;
    private String quizID, quizModuleID, quizTitle, passing, items, prevChoice, prevChoiceID, chosenChoiceID, chosenChoice, attempt, attemptID, userQuizID, moduleSlug;
    private Intent intent;
    private SharedPreferences spModulesInfo;
    private Gson gson;
    private int questionIndex = 0;

    @Override
    protected void onStop() {
        super.onStop();

//        if (llQuizLoading.getVisibility() == View.VISIBLE) {
//            QuizHandler.getInstance(thisContext).deleteGeneratedQuestions(attemptID, new VolleyResponseListener() {
//                @Override
//                public void onError(String response) {
//                    Log.e(TAG, "onError: deleteGeneratedQuestions: " + response);
//                }
//
//                @Override
//                public void onResponse(String response) {
//                    startActivity(new Intent(thisContext, Home.class));
//                }
//            });
//        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        initAll();

        btnQuizPreviousQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                questionIndex -= 1;
                reset();
            }
        });

        btnQuizNextQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                questionIndex += 1;
                reset();
            }
        });

        btnQuizSubmitQuiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int notDoneQuestions = 0;

                for (int i = 0; i < quizItems.size(); i ++) {
                    View v = llQuizItemNavigation.getChildAt(i);

                    if (v instanceof TextView) {
                        TextView tvItem = (TextView) v;

                        if (tvItem.getCurrentTextColor() == getResources().getColor(R.color.cb_surface)) {
                            notDoneQuestions += 1;
                        }
                    }
                }

                if (notDoneQuestions > 0) {
                    showUnansweredQuestionsDialog("There are " + String.valueOf(notDoneQuestions) + " unanswered questions");
                } else {
                    goToResults();
                }
            }
        });
    }

    private void showUnansweredQuestionsDialog(String message) {
        Dialog unansweredQuestionsDialog = new Dialog(thisContext);
        unansweredQuestionsDialog.setContentView(R.layout.alert_box_unanswered_questions);

        TextView tvAbUnansweredQuestionsMessage = (TextView) unansweredQuestionsDialog.findViewById(R.id.tv_ab_unanswered_questions_message);
        Button btnAbUnAnsweredQuestionsCancel = (Button) unansweredQuestionsDialog.findViewById(R.id.btn_ab_unanswered_questions_cancel);
        Button btnAbUnAnsweredQuestionsProceed = (Button) unansweredQuestionsDialog.findViewById(R.id.btn_ab_unanswered_questions_proceed);

        tvAbUnansweredQuestionsMessage.setText(message + ", do you still want to continue?");

        btnAbUnAnsweredQuestionsCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                unansweredQuestionsDialog.dismiss();
            }
        });

        btnAbUnAnsweredQuestionsProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToResults();
            }
        });

        unansweredQuestionsDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        unansweredQuestionsDialog.show();
    }

    private void goToResults() {
        if (quizID.equals("1")) {
            Intent intent = new Intent(thisContext, PretestResults.class);
            intent.putExtra("quiz_id", quizID);
            intent.putExtra("attempt_id", attemptID);
            intent.putExtra("total_items", String.valueOf(quizItems.size()));
            intent.putExtra("passing", passing);
            intent.putExtra("module_slug", moduleSlug);
            startActivity(intent);
        } else {
            Intent intent = new Intent(thisContext, SummativeResults.class);
            intent.putExtra("quiz_id", quizID);
            intent.putExtra("module_id", quizModuleID);
            intent.putExtra("attempt_id", attemptID);
            intent.putExtra("total_items", String.valueOf(quizItems.size()));
            intent.putExtra("passing", passing);
            intent.putExtra("module_slug", moduleSlug);
            startActivity(intent);
        }

        finish();
    }

    private void reset() {
        chosenChoice = "";
        displayQuizQuestions(quizItems);
        llQuizChoices.removeAllViews();
    }

    private void displayQuizQuestions(ArrayList<QuizItem> quizItems) {
        rlQuizButtons.setVisibility(View.VISIBLE);
        llQuizLoading.setVisibility(View.GONE);
        disableQuizNavigationButtons();

        if (questionIndex == 0) {
            btnQuizPreviousQuestion.setVisibility(View.GONE);
        } else if (questionIndex == quizItems.size() - 1){
            btnQuizNextQuestion.setVisibility(View.GONE);
        } else {
            btnQuizPreviousQuestion.setVisibility(View.VISIBLE);
            btnQuizNextQuestion.setVisibility(View.VISIBLE);
        }

        Glide.with(thisContext)
                .load(quizItems.get(questionIndex).getPhoto())
                .into(ivQuizCodeImage);

        tvQuizQuestionTitle.setText("Question " + Integer.toString(questionIndex + 1));
        tvQuizQuestion.setText(quizItems.get(questionIndex).getQuestion());

        QuizHandler.getInstance(thisContext).getQuizQuestionChoices(quizItems.get(questionIndex).getId(), new VolleyResponseListener() {
            @Override
            public void onError(String response) {
                Log.e(TAG, "onError: getQuizQuestionChoices: " + response);
            }

            @Override
            public void onResponse(String response) {
                llQuizChoices = findViewById(R.id.ll_quiz_choices);

                try {
                    JSONArray arrayChoices = new JSONArray(response);

                    lpQuizChoicesDetails = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    lpQuizChoicesDetails.setMargins(0, 12, 0, 0);

                    for (int i = 0; i < arrayChoices.length(); i ++) {
                        JSONObject objectChoice = arrayChoices.getJSONObject(i);

                        Button choice = new Button(thisContext);
                        choice.setLayoutParams(lpQuizChoicesDetails);
                        choice.setAllCaps(false);
                        choice.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                        choice.setPadding(42, 42, 42, 42);
                        choice.setText(objectChoice.getString("choice"));
                        choice.setId(i);

                        if (chosenChoiceIDs.contains(objectChoice.getString("id"))) {
                            choice.setTextColor(getResources().getColor(R.color.cb_on_surface_white));
                            choice.setBackgroundResource(R.drawable.rounded_corners_background_black);
                        } else {
                            choice.setTextColor(getResources().getColor(R.color.cb_surface));
                            choice.setBackgroundResource(R.drawable.rounded_corners_border_gray);
                        }

                        llQuizChoices.addView(choice);

                        choice.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                View tvView = llQuizItemNavigation.getChildAt(questionIndex);

                                if (tvView instanceof TextView) {
                                    TextView tvItem = (TextView) tvView;
                                    tvItem.setTextColor(getResources().getColor(R.color.cb_primary_purple));
                                }

                                disableQuizNavigationButtons();
                                prevChoice = "";
                                prevChoiceID = "";

                                for (int j = 0; j < arrayChoices.length(); j ++) {
                                    View vv = llQuizChoices.getChildAt(j);

                                    if (v instanceof Button) {
                                        Button button = (Button) vv;

                                        if (Objects.equals(button.getBackground().getConstantState(), getResources().getDrawable(R.drawable.rounded_corners_background_black).getConstantState())) {
                                            prevChoice = button.getText().toString();
                                        }
                                    }
                                }

                                for (int j = 0; j < arrayChoices.length(); j ++) {
                                    chosenChoice = "";
                                    chosenChoiceID = "";
                                    View view = llQuizChoices.getChildAt(j);

                                    if (view instanceof Button) {
                                        Button button = (Button) view;
                                        button.setTextColor(getResources().getColor(R.color.cb_surface));
                                        button.setLayoutParams(lpQuizChoicesDetails);
                                        button.setBackgroundResource(R.drawable.rounded_corners_border_gray);
                                    }
                                }

                                choice.setTextColor(getResources().getColor(R.color.white));
                                choice.setLayoutParams(lpQuizChoicesDetails);
                                choice.setBackgroundResource(R.drawable.rounded_corners_background_black);
                                chosenChoice = choice.getText().toString();

                                if (!prevChoice.equals("")) {
                                    prevChoiceID = QuizHandler.getInstance(thisContext).getQuizChoiceID(prevChoice, quizItems.get(questionIndex).getId());
                                }

                                chosenChoiceID = QuizHandler.getInstance(thisContext).getQuizChoiceID(chosenChoice, quizItems.get(questionIndex).getId());

                                if (chosenChoiceIDs.contains(prevChoiceID)) {
                                    chosenChoiceIDs.remove(prevChoiceID);
                                    chosenChoiceIDs.add(chosenChoiceID);
                                } else {
                                    chosenChoiceIDs.add(chosenChoiceID);
                                }

                                if (!doneQuestionNumbers.contains(String.valueOf(questionIndex + 1))) {
                                    doneQuestionNumbers.add(String.valueOf(questionIndex + 1));
                                }

                                QuizHandler.getInstance(thisContext).updateQuizAnswer(quizItems.get(questionIndex).getId(), chosenChoiceID, attemptID, new VolleyResponseListener() {
                                    @Override
                                    public void onError(String response) {
                                        Log.e(TAG, "onError: updateQuizAnswer: " + response);
                                    }

                                    @Override
                                    public void onResponse(String response) {
                                        if (response.equals("Success")) {
                                            enableQuizNavigationButtons();
                                        } else {
                                            Generals.makeToast(thisContext, "Something went wrong...");
                                        }
                                    }
                                });
                            }
                        });
                    }

                    enableQuizNavigationButtons();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void disableQuizNavigationButtons() {
        btnQuizPreviousQuestion.setClickable(false);
        btnQuizNextQuestion.setClickable(false);
        btnQuizSubmitQuiz.setClickable(false);
    }

    private void enableQuizNavigationButtons() {
        btnQuizPreviousQuestion.setClickable(true);
        btnQuizNextQuestion.setClickable(true);
        btnQuizSubmitQuiz.setClickable(true);
    }

    private void initAll() {
        llQuizLoading = findViewById(R.id.ll_quiz_loading);
        llQuizLoading.setVisibility(View.VISIBLE);

        // variables
        intent = getIntent();
        quizID = intent.getStringExtra("quiz_id");
        quizModuleID = intent.getStringExtra("quiz_module_id");
        quizTitle = intent.getStringExtra("quiz_title");
        passing = intent.getStringExtra("passing");
        items = intent.getStringExtra("items");
        attempt = intent.getStringExtra("user_max_quiz_attempt");
        userQuizID = intent.getStringExtra("user_quiz_id");
        quizItems = new ArrayList<>();
        chosenChoiceIDs = new ArrayList<>();
        doneQuestionNumbers = new ArrayList<>();
        generatedQuestionIDs = new ArrayList<>();
        allQuizChoices = new ArrayList<>();
        spModulesInfo = getSharedPreferences(Constants.SP_MODULES_INFO, MODE_PRIVATE);
        gson = new Gson();

        // views
        tbQuizToolbar = findViewById(R.id.mtb_quiz_toolbar);
        tvQuizQuestionTitle = findViewById(R.id.tv_quiz_question_title);
        tvQuizQuestionCounter = findViewById(R.id.tv_quiz_question_counter);
        tvQuizQuestion = findViewById(R.id.tv_quiz_question);
        btnQuizNextQuestion = findViewById(R.id.btn_quiz_next_question);
        btnQuizPreviousQuestion = findViewById(R.id.btn_quiz_previous_question);
        btnQuizSubmitQuiz = findViewById(R.id.btn_quiz_submit_quiz);
        ivQuizCodeImage = findViewById(R.id.iv_quiz_code_image);
        rlQuizButtons = findViewById(R.id.rl_quiz_buttons);

        tvQuizQuestion.setText("");
        tvQuizQuestionTitle.setText("");
        tbQuizToolbar.setTitle(quizTitle);

        if (quizID.equals("1")) {
            moduleSlug = "pretest";
        } else {
            String moduleInfoJson = spModulesInfo.getString(quizModuleID, null);
            String[] moduleInfo = gson.fromJson(moduleInfoJson, String[].class);
            moduleSlug = moduleInfo[2];
        }

        QuizHandler.getInstance(thisContext).getAllQuizChoices(new VolleyResponseListener() {
            @Override
            public void onError(String response) {
                Log.e(TAG, "onError: getAllQuizChoices: " + response);
            }

            @Override
            public void onResponse(String response) {
                try {
                    JSONArray array = new JSONArray(response);

                    for (int i = 0; i < array.length(); i ++) {
                        JSONObject object = array.getJSONObject(i);

                        allQuizChoices.add(new QuizChoice(object.getString("id"),
                                object.getString("choice"),
                                object.getString("is_correct"),
                                object.getString("question_id")));
                    }

                    QuizHandler.getInstance(thisContext).saveQuizChoicesToSP(allQuizChoices);

                    // ====

                    QuizHandler.getInstance(thisContext).getQuizAttemptID(userQuizID, attempt, new VolleyResponseListener() {
                        @Override
                        public void onError(String response) {
                            Log.e(TAG, "onError: getQuizAttemptID: " + response);
                        }

                        @Override
                        public void onResponse(String response) {
                            attemptID = response;

                            QuizHandler.getInstance(thisContext).getUserQuizQuestions(attemptID, new VolleyResponseListener() {
                                @Override
                                public void onError(String response) {
                                    Log.e(TAG, "onError: getUserQuizQuestions: " + response);
                                }

                                @Override
                                public void onResponse(String response) {
                                    if (response.equals("[]") || response.equals("") || response == null) {
                                        if (quizID.equals("1")) {
                                            // pretest
                                            QuizHandler.getInstance(thisContext).getRandomPretestQuestions(new VolleyResponseListener() {
                                                @Override
                                                public void onError(String response) {
                                                    Log.e(TAG, "onError: getRandomPretestQuestions: " + response);
                                                }

                                                @Override
                                                public void onResponse(String response) {
                                                    int itemsPerModule = Integer.parseInt(items) / 3;

                                                    int module10Counter = 0;
                                                    int module19Counter = 0;
                                                    int module20Counter = 0;

                                                    try {
                                                        JSONArray array = new JSONArray(response);

                                                        for (int i = 0; i < array.length(); i ++) {
                                                            JSONObject object = array.getJSONObject(i);
                                                            String questionModuleID = object.getString("module_id");
                                                            String questionID = object.getString("question_id");
                                                            String questionQuestion = object.getString("question");
                                                            String questionPhoto = object.getString("photo");
                                                            String questionScore = object.getString("score");
                                                            String questionQuizID = object.getString("quiz_id");

                                                            if (object.getString("module_id").equals("10") && module10Counter < itemsPerModule) {

                                                                generatedQuestionIDs.add(questionID);
                                                                quizItems.add(new QuizItem(questionID, questionQuestion, questionPhoto, questionScore, questionQuizID));
                                                                module10Counter += 1;
                                                            } else if (object.getString("module_id").equals("19") && module19Counter < itemsPerModule) {

                                                                generatedQuestionIDs.add(questionID);
                                                                quizItems.add(new QuizItem(questionID, questionQuestion, questionPhoto, questionScore, questionQuizID));
                                                                module19Counter += 1;
                                                            } else if (object.getString("module_id").equals("20") && module20Counter < itemsPerModule) {

                                                                generatedQuestionIDs.add(questionID);
                                                                quizItems.add(new QuizItem(questionID, questionQuestion, questionPhoto, questionScore, questionQuizID));
                                                                module20Counter += 1;
                                                            }
                                                        }

                                                        Collections.sort(quizItems, new Comparator<QuizItem>(){
                                                            public int compare(QuizItem qi1, QuizItem qi2) {
                                                                return qi1.getId().compareToIgnoreCase(qi2.getId());
                                                            }
                                                        });

                                                        setUpItemNavigation(quizItems);
                                                        addGeneratedQuizQuestions(quizItems, generatedQuestionIDs);
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            });
                                        } else {
                                            // quiz
                                            QuizHandler.getInstance(thisContext).getRandomQuizQuestions(quizID, items, new VolleyResponseListener() {
                                                @Override
                                                public void onError(String response) {
                                                    Log.e(TAG, "onError: getRandomQuizQuestions: " + response);
                                                }

                                                @Override
                                                public void onResponse(String response) {
                                                    try {
                                                        JSONArray arrayQuizQuestions = new JSONArray(response);

                                                        for (int i = 0; i < arrayQuizQuestions.length(); i++) {
                                                            JSONObject objectQuizQuestion = arrayQuizQuestions.getJSONObject(i);

                                                            String questionID = objectQuizQuestion.getString("id");
                                                            String questionQuestion = objectQuizQuestion.getString("question");
                                                            String questionPhoto = objectQuizQuestion.getString("photo");
                                                            String questionScore = objectQuizQuestion.getString("score");
                                                            String questionQuizID = objectQuizQuestion.getString("quiz_id");

                                                            generatedQuestionIDs.add(questionID);
                                                            quizItems.add(new QuizItem(questionID, questionQuestion, questionPhoto, questionScore, questionQuizID));
                                                        }

                                                        Collections.sort(quizItems, new Comparator<QuizItem>(){
                                                            public int compare(QuizItem qi1, QuizItem qi2) {
                                                                return qi1.getId().compareToIgnoreCase(qi2.getId());
                                                            }
                                                        });

                                                        setUpItemNavigation(quizItems);
                                                        addGeneratedQuizQuestions(quizItems, generatedQuestionIDs);
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            });
                                        }
                                    } else {
                                        QuizHandler.getInstance(thisContext).getQuizQuestions(attemptID, new VolleyResponseListener() {
                                            @Override
                                            public void onError(String response) {
                                                Log.e(TAG, "onError: getQuizQuestions: " + response);
                                            }

                                            @Override
                                            public void onResponse(String response) {
                                                try {
                                                    JSONArray arrayQuestions = new JSONArray(response);
//                                                    tvQuizQuestionCounter.setVisibility(View.VISIBLE);
//                                                    tvQuizQuestionCounter.setText("Resuming Quiz...");

                                                    for (int i = 0; i < arrayQuestions.length(); i ++) {
                                                        JSONObject objectQuestion = arrayQuestions.getJSONObject(i);

                                                        String questionQuestionID = objectQuestion.getString("question_id");
                                                        String questionID = objectQuestion.getString("id");
                                                        String questionQuestion = objectQuestion.getString("question");
                                                        String questionPhoto = objectQuestion.getString("photo");
                                                        String questionScore = objectQuestion.getString("score");
                                                        String questionQuizID = objectQuestion.getString("quiz_id");

                                                        quizItems.add(new QuizItem(questionID, questionQuestion, questionPhoto, questionScore, questionQuizID));
                                                    }

//                                                    tvQuizQuestionCounter.setVisibility(View.GONE);

                                                    Collections.sort(quizItems, new Comparator<QuizItem>(){
                                                        public int compare(QuizItem qi1, QuizItem qi2) {
                                                            return qi1.getId().compareToIgnoreCase(qi2.getId());
                                                        }
                                                    });

                                                    QuizHandler.getInstance(thisContext).getQuizAnsweredChoiceIDs(attemptID, new VolleyResponseListener() {
                                                        @Override
                                                        public void onError(String response) {
                                                            Log.e(TAG, "onError: getQuizAnsweredChoiceIDs: " + response);
                                                        }

                                                        @Override
                                                        public void onResponse(String response) {
                                                            try {
                                                                JSONArray arrayChoiceIDs = new JSONArray(response);

                                                                for (int i = 0; i < arrayChoiceIDs.length(); i ++) {
                                                                    JSONObject objectChoiceID = arrayChoiceIDs.getJSONObject(i);
                                                                    chosenChoiceIDs.add(objectChoiceID.getString("choice_id"));
                                                                }

                                                                QuizHandler.getInstance(thisContext).getCurrentQuizNumber(attemptID, new VolleyResponseListener() {
                                                                    @Override
                                                                    public void onError(String response) {
                                                                        Log.e(TAG, "onError: getCurrentQuizNumber: " + response);
                                                                    }

                                                                    @Override
                                                                    public void onResponse(String response) {
                                                                        if (response.equals("") || Integer.parseInt(response) == 0) {
                                                                            questionIndex = 0;
                                                                        } else {
                                                                            questionIndex = Integer.parseInt(response) - 1;
                                                                        }

                                                                        QuizHandler.getInstance(thisContext).getQuizAnsweredQuestionIDs(attemptID, new VolleyResponseListener() {
                                                                            @Override
                                                                            public void onError(String response) {
                                                                                Log.e(TAG, "onError: getQuizAnsweredQuestionIDs: " + response);
                                                                            }

                                                                            @Override
                                                                            public void onResponse(String response) {
                                                                                try {
                                                                                    JSONArray arrayQuestionIDs = new JSONArray(response);

                                                                                    for (int i = 0; i < arrayQuestionIDs.length(); i ++) {
                                                                                        JSONObject objectQuestionID = arrayQuestionIDs.getJSONObject(i);

                                                                                        doneQuestionNumbers.add(objectQuestionID.getString("question_id"));
                                                                                    }

                                                                                    setUpItemNavigation(quizItems);
                                                                                    displayQuizQuestions(quizItems);

                                                                                    QuizHandler.getInstance(thisContext).logStartQuiz(moduleSlug);
                                                                                } catch (JSONException e) {
                                                                                    e.printStackTrace();
                                                                                }
                                                                            }
                                                                        });
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
                                }
                            });
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        // listeners
        tbQuizToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (llQuizLoading.getVisibility() == View.GONE) {
                    QuizHandler.getInstance(thisContext).updateQuizCurrentNumber(attemptID, Integer.toString(questionIndex + 1), new VolleyResponseListener() {
                        @Override
                        public void onError(String response) {
                            Log.e(TAG, "onError: updateQuizCurrentNumber" + response);
                        }

                        @Override
                        public void onResponse(String response) {
                            Intent intent;

                            if (quizID.equals("1")) {
                                intent = new Intent(thisContext, Home.class);
                            } else {
                                intent = new Intent(thisContext, Topics.class);
                                intent.putExtra("module_id", quizModuleID);
                                intent.putExtra("FROM_ACTIVITY", "Quiz");
                            }

                            quizItems.clear();
                            startActivity(intent);
                        }
                    });
                }
            }
        });
    }

    private void addGeneratedQuizQuestions(ArrayList<QuizItem> quizItems, ArrayList<String> generatedQuestionIDs) {
        tvQuizQuestionCounter.setVisibility(View.VISIBLE);

        for (int i = 0; i < generatedQuestionIDs.size(); i ++) {

            int counter = i;
            QuizHandler.getInstance(thisContext).addGeneratedQuizQuestion(quizID, generatedQuestionIDs.get(i), attemptID, new VolleyResponseListener() {
                @Override
                public void onError(String response) {
                    Log.e(TAG, "onError: addGeneratedQuizQuestion: " + response);
                }

                @Override
                public void onResponse(String response) {
                    tvQuizQuestionCounter.setText("Preparing question " + String.valueOf(counter + 1) + "/" + String.valueOf(generatedQuestionIDs.size() + ", please wait..."));

                    if (counter == generatedQuestionIDs.size() - 1) {
                        displayQuizQuestions(quizItems);

                        QuizHandler.getInstance(thisContext).logStartQuiz(moduleSlug);
                    }
                }
            });
        }
    }

    private void setUpItemNavigation(ArrayList<QuizItem> quizItems) {
        llQuizItemNavigation = findViewById(R.id.ll_quiz_item_navigation);
        Typeface tfOpenSansRegular = ResourcesCompat.getFont(thisContext, R.font.open_sans_regular);

        for (int i = 0; i < quizItems.size(); i ++) {
            TextView tvItem = new TextView(thisContext);
            tvItem.setText("Question #" + Integer.toString(i + 1));
            tvItem.setPadding(6, 6, 6, 6);
            tvItem.setTextSize(18);
            tvItem.setTypeface(tfOpenSansRegular);
            tvItem.setAlpha(0.6f);

            if (doneQuestionNumbers.contains(quizItems.get(i).getId())) {
                tvItem.setTextColor(getResources().getColor(R.color.cb_primary_purple));
            } else {
                tvItem.setTextColor(getResources().getColor(R.color.cb_surface));
            }

            tvItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int hashtagIndex = tvItem.getText().toString().indexOf("#");
                    questionIndex = Integer.parseInt(tvItem.getText().toString().substring(hashtagIndex + 1)) - 1;

                    reset();
                }
            });

            llQuizItemNavigation.addView(tvItem);
        }
    }
}