package com.example.expresso.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.expresso.R;
import com.example.expresso.activities.Topics;
import com.example.expresso.glideSVG.GlideApp;
import com.example.expresso.interfaces.VolleyResponseListener;
import com.example.expresso.network.RequestQueueSingleton;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.models.Shape;

public class ExerciseHandler {
    // constants
    private static final String TAG = "ExerciseHandler";

    // general
    private static void showOutputLoading(Context mContext) {
        LinearLayout llExerciseEditorFragmentOutputLoading = (LinearLayout) ((Activity)mContext).findViewById(R.id.ll_exercise_editor_fragment_output_loading);
        llExerciseEditorFragmentOutputLoading.setVisibility(View.VISIBLE);
    }

    private static void hideOutputLoading(Context mContext) {
        LinearLayout llExerciseEditorFragmentOutputLoading = (LinearLayout) ((Activity)mContext).findViewById(R.id.ll_exercise_editor_fragment_output_loading);
        llExerciseEditorFragmentOutputLoading.setVisibility(View.GONE);
    }

    private static void showTestCasesLoading(Context mContext) {
        LinearLayout llExerciseEditorFragmentTestCasesLoading = (LinearLayout) ((Activity)mContext).findViewById(R.id.ll_exercise_editor_fragment_test_cases_loading);
        llExerciseEditorFragmentTestCasesLoading.setVisibility(View.VISIBLE);
    }

    private static void hideTestCasesLoading(Context mContext) {
        LinearLayout llExerciseEditorFragmentTestCasesLoading = (LinearLayout) ((Activity)mContext).findViewById(R.id.ll_exercise_editor_fragment_test_cases_loading);
        llExerciseEditorFragmentTestCasesLoading.setVisibility(View.GONE);
    }

    public static void getExerciseAttempts(Context mContext, String userExerciseID, String exerciseID, VolleyResponseListener volleyResponseListener) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Routes.URL_GET_EXERCISE_ATTEMPTS, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                volleyResponseListener.onResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse: " + error.toString());
                volleyResponseListener.onError(error.toString());
            }
        }) {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", Constants.ID);
                params.put("user_exercise_id", userExerciseID);
                params.put("exercise_id", exerciseID);

                return params;
            }
        };

        RequestQueueSingleton.getInstance(mContext).addToRequestQueue(stringRequest);
    }

    // ================ testing
    public static void testScript(Context mContext, String moduleID, String exerciseID, String script, ArrayList<String> inputs, ArrayList<String> outputs, String exercisesCount, String slug) {
        Generals.saveFile(mContext, script);
        String modifiedScript = Generals.loadFile(mContext, Constants.MAIN_SCRIPT_FILE_NAME + Constants.TXT_EXTENSION);

        showTestCasesLoading(mContext);
        new Test(mContext, moduleID, exerciseID, modifiedScript, inputs, outputs, exercisesCount, slug).execute();
    }

    private static class Test extends AsyncTask<Void, Void, Void> {
        protected String script, exerciseID, moduleID, slug;
        protected Context mContext;
        protected final Handler handler;
        protected String exercisesCount;
        protected ArrayList<String> inputs = new ArrayList<>();
        protected ArrayList<String> outputs= new ArrayList<>();

        public Test(Context mContext, String moduleID, String exerciseID, String script, ArrayList<String> inputs, ArrayList<String> outputs, String exercisesCount, String slug) {
            this.mContext = mContext;
            this.exercisesCount = exercisesCount;
            this.moduleID = moduleID;
            this.exerciseID = exerciseID;
            this.script = script;
            this.inputs = inputs;
            this.outputs = outputs;
            this.slug = slug;
            handler = new Handler(mContext.getMainLooper());
        }

        @Override
        protected Void doInBackground(Void... voids) {
            for (int i = 0; i < inputs.size(); i ++) {
                try {
                    int counter = i;
                    URL url = new URL(Routes.URL_COMPILE);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoOutput(true);
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");

                    String inputRequest = "{\"script\":\"" + script + "\",\"stdin\":\"" + inputs.get(i).replaceAll("\n", "\\\\n") + "\"} ";

                    System.out.println(inputRequest);

                    OutputStream outputStream = connection.getOutputStream();
                    outputStream.write(inputRequest.getBytes());
                    outputStream.flush();

                    BufferedReader bufferedReader;
                    bufferedReader = new BufferedReader(new InputStreamReader((connection.getInputStream())));

                    String outputString = "";

                    while ((outputString = bufferedReader.readLine()) != null) {
                        String input = inputs.get(i);
                        String output = outputs.get(i);

                        JSONObject object = new JSONObject(outputString);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Typeface tfOpenSans = ResourcesCompat.getFont(mContext, R.font.open_sans_regular);
                                RelativeLayout.LayoutParams tvLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                                RelativeLayout.LayoutParams ivLayoutParams = new RelativeLayout.LayoutParams(35, 35);
                                LinearLayout.LayoutParams lpcvExerciseExample, lpllTestCaseInfo;
                                RelativeLayout.LayoutParams lprlTestCase;

                                String remark = "";

                                try {
                                    String outputRaw = object.getString("output").trim();

                                    if (outputRaw.equals(output)) {
                                        remark = "correct";
                                    } else {
                                        remark = "wrong";
                                    }

                                    LinearLayout llExerciseEditorFragmentTestCases = (LinearLayout) ((Activity) mContext).findViewById(R.id.ll_exercise_editor_fragment_test_cases);

                                    float scale = mContext.getResources().getDisplayMetrics().density;
                                    int padding_12dp = (int) (12 * scale + 0.5f);

                                    lpcvExerciseExample = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                    lprlTestCase = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                                    lpllTestCaseInfo = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                    lpllTestCaseInfo.setMargins(24, 24, 0, 0);

                                    // ======

                                    MaterialCardView mcvTestCases = new MaterialCardView(mContext);
                                    mcvTestCases.setLayoutParams(lpcvExerciseExample);
                                    mcvTestCases.setCardElevation(0);
                                    mcvTestCases.setBackground(mContext.getResources().getDrawable(R.drawable.exercise_test_case));

                                    // ======

                                    RelativeLayout rlTestCase = new RelativeLayout(mContext);
                                    rlTestCase.setLayoutParams(lprlTestCase);
                                    rlTestCase.setPadding(padding_12dp, padding_12dp, padding_12dp, padding_12dp);

                                    // ======

                                    TextView tvTestCaseTitle = new TextView(mContext);
                                    tvTestCaseTitle.setText("Test #" + Integer.toString(counter + 1));
                                    tvTestCaseTitle.setAlpha(0.87f);
                                    tvLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                                    tvTestCaseTitle.setLayoutParams(tvLayoutParams);
                                    tvTestCaseTitle.setTypeface(tfOpenSans);

                                    // ======

                                    if (remark.equals("correct")) {
                                        tvTestCaseTitle.setTextColor(mContext.getResources().getColor(R.color.cb_green));
                                        Constants.TESTING_SCORE += 1;
                                    } else {
                                        tvTestCaseTitle.setTextColor(mContext.getResources().getColor(R.color.cb_red));
                                    }

                                    // ======

                                    tvTestCaseTitle.setTypeface(tvTestCaseTitle.getTypeface(), Typeface.BOLD);
                                    rlTestCase.addView(tvTestCaseTitle);

                                    // ======

                                    LinearLayout llTestCaseInfo = new LinearLayout(mContext);
                                    llTestCaseInfo.setLayoutParams(lpllTestCaseInfo);
                                    llTestCaseInfo.setOrientation(LinearLayout.VERTICAL);

                                    // TODO: fix UI, try first inserting a linear layout

                                    // ======

                                    TextView tvInput = new TextView(mContext);
                                    tvInput.setVisibility(View.GONE);
                                    tvInput.setText("Input: " + "\n" + input);
                                    tvInput.setTypeface(tfOpenSans);
                                    tvInput.setAlpha(0.6f);
                                    llTestCaseInfo.addView(tvInput);

                                    // ======

                                    TextView tvOutput = new TextView(mContext);
                                    tvOutput.setVisibility(View.GONE);
                                    tvOutput.setText("\n\nOutput: \n" + outputRaw);
                                    tvOutput.setTypeface(tfOpenSans);
                                    tvOutput.setAlpha(0.6f);
                                    llTestCaseInfo.addView(tvOutput);

                                    // ======

                                    TextView tvExpectedOutput = new TextView(mContext);
                                    tvExpectedOutput.setVisibility(View.GONE);
                                    tvExpectedOutput.setText("\n\nExpected Output: \n" + output + "\n");
                                    tvExpectedOutput.setTypeface(tfOpenSans);
                                    tvExpectedOutput.setAlpha(0.6f);
                                    llTestCaseInfo.addView(tvExpectedOutput);

                                    // ======

                                    ivLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                                    ivLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);

                                    // ======

                                    ImageView ivShowTestCase = new ImageView(mContext);
                                    ivShowTestCase.setImageDrawable(mContext.getDrawable(R.drawable.arrow_down));
                                    ivShowTestCase.setLayoutParams(ivLayoutParams);
                                    rlTestCase.addView(ivShowTestCase);

                                    // ======

                                    ImageView ivHideTestCase = new ImageView(mContext);
                                    ivHideTestCase.setVisibility(View.GONE);
                                    ivHideTestCase.setImageDrawable(mContext.getDrawable(R.drawable.arrow_up));
                                    ivHideTestCase.setLayoutParams(ivLayoutParams);
                                    rlTestCase.addView(ivHideTestCase);

                                    // ======

                                    // listeners

                                    AutoTransition autoTransition = new AutoTransition();
                                    autoTransition.setDuration(100);

                                    ivShowTestCase.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            TransitionManager.beginDelayedTransition(llTestCaseInfo, autoTransition);
                                            ivShowTestCase.setVisibility(View.GONE);
                                            ivHideTestCase.setVisibility(View.VISIBLE);
                                            tvInput.setVisibility(View.VISIBLE);
                                            tvOutput.setVisibility(View.VISIBLE);
                                            tvExpectedOutput.setVisibility(View.VISIBLE);
                                        }
                                    });

                                    ivHideTestCase.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            TransitionManager.beginDelayedTransition(llTestCaseInfo, autoTransition);
                                            ivHideTestCase.setVisibility(View.GONE);
                                            ivShowTestCase.setVisibility(View.VISIBLE);
                                            tvInput.setVisibility(View.GONE);
                                            tvOutput.setVisibility(View.GONE);
                                            tvExpectedOutput.setVisibility(View.GONE);
                                        }
                                    });

                                    // mounting
                                    mcvTestCases.addView(rlTestCase);
                                    llExerciseEditorFragmentTestCases.addView(mcvTestCases);
                                    llExerciseEditorFragmentTestCases.addView(llTestCaseInfo);

                                    if (counter == inputs.size() - 1) {
                                        hideTestCasesLoading(mContext);
                                        llExerciseEditorFragmentTestCases.setVisibility(View.VISIBLE);

                                        if (Constants.TESTING_SCORE == 3) {
                                            showConfetti(mContext);
                                            Button btnExerciseEditorFragmentSolution = (Button) ((Activity)mContext).findViewById(R.id.btn_exercise_editor_fragment_solution);
                                            btnExerciseEditorFragmentSolution.setVisibility(View.VISIBLE);
                                            new SubmitExercise(mContext, "passed", script, exerciseID, moduleID, exercisesCount, slug).execute();
                                        } else {
                                            new SubmitExercise(mContext, "failed", script, exerciseID, moduleID, exercisesCount, slug).execute();
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }

                    connection.disconnect();

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

        private void runOnUiThread(Runnable r) {
            handler.post(r);
        }
    }


    // ================ compilation
    public static void compileScript(Context mContext, String script) {
        Generals.saveFile(mContext, script);
        String modifiedScript = Generals.loadFile(mContext,Constants.MAIN_SCRIPT_FILE_NAME + Constants.TXT_EXTENSION);
        showOutputLoading(mContext);
        new Compile(mContext, modifiedScript).execute();
    }

    private static class Compile extends AsyncTask<Void, Void, Void> {
        protected String script;
        protected final Handler handler;
        protected Context mContext;

        public Compile(Context mContext, String script) {
            this.script = script;
            this.mContext = mContext;
            handler = new Handler(mContext.getMainLooper());
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                URL url = new URL(Routes.URL_COMPILE);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");

                // views
                TextView tvExerciseEditorFragmentOutput = (TextView) ((Activity)mContext).findViewById(R.id.tv_exercise_editor_fragment_output);
                TextView tvExerciseEditorFragmentMemoryAllocated = (TextView) ((Activity)mContext).findViewById(R.id.tv_exercise_editor_fragment_memory_allocated);
                TextView tvExerciseEditorFragmentCPUTime = (TextView) ((Activity)mContext).findViewById(R.id.tv_exercise_editor_fragment_cpu_time);

                // views
                EditText etExerciseEditorFragmentInputs = (EditText) ((Activity)mContext).findViewById(R.id.et_exercise_editor_fragment_inputs);

                String input = "";

                if (!etExerciseEditorFragmentInputs.getText().equals("")) {
                    input = "{\"script\":\"" + script + "\",\"stdin\":\"" + etExerciseEditorFragmentInputs.getText().toString().replaceAll("\n", "\\\\n") + "\"} ";
                } else {
                    input = "{\"script\":\"" + script + "\"} ";
                }

                System.out.println(input);

                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(input.getBytes());
                outputStream.flush();

                BufferedReader bufferedReader;
                bufferedReader = new BufferedReader(new InputStreamReader((connection.getInputStream())));

                String output = "";

                while ((output = bufferedReader.readLine()) != null) {
                    System.out.println(output);
                    JSONObject object = new JSONObject(output);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                tvExerciseEditorFragmentOutput.setVisibility(View.VISIBLE);
                                tvExerciseEditorFragmentMemoryAllocated.setVisibility(View.VISIBLE);
                                tvExerciseEditorFragmentCPUTime.setVisibility(View.VISIBLE);

                                tvExerciseEditorFragmentOutput.setText(object.getString("output"));
                                tvExerciseEditorFragmentMemoryAllocated.setText("Memory allocated: " + object.getString("memory"));
                                tvExerciseEditorFragmentCPUTime.setText("CPU time: " + object.getString("cpuTime") + "ms");

                                hideOutputLoading(mContext);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }

                connection.disconnect();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

        private void runOnUiThread(Runnable r) {
            handler.post(r);
        }
    }

    public static void getExerciseInputOutput(Context mContext, String exerciseID, VolleyResponseListener volleyResponseListener) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Routes.URL_GET_EXERCISE_INPUT_OUTPUT, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                volleyResponseListener.onResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse: " + error.toString());
                volleyResponseListener.onError(error.toString());
            }
        }) {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("exercise_id", exerciseID);

                return params;
            }
        };

        RequestQueueSingleton.getInstance(mContext).addToRequestQueue(stringRequest);
    }


    // ================ visualization
    public static void visualizeScript(Context mContext, String script) {
        Generals.saveFile(mContext, script);
        String modifiedScript = Generals.loadFile(mContext, Constants.MAIN_SCRIPT_FILE_NAME + Constants.TXT_EXTENSION);
        new Visualize(mContext, modifiedScript).execute();
    }

    private static class Visualize extends AsyncTask<Void, Void, Void> {
        protected String script;
        protected final Handler handler;
        protected Context mContext;

        public Visualize(Context mContext, String script) {
            this.script = script;
            this.mContext = mContext;
            handler = new Handler(mContext.getMainLooper());
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                URL url = new URL(Routes.URL_VISUALIZE);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");

                String input = "{\"script\":\"" + script + "\"} ";

                System.out.println(input);

                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(input.getBytes());
                outputStream.flush();

                BufferedReader bufferedReader;
                bufferedReader = new BufferedReader(new InputStreamReader((connection.getInputStream())));

                String output = "";

                while ((output = bufferedReader.readLine()) != null) {
                    System.out.println(output);
                    JSONObject object = new JSONObject(output);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                showVisualizedCodeDialog(mContext, object.getString("url"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }

                connection.disconnect();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

        private void runOnUiThread(Runnable r) {
            handler.post(r);
        }
    }

    private static void showVisualizedCodeDialog(Context mContext, String url) {
        Dialog visualizeCodeDialog = new Dialog(mContext);
        visualizeCodeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        visualizeCodeDialog.getWindow().setBackgroundDrawableResource(R.drawable.visualized_code_background);
        visualizeCodeDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                //nothing;
            }
        });

        ImageView ivVisualizedCode = new ImageView(mContext);

        String modifiedURL = url.replaceAll("https", "https").replaceAll("http", "https");

        GlideApp.with(mContext)
                .load(modifiedURL)
                .centerCrop()
                .into(ivVisualizedCode);

        visualizeCodeDialog.addContentView(ivVisualizedCode, new RelativeLayout.LayoutParams(1000, 300));
        visualizeCodeDialog.show();
    }

    // exercise items
    public static void getUserExercises(Context mContext, String moduleID, VolleyResponseListener volleyResponseListener) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Routes.URL_GET_USER_EXERCISES, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                volleyResponseListener.onResponse(response);
            }}, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                volleyResponseListener.onError(error.toString());
            }
        }) {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", Constants.ID);
                params.put("module_id", moduleID);

                return params;
            }
        };

        RequestQueueSingleton.getInstance(mContext).addToRequestQueue(stringRequest);
    }

    public static void updateExerciseCode(Context mContext, String attemptID, String code, VolleyResponseListener volleyResponseListener) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Routes.URL_UPDATE_EXERCISE_CODE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                volleyResponseListener.onResponse(response);
            }}, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                volleyResponseListener.onError(error.toString());
            }
        }) {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("id", attemptID);
                params.put("code", code);

                return params;
            }
        };

        RequestQueueSingleton.getInstance(mContext).addToRequestQueue(stringRequest);
    }

    public static void getExerciseFailedAttemptsCount(Context mContext, String exerciseID, VolleyResponseListener volleyResponseListener) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Routes.URL_GET_EXERCISE_FAILED_ATTEMPTS_COUNT, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                volleyResponseListener.onResponse(response);
            }}, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                volleyResponseListener.onError(error.toString());
            }
        }) {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", Constants.ID);
                params.put("exercise_id", exerciseID);

                return params;
            }
        };

        RequestQueueSingleton.getInstance(mContext).addToRequestQueue(stringRequest);
    }

    public static void getAllExercises(Context mContext, String moduleID, VolleyResponseListener volleyResponseListener) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Routes.URL_GET_ALL_EXERCISES, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                volleyResponseListener.onResponse(response);
            }}, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                volleyResponseListener.onError(error.toString());
            }
        }) {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("module_id", moduleID);

                return params;
            }
        };

        RequestQueueSingleton.getInstance(mContext).addToRequestQueue(stringRequest);
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

    public static void getExerciseExamples(Context mContext, String exerciseID, VolleyResponseListener volleyResponseListener) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Routes.URL_GET_EXERCISE_EXAMPLES, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                volleyResponseListener.onResponse(response);
            }}, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                volleyResponseListener.onError(error.toString());
            }
        }) {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("exercise_id", exerciseID);

                return params;
            }
        };

        RequestQueueSingleton.getInstance(mContext).addToRequestQueue(stringRequest);
    }

    public static void getPassedExerciseIDs(Context mContext, VolleyResponseListener volleyResponseListener) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Routes.URL_GET_PASSED_EXERCISE_IDS, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                volleyResponseListener.onResponse(response);
            }}, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                volleyResponseListener.onError(error.toString());
            }
        }) {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", Constants.ID);

                return params;
            }
        };

        RequestQueueSingleton.getInstance(mContext).addToRequestQueue(stringRequest);
    }

    public static void checkExerciseRemarks(Context mContext, String exerciseID, VolleyResponseListener volleyResponseListener) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Routes.URL_CHECK_EXERCISE_REMARKS, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                volleyResponseListener.onResponse(response);
            }}, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                volleyResponseListener.onError(error.toString());
            }
        }) {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", Constants.ID);
                params.put("exercise_id", exerciseID);

                return params;
            }
        };

        RequestQueueSingleton.getInstance(mContext).addToRequestQueue(stringRequest);
    }

    public static void getPassedGeneratedExerciseIDs(Context mContext, String moduleID, VolleyResponseListener volleyResponseListener) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Routes.URL_GET_PASSED_GENERATED_EXERCISE_IDS, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                volleyResponseListener.onResponse(response);
            }}, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                volleyResponseListener.onError(error.toString());
            }
        }) {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", Constants.ID);
                params.put("module_id", moduleID);

                return params;
            }
        };

        RequestQueueSingleton.getInstance(mContext).addToRequestQueue(stringRequest);
    }

    public static void generateExercises(Context mContext, String moduleSlug) {
        new GenerateExercises(mContext, moduleSlug).execute();
    }

    private static class GenerateExercises extends AsyncTask<Void, Void, Void> {
        protected String moduleSlug;
        protected Context mContext;

        public GenerateExercises(Context mContext, String moduleSlug) {
            this.mContext = mContext;
            this.moduleSlug = moduleSlug;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                URL url = new URL(Routes.URL_GENERATE_EXERCISES);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");

                String input = "{\"email\":\"" + Constants.EMAIL + "\",\"slug\":\"" + moduleSlug + "\"} ";

                System.out.println(input);

                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(input.getBytes());
                outputStream.flush();

                connection.getInputStream();
                connection.disconnect();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    private static class SubmitExercise extends AsyncTask<Void, Void, Void> {
        protected final Handler handler;
        protected String remarks, code, exerciseID, moduleID, exercisesCount, slug;
        protected Context mContext;

        public SubmitExercise(Context mContext, String remarks, String code, String exerciseID, String moduleID, String exercisesCount, String slug) {
            this.mContext = mContext;
            this.remarks = remarks;
            this.code = code;
            this.moduleID = moduleID;
            this.exerciseID = exerciseID;
            this.exercisesCount = exercisesCount;
            this.slug = slug;
            handler = new Handler(mContext.getMainLooper());
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                URL url = new URL(Routes.URL_SUBMIT_EXERCISE);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");

                String input = "{\"email\":\"" + Constants.EMAIL + "\",\"remarks\":\"" + remarks + "\",\"code\":\"" + code + "\",\"exercise_id\":\"" + exerciseID + "\"} ";

                System.out.println(input);

                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(input.getBytes());
                outputStream.flush();

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                String output = "";

                while ((output = bufferedReader.readLine()) != null) {
                    System.out.println(output);

                    JSONObject object = new JSONObject(output);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String id = object.getString("id");
                                String code = Generals.getUserCode(mContext, Constants.MAIN_SCRIPT_FILE_NAME + Constants.TXT_EXTENSION);

                                updateExerciseCode(mContext, id, code, new VolleyResponseListener() {
                                    @Override
                                    public void onError(String response) {
                                        Log.e(TAG, "onError: updateExerciseCode: " + response);
                                    }

                                    @Override
                                    public void onResponse(String response) {
                                        logSubmitExerciseAttempt(mContext, slug);

                                        getPassedGeneratedExerciseIDs(mContext, moduleID, new VolleyResponseListener() {
                                            @Override
                                            public void onError(String response) {
                                                Log.e(TAG, "onError: getPassedGeneratedExerciseIDs: " + response);
                                            }

                                            @Override
                                            public void onResponse(String response) {
                                                try {
                                                    JSONArray arrayExerciseIDs = new JSONArray(response);

//                                                    Generals.makeToast(mContext, "Passed exercises count: " + String.valueOf(arrayExerciseIDs.length()));
//                                                    Generals.makeToast(mContext, "Exercises count: " + exercisesCount);

                                                    if (arrayExerciseIDs.length() == Integer.parseInt(exercisesCount)) {

                                                        // added

                                                        Generals.getUserPassedModuleQuizAndGeneratedExercisesCount(mContext, moduleID, new VolleyResponseListener() {
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

                                                                    if (userPassedModuleSummativeCount.equals("1")) {
                                                                        ModuleHandler.getInstance(mContext).updateModuleDone(moduleID, new VolleyResponseListener() {
                                                                            @Override
                                                                            public void onError(String response) {
                                                                                Log.e(TAG, "onError: updateModuleDone: " + response);
                                                                            }

                                                                            @Override
                                                                            public void onResponse(String response) {
                                                                                if (response.equals("Success")) {
                                                                                    showExpressoDialog(mContext, "Congratulation on finishing the exercises! \uD83C\uDF89\n" + "You can proceed to the next module to enhance your skills and know more about OOP concepts.\n" + "Stay curious and good luck!", moduleID);
                                                                                }
                                                                            }
                                                                        });
                                                                    }
                                                                } catch (JSONException e) {
                                                                    e.printStackTrace();
                                                                }
                                                            }
                                                        });

//                                                        ModuleHandler.getInstance(mContext).updateModuleDone(moduleID, new VolleyResponseListener() {
//                                                            @Override
//                                                            public void onError(String response) {
//                                                                Log.e(TAG, "onError: updateModuleDone: " + response);
//                                                            }
//
//                                                            @Override
//                                                            public void onResponse(String response) {
//                                                                if (response.equals("Success")) {
//                                                                    showExpressoDialog(mContext, "Congratulation on finishing the exercises! \uD83C\uDF89\n" + "You can proceed to the next module to enhance your skills and know more about OOP concepts.\n" + "Stay curious and good luck!", moduleID);
//                                                                }
//                                                            }
//                                                        });
                                                    }

                                                    checkExerciseRemarks(mContext, exerciseID, new VolleyResponseListener() {
                                                        @Override
                                                        public void onError(String response) {
                                                            Log.e(TAG, "onError: checkExerciseRemarks: " + response);
                                                        }

                                                        @Override
                                                        public void onResponse(String response) {
                                                            if (response.equals("Failed")) {
                                                                getExerciseFailedAttemptsCount(mContext, exerciseID, new VolleyResponseListener() {
                                                                    @Override
                                                                    public void onError(String response) {
                                                                        Log.e(TAG, "onError: getFailedExerciseAttemptsCount: " + response);
                                                                    }

                                                                    @Override
                                                                    public void onResponse(String response) {
                                                                        int count = Integer.parseInt(response);

                                                                        if (count > 4) {
                                                                            SharedPreferences spModulesInfo = mContext.getSharedPreferences(Constants.SP_MODULES_INFO, Context.MODE_PRIVATE);
                                                                            Gson gson = new Gson();

                                                                            String moduleInfoJson = spModulesInfo.getString(moduleID, null);

                                                                            if (moduleInfoJson != null) {
                                                                                String[] moduleInfo = gson.fromJson(moduleInfoJson, String[].class);
                                                                                new GenerateExercise(mContext, moduleInfo[2]).execute();

                                                                                showExpressoDialog(mContext, "Looks like you are having a hard time solving the exercise, I provided a new exercise for you", moduleID);
                                                                            }
                                                                        }
                                                                    }
                                                                });
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

                connection.disconnect();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

        private void runOnUiThread(Runnable r) {
            handler.post(r);
        }
    }

    private static class GenerateExercise extends AsyncTask<Void, Void, Void> {
        protected String moduleSlug;
        protected Context mContext;

        public GenerateExercise(Context mContext, String moduleSlug) {
            this.mContext = mContext;
            this.moduleSlug = moduleSlug;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                URL url = new URL(Routes.URL_GENERATE_EXERCISE);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");

                String input = "{\"email\":\"" + Constants.EMAIL + "\",\"slug\":\"" + moduleSlug + "\"} ";

                System.out.println(input);

                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(input.getBytes());
                outputStream.flush();

                connection.getInputStream();
                connection.disconnect();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    private static void showConfetti(Context mContext) {
        KonfettiView kvConfetti = (KonfettiView) ((Activity)mContext).findViewById(R.id.kv_exercise_editor_fragment_confetti);
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

    private static void logSubmitExerciseAttempt(Context mContext, String exerciseSlug) {
        LogsHandler.getInstance(mContext).addLog("submit exercise attempt: " + exerciseSlug, new VolleyResponseListener() {
            @Override
            public void onError(String response) {
                Log.e(TAG, "onError: addLog: " + response);
            }

            @Override
            public void onResponse(String response) {
                // do nothing
            }
        });
    }
}
