package com.example.expresso.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.expresso.R;
import com.example.expresso.interfaces.VolleyResponseListener;
import com.example.expresso.utils.Generals;
import com.example.expresso.utils.LogsHandler;
import com.example.expresso.utils.ModuleHandler;
import com.example.expresso.utils.QuizHandler;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.models.Shape;

public class PretestResults extends AppCompatActivity {
    // constants
    private static final String TAG = "PretestResults";
    private final Context thisContext = PretestResults.this;

    // views
    private TextView tvPretestResultsMessageText, tvPretestResultsEvaluationText, tvPretestResultsModule10Score, tvPretestResultsModule19Score, tvPretestResultsModule20Score;
    private LinearProgressIndicator lpiPretestResultsModule10, lpiPretestResultsModule19, lpiPretestResultsModule20;
    private Button btnPretestResultsProceed;
    private LinearLayout llPretestResultsCalculationLoading;

    // variables
    private String attemptID, quizScore, totalItems, startModuleName, quizID, moduleSlug;
    private int module10Score, module19Score, module20Score;
    private ArrayList<Integer> moduleIDs, doneModuleIDs;
    private ArrayList<String> module10QuestionIDs, module19QuestionIDs, module20QuestionIDs;
    private double passingScore, passingScorePerModule;
    private double passing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pretest_results);

        initAll();

        btnPretestResultsProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(thisContext, Home.class));
            }
        });
    }

    private void showViews() {
        btnPretestResultsProceed.setVisibility(View.VISIBLE);
        tvPretestResultsMessageText.setVisibility(View.VISIBLE);
        tvPretestResultsEvaluationText.setVisibility(View.VISIBLE);

        showConfetti();
    }

    private void hideViews() {
        btnPretestResultsProceed.setVisibility(View.GONE);
        tvPretestResultsMessageText.setVisibility(View.GONE);
        tvPretestResultsEvaluationText.setVisibility(View.GONE);
    }

    private void showConfetti() {
        KonfettiView kvConfetti = findViewById(R.id.kv_pretest_results_confetti);
        kvConfetti.build()
                .addColors(Color.YELLOW, Color.GREEN, Color.MAGENTA)
                .setSpeed(1f, 4f)
                .setDirection(0.0, 359.0)
                .setFadeOutEnabled(true)
                .setTimeToLive(2000L)
                .addShapes(Shape.Square.INSTANCE, Shape.Circle.INSTANCE)
                .addSizes(new nl.dionsegijn.konfetti.models.Size(8, 5f))
                .setPosition(-50f, kvConfetti.getWidth() + 50f, -50f, -50f)
                .streamFor(300, 2000L);
    }

    private void initAll() {
        btnPretestResultsProceed = findViewById(R.id.btn_pretest_results_proceed);
        tvPretestResultsMessageText = findViewById(R.id.tv_pretest_results_message_text);
        tvPretestResultsEvaluationText = findViewById(R.id.tv_pretest_results_evaluation_text);
        lpiPretestResultsModule10 = findViewById(R.id.lpi_pretest_results_module_10);
        lpiPretestResultsModule19 = findViewById(R.id.lpi_pretest_results_module_19);
        lpiPretestResultsModule20 = findViewById(R.id.lpi_pretest_results_module_20);
        tvPretestResultsModule10Score = findViewById(R.id.tv_pretest_results_module_10_score);
        tvPretestResultsModule19Score = findViewById(R.id.tv_pretest_results_module_19_score);
        tvPretestResultsModule20Score = findViewById(R.id.tv_pretest_results_module_20_score);
        llPretestResultsCalculationLoading = findViewById(R.id.ll_pretest_results_calculation_loading);

        // start

        llPretestResultsCalculationLoading.setVisibility(View.VISIBLE);
        hideViews();

        module10Score = 0;
        module19Score = 0;
        module20Score = 0;
        passingScore = 0;
        passingScorePerModule = 0;
        moduleIDs = new ArrayList<>();
        doneModuleIDs = new ArrayList<>();
        module10QuestionIDs = new ArrayList<>();
        module19QuestionIDs = new ArrayList<>();
        module20QuestionIDs = new ArrayList<>();

        Intent intent = getIntent();
        quizID = intent.getStringExtra("quiz_id");
        attemptID = intent.getStringExtra("attempt_id");
        totalItems = intent.getStringExtra("total_items");
        passing = Double.parseDouble(intent.getStringExtra("passing"));
        moduleSlug = intent.getStringExtra("module_slug");
        quizScore = "";

        QuizHandler.getInstance(thisContext).getQuestionQuizIDs(new VolleyResponseListener() {
            @Override
            public void onError(String response) {
                Log.e(TAG, "onError: getQuestionQuizIDs: " + response);
            }

            @Override
            public void onResponse(String response) {
                try {
                    JSONArray arrayQuestionQuizIDs = new JSONArray(response);

                    for (int i = 0; i < arrayQuestionQuizIDs.length(); i ++) {

                        JSONObject objectQuestionQuizID = arrayQuestionQuizIDs.getJSONObject(i);

                        String questionModuleID = objectQuestionQuizID.getString("module_id");
                        String questionID = objectQuestionQuizID.getString("question_id");

                        switch (questionModuleID) {
                            case "10":
                                module10QuestionIDs.add(questionID);
                                break;
                            case "19":
                                module19QuestionIDs.add(questionID);
                                break;
                            case "20":
                                module20QuestionIDs.add(questionID);
                                break;
                        }
                    }

                    setValues();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void setValues() {
        QuizHandler.getInstance(thisContext).getRightQuizAnswers(quizID, attemptID, new VolleyResponseListener() {
            @Override
            public void onError(String response) {
                Log.e(TAG, "onError: getRightQuizAnswers: " + response);
            }

            @Override
            public void onResponse(String response) {
                try {
                    JSONArray array = new JSONArray(response);
                    quizScore = Integer.toString(array.length());

                    for (int i = 0; i < array.length(); i ++) {
                        JSONObject object = array.getJSONObject(i);

                        String questionModuleID = object.getString("question_id");

                        if (module10QuestionIDs.contains(questionModuleID)) {
                            module10Score += 1;
                        } else if (module19QuestionIDs.contains(questionModuleID)) {
                            module19Score += 1;
                        } else if (module20QuestionIDs.contains(questionModuleID)) {
                            module20Score += 1;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                passingScorePerModule = ((Integer.parseInt(totalItems) / 3) * passing);

                if (module10Score >= passingScorePerModule) {
                    moduleIDs.add(10);
                    doneModuleIDs.add(10);
                    startModuleName = "Java Fundamentals";

                    if (module19Score >= passingScorePerModule) {
                        doneModuleIDs.add(19);
                        moduleIDs.add(19);
                        startModuleName = "OOP Methods";

                        if (module20Score >= passingScorePerModule) {
                            moduleIDs.add(20);
                            startModuleName = "Creating Own Class";
                        } else {
                            moduleIDs.add(20);
                            startModuleName = "Creating Own Class";
                        }
                    } else {
                        moduleIDs.add(19);
                        startModuleName = "OOP Methods";
                    }
                } else {
                    moduleIDs.add(10);
                    startModuleName = "Java Fundamentals";
                }

                insertUserModules(moduleIDs);
            }
        });
    }

    private void insertUserModules(ArrayList<Integer> moduleIDs) {
        for (int i = 0; i < moduleIDs.size(); i ++) {
            int counter = i;
            ModuleHandler.getInstance(thisContext).addModule(Integer.toString(moduleIDs.get(i)), new VolleyResponseListener() {
                @Override
                public void onError(String response) {
                    Log.e(TAG, "onError: addModule: " + response);
                }

                @Override
                public void onResponse(String response) {
                    if (counter == moduleIDs.size() - 1) {
                        if (doneModuleIDs.size() != 0) {
                            for (int i = 0; i < doneModuleIDs.size(); i ++) {
                                int counter = i;

                                ModuleHandler.getInstance(thisContext).updateModuleDone(String.valueOf(doneModuleIDs.get(i)), new VolleyResponseListener() {
                                    @Override
                                    public void onError(String response) {
                                        Log.e(TAG, "onError: updateModuleDone: " + response);
                                    }

                                    @Override
                                    public void onResponse(String response) {
                                        if (counter == doneModuleIDs.size() - 1) {
                                            updateRemarks();
                                        }
                                    }
                                });
                            }
                        } else {
                            updateRemarks();
                        }
                    }
                }
            });
        }
    }

    private void updateRemarks() {
        String remarks;

        passingScore = (Integer.parseInt(totalItems) * passing);

        if (Integer.parseInt(quizScore) >= passingScore) {
            remarks = "passed";
        } else {
            remarks = "failed";
        };

        QuizHandler.getInstance(thisContext).updateQuizAttemptInfo(remarks, quizScore, "0", attemptID, new VolleyResponseListener() {
            @Override
            public void onError(String response) {
                Log.e(TAG, "onError: updateQuizAttemptInfo: " + response);
            }

            @Override
            public void onResponse(String response) {
                lpiPretestResultsModule10.setProgress(module10Score * 10);
                lpiPretestResultsModule19.setProgress(module19Score * 10);
                lpiPretestResultsModule20.setProgress(module20Score * 10);

                tvPretestResultsModule10Score.setText(Integer.toString(module10Score) + "/" + String.valueOf(Integer.parseInt(totalItems)/3));
                tvPretestResultsModule19Score.setText(Integer.toString(module19Score) + "/" + String.valueOf(Integer.parseInt(totalItems)/3));
                tvPretestResultsModule20Score.setText(Integer.toString(module20Score) + "/" + String.valueOf(Integer.parseInt(totalItems)/3));

                tvPretestResultsMessageText.setText("We evaluated your score");
                tvPretestResultsEvaluationText.setText("Great job! \uD83C\uDF89 I have evaluated your current knowledge. Based on your pretest results, you’ll start at module " + startModuleName + ".\n" + "Click CONTINUE to see your modules and start studying \uD83D\uDCDA. Good luck and enjoy! \uD83E\uDD73");
                llPretestResultsCalculationLoading.setVisibility(View.GONE);
                showViews();

                QuizHandler.getInstance(thisContext).logFinishedQuiz(moduleSlug);
            }
        });
    }
}