package com.example.expresso.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.expresso.R;
import com.example.expresso.interfaces.VolleyResponseListener;
import com.example.expresso.utils.ExerciseHandler;
import com.example.expresso.utils.Generals;
import com.example.expresso.utils.LogsHandler;
import com.example.expresso.utils.ModuleHandler;
import com.example.expresso.utils.QuizHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.models.Shape;

public class SummativeResults extends AppCompatActivity {
    // constants
    private static final String TAG = "SummativeResults";
    private final Context thisContext = SummativeResults.this;

    // views
    private Button btnQuizResultsProceed;
    private TextView tvQuizResultsEvaluationText, tvQuizResultsScoreText;
    private LinearLayout llQuizResultsCalculationLoading;

    // variables
    private String quizScore, attemptID, totalItems, moduleID, quizID, moduleSlug;
    private double passing;
    private double passingScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summative_results);

        initAll();

        btnQuizResultsProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent;
                intent = new Intent(thisContext, Topics.class);
                intent.putExtra("module_id", moduleID);
                intent.putExtra("FROM_ACTIVITY", "SummativeResults");
                startActivity(intent);
            }
        });
    }

    private void showViews() {
        btnQuizResultsProceed.setVisibility(View.VISIBLE);
        tvQuizResultsEvaluationText.setVisibility(View.VISIBLE);
        tvQuizResultsScoreText.setVisibility(View.VISIBLE);

        showConfetti();
    }

    private void hideViews() {
        btnQuizResultsProceed.setVisibility(View.GONE);
        tvQuizResultsEvaluationText.setVisibility(View.GONE);
        tvQuizResultsScoreText.setVisibility(View.GONE);
    }

    private void showConfetti() {
        KonfettiView kvConfetti = findViewById(R.id.kv_quiz_results_confetti);
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
        btnQuizResultsProceed = findViewById(R.id.btn_quiz_results_proceed);
        tvQuizResultsEvaluationText = findViewById(R.id.tv_quiz_results_evaluation_text);
        tvQuizResultsScoreText = findViewById(R.id.tv_quiz_results_score_text);
        llQuizResultsCalculationLoading = findViewById(R.id.ll_quiz_results_calculation_loading);

        llQuizResultsCalculationLoading.setVisibility(View.VISIBLE);
        hideViews();

        Intent intent = getIntent();
        quizID = intent.getStringExtra("quiz_id");
        attemptID = intent.getStringExtra("attempt_id");
        moduleID = intent.getStringExtra("module_id");
        totalItems = intent.getStringExtra("total_items");
        passing = Double.parseDouble(intent.getStringExtra("passing"));
        moduleSlug = intent.getStringExtra("module_slug");

        quizScore = "";
        passingScore = 0;

        setValues();
    }

    private void setValues() {
        QuizHandler.getInstance(thisContext).getRightQuizAnswers(quizID, attemptID, new VolleyResponseListener() {
            @Override
            public void onError(String response) {
                Log.e(TAG, "onError: getRightQuizAnswers + " + response);
            }

            @Override
            public void onResponse(String response) {
                try {
                    JSONArray array = new JSONArray(response);
                    quizScore = Integer.toString(array.length());
                    passingScore = (Integer.parseInt(totalItems) * passing);

                    String remarks;

                    if (Integer.parseInt(quizScore) >= passingScore) {
                        remarks = "passed";
                        tvQuizResultsEvaluationText.setText("You passed the quiz! Congratulations and keep up the good work! You can now proceed on answering the exercises I have provided to you.");
                    } else {
                        tvQuizResultsEvaluationText.setText("Everyone fails at some point. Others maybe at the same position as you but don’t worry, you’ll get through this! To help you pass the quiz, I have provided you a new video material. You may check it now so you can start studying again. Good luck!");
                        remarks = "failed";
                    }

                    tvQuizResultsScoreText.setText(quizScore + "/" + totalItems);

                    QuizHandler.getInstance(thisContext).updateQuizAttemptInfo(remarks, quizScore, "0", attemptID, new VolleyResponseListener() {
                        @Override
                        public void onError(String response) {
                            Log.e(TAG, "onError: updateQuizAttemptInfo: " + response);
                        }

                        @Override
                        public void onResponse(String response) {
                            llQuizResultsCalculationLoading.setVisibility(View.GONE);
                            showViews();

                            QuizHandler.getInstance(thisContext).logFinishedQuiz(moduleSlug);

                            ExerciseHandler.getUserExercises(thisContext, moduleID, new VolleyResponseListener() {
                                @Override
                                public void onError(String response) {
                                    Log.e(TAG, "onError: getUserExercises: " + response);
                                }

                                @Override
                                public void onResponse(String response) {
                                    try {
                                        JSONArray array1 = new JSONArray(response);
                                        String generatedExercisesCount = String.valueOf(array1.length());

                                        Generals.getUserPassedModuleQuizAndGeneratedExercisesCount(thisContext, moduleID, new VolleyResponseListener() {
                                            @Override
                                            public void onError(String response) {
                                                Log.e(TAG, "onError: getUserPassedModuleQuizAndGeneratedExercisesCount: " + response);
                                            }

                                            @Override
                                            public void onResponse(String response) {
                                                try {
                                                    JSONArray array_ = new JSONArray(response);
                                                    JSONObject object_ = array_.getJSONObject(0);

                                                    String userPassedGeneratedModuleExercisesCount = object_.getString("user_passed_generated_module_exercises_count");
                                                    String userPassedModuleSummativeCount = object_.getString("user_passed_module_summative_count");

                                                    if (userPassedGeneratedModuleExercisesCount.equals(generatedExercisesCount) && userPassedModuleSummativeCount.equals("1")) {
                                                        ModuleHandler.getInstance(thisContext).updateModuleDone(moduleID, new VolleyResponseListener() {
                                                            @Override
                                                            public void onError(String response) {
                                                                Log.e(TAG, "onError: updateModuleDone: " + response);
                                                            }

                                                            @Override
                                                            public void onResponse(String response) {
                                                                showExpressoDialog(thisContext, "Congratulation on finishing the exercises! \uD83C\uDF89\n" + "You can proceed to the next module to enhance your skills and know more about OOP concepts.\n" + "Stay curious and good luck!", moduleID);
                                                            }
                                                        });
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
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static void showExpressoDialog(Context mContext, String message, String moduleID) {
        Dialog expressoDialog = new Dialog(mContext);
        expressoDialog.setContentView(R.layout.alert_box_expresso);
        expressoDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        TextView tvAbExpressoMessage = (TextView) expressoDialog.findViewById(R.id.tv_ab_expresso_message);
        Button btnAbExpressoProceed = (Button) expressoDialog.findViewById(R.id.btn_ab_expresso_proceed);

        tvAbExpressoMessage.setText(message);
        btnAbExpressoProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, Topics.class);
                intent.putExtra("FROM_ACTIVITY", "Exercise");
                intent.putExtra("module_id", moduleID);
                mContext.startActivity(intent);
            }
        });

        expressoDialog.show();
    }
}