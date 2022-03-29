package com.example.expresso.fragments;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.example.expresso.R;
import com.example.expresso.activities.Exercise;
import com.example.expresso.interfaces.VolleyResponseListener;
import com.example.expresso.utils.Constants;
import com.example.expresso.utils.ExerciseHandler;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.github.rosemoe.editor.langs.java.JavaLanguage;
import io.github.rosemoe.editor.widget.CodeEditor;
import io.github.rosemoe.editor.widget.EditorColorScheme;

public class ExerciseEditorFragment extends Fragment {
    private static final String TAG = "ExerciseEditorFragment";
    
    // variables
    private Context mContext;
    private String exerciseID, exercisesCount;

    // views
    private CodeEditor ceExerciseEditorFragmentEditor;
    private EditText etExerciseEditorFragmentInputs;
    private Button btnExerciseEditorFragmentRun, btnExerciseEditorFragmentVisualize, btnExerciseEditorFragmentSubmit, btnExerciseEditorFragmentSolution, btnExerciseEditorFragmentClearInput;
    private LinearLayout llExerciseEditorFragmentTestCases;

    // variables
    private Typeface tfConsolasRegular;
    private String slug, templateCode, initialInput, moduleID, solution;

    public ExerciseEditorFragment(Context mContext) {
        this.mContext = mContext;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exercise_editor, container, false);

        initAll(view);

        btnExerciseEditorFragmentRun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ExerciseHandler.compileScript(mContext, ceExerciseEditorFragmentEditor.getText().toString());
            }
        });

        btnExerciseEditorFragmentVisualize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ExerciseHandler.visualizeScript(mContext, ceExerciseEditorFragmentEditor.getText().toString());
            }
        });

        btnExerciseEditorFragmentSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                llExerciseEditorFragmentTestCases.removeAllViews();
                llExerciseEditorFragmentTestCases.setVisibility(View.GONE);
                Constants.TESTING_SCORE = 0;

                ExerciseHandler.getExerciseInputOutput(mContext, exerciseID, new VolleyResponseListener() {
                    @Override
                    public void onError(String response) {
                        Log.e(TAG, "onError: getExerciseInputOutput: " + response);
                    }

                    @Override
                    public void onResponse(String response) {
                        ArrayList<String> inputs = new ArrayList<>();
                        ArrayList<String> outputs = new ArrayList<>();

                        try {
                            JSONArray array = new JSONArray(response);

                            for (int i = 0; i < array.length(); i ++) {
                                JSONObject object = array.getJSONObject(i);

                                inputs.add(object.getString("input"));
                                outputs.add(object.getString("output"));
                            }

                            Snackbar snackbar = Snackbar.make(view, "Scroll down a bit to see the test cases", Snackbar.LENGTH_LONG);
                            snackbar.show();

                            ExerciseHandler.testScript(mContext, moduleID, exerciseID, ceExerciseEditorFragmentEditor.getText().toString(), inputs, outputs, exercisesCount, slug);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

        btnExerciseEditorFragmentClearInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etExerciseEditorFragmentInputs.setText("");
            }
        });

        btnExerciseEditorFragmentSolution.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSolutionDialog();
            }
        });

        return view;
    }

    private void showSolutionDialog() {
        Dialog solutionDialog = new Dialog(mContext);
        solutionDialog.setContentView(R.layout.alert_box_expresso_solution);

        CodeEditor ceAbExpressoSolutionEditor = (CodeEditor) solutionDialog.findViewById(R.id.ce_ab_expresso_solution_editor);
        Button btnAbExpressoSolutionYourSolution = (Button) solutionDialog.findViewById(R.id.btn_ab_expresso_solution_your_solution);
        Button btnAbExpressoSolutionMySolution = (Button) solutionDialog.findViewById(R.id.btn_ab_expresso_solution_my_solution);

        btnAbExpressoSolutionYourSolution.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ceAbExpressoSolutionEditor.setText(ceExerciseEditorFragmentEditor.getText().toString());
                btnAbExpressoSolutionYourSolution.setTextColor(mContext.getResources().getColor(R.color.cb_primary_purple));
                btnAbExpressoSolutionMySolution.setTextColor(mContext.getResources().getColor(R.color.cb_on_surface_white));
            }
        });

        btnAbExpressoSolutionMySolution.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ceAbExpressoSolutionEditor.setText(solution);
                btnAbExpressoSolutionMySolution.setTextColor(mContext.getResources().getColor(R.color.cb_primary_purple));
                btnAbExpressoSolutionYourSolution.setTextColor(mContext.getResources().getColor(R.color.cb_on_surface_white));
            }
        });


        ceAbExpressoSolutionEditor.setEditorLanguage(new JavaLanguage());

        EditorColorScheme editorColorSchemeSolutionEditor = new EditorColorScheme();

        editorColorSchemeSolutionEditor.setColor(1, getResources().getColor(R.color.cb_on_background_black));
        editorColorSchemeSolutionEditor.setColor(2, getResources().getColor(R.color.cb_gray_200));
        editorColorSchemeSolutionEditor.setColor(3, getResources().getColor(R.color.cb_on_background_black));
        editorColorSchemeSolutionEditor.setColor(4, getResources().getColor(R.color.cb_on_background_black));
        editorColorSchemeSolutionEditor.setColor(5, getResources().getColor(R.color.cb_on_background_white));
        editorColorSchemeSolutionEditor.setColor(6, getResources().getColor(R.color.cb_gray_200));
        editorColorSchemeSolutionEditor.setColor(7, getResources().getColor(R.color.cb_on_background_white));
        editorColorSchemeSolutionEditor.setColor(8, getResources().getColor(R.color.cb_gray_200));
        editorColorSchemeSolutionEditor.setColor(9, getResources().getColor(R.color.cb_gray_600));
        editorColorSchemeSolutionEditor.setColor(10, getResources().getColor(R.color.cb_on_background_black));
        editorColorSchemeSolutionEditor.setColor(11, getResources().getColor(R.color.cb_yellow)); // configure
        editorColorSchemeSolutionEditor.setColor(12, getResources().getColor(R.color.cb_yellow)); // configure
        editorColorSchemeSolutionEditor.setColor(13, getResources().getColor(R.color.cb_yellow)); // configure
        editorColorSchemeSolutionEditor.setColor(14, getResources().getColor(R.color.cb_gray_600));
        editorColorSchemeSolutionEditor.setColor(15, getResources().getColor(R.color.cb_gray_200));
        editorColorSchemeSolutionEditor.setColor(16, getResources().getColor(R.color.cb_yellow)); // configure
        editorColorSchemeSolutionEditor.setColor(17, getResources().getColor(R.color.cb_on_background_black));
        editorColorSchemeSolutionEditor.setColor(18, getResources().getColor(R.color.cb_yellow)); // configure
        editorColorSchemeSolutionEditor.setColor(19, getResources().getColor(R.color.cb_on_background_black));
        editorColorSchemeSolutionEditor.setColor(20, getResources().getColor(R.color.cb_on_background_black));
        editorColorSchemeSolutionEditor.setColor(21, getResources().getColor(R.color.cb_light_blue));
        editorColorSchemeSolutionEditor.setColor(22, getResources().getColor(R.color.cb_gray_400));
        editorColorSchemeSolutionEditor.setColor(23, getResources().getColor(R.color.cb_pink));
        editorColorSchemeSolutionEditor.setColor(24, getResources().getColor(R.color.cb_purple));
        editorColorSchemeSolutionEditor.setColor(25, getResources().getColor(R.color.cb_on_background_white));
        editorColorSchemeSolutionEditor.setColor(26, getResources().getColor(R.color.cb_yellow_green));
        editorColorSchemeSolutionEditor.setColor(27, getResources().getColor(R.color.cb_on_background_white));
        editorColorSchemeSolutionEditor.setColor(28, getResources().getColor(R.color.cb_pink));
        editorColorSchemeSolutionEditor.setColor(29, getResources().getColor(R.color.cb_on_background_black));
        editorColorSchemeSolutionEditor.setColor(30, getResources().getColor(R.color.cb_on_background_black));

        ceAbExpressoSolutionEditor.setColorScheme(editorColorSchemeSolutionEditor);
        ceAbExpressoSolutionEditor.setTextSize(14);
        ceAbExpressoSolutionEditor.setTypefaceText(tfConsolasRegular);

        ceAbExpressoSolutionEditor.setText(ceExerciseEditorFragmentEditor.getText().toString());
        btnAbExpressoSolutionYourSolution.setTextColor(mContext.getResources().getColor(R.color.cb_primary_purple));

        solutionDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        solutionDialog.show();
    }

    private void initAll(View view) {
        // views
        ceExerciseEditorFragmentEditor = view.findViewById(R.id.ce_exercise_editor_fragment_editor);
        btnExerciseEditorFragmentRun = view.findViewById(R.id.btn_exercise_editor_fragment_run);
        btnExerciseEditorFragmentVisualize = view.findViewById(R.id.btn_exercise_editor_fragment_visualize);
        btnExerciseEditorFragmentSubmit = view.findViewById(R.id.btn_exercise_editor_fragment_submit);
        btnExerciseEditorFragmentSolution = view.findViewById(R.id.btn_exercise_editor_fragment_solution);
        btnExerciseEditorFragmentClearInput = view.findViewById(R.id.btn_exercise_editor_fragment_clear_input);
        etExerciseEditorFragmentInputs = view.findViewById(R.id.et_exercise_editor_fragment_inputs);
        llExerciseEditorFragmentTestCases = view.findViewById(R.id.ll_exercise_editor_fragment_test_cases);

        ceExerciseEditorFragmentEditor.setEditorLanguage(new JavaLanguage());
        EditorColorScheme editorColorScheme = new EditorColorScheme();
        editorColorScheme.setColor(1, getResources().getColor(R.color.cb_on_background_black));
        editorColorScheme.setColor(2, getResources().getColor(R.color.cb_gray_200));
        editorColorScheme.setColor(3, getResources().getColor(R.color.cb_on_background_black));
        editorColorScheme.setColor(4, getResources().getColor(R.color.cb_on_background_black));
        editorColorScheme.setColor(5, getResources().getColor(R.color.cb_on_background_white));
        editorColorScheme.setColor(6, getResources().getColor(R.color.cb_gray_200));
        editorColorScheme.setColor(7, getResources().getColor(R.color.cb_on_background_white));
        editorColorScheme.setColor(8, getResources().getColor(R.color.cb_gray_200));
        editorColorScheme.setColor(9, getResources().getColor(R.color.cb_gray_600));
        editorColorScheme.setColor(10, getResources().getColor(R.color.cb_on_background_black));
        editorColorScheme.setColor(11, getResources().getColor(R.color.cb_yellow)); // configure
        editorColorScheme.setColor(12, getResources().getColor(R.color.cb_yellow)); // configure
        editorColorScheme.setColor(13, getResources().getColor(R.color.cb_yellow)); // configure
        editorColorScheme.setColor(14, getResources().getColor(R.color.cb_gray_600));
        editorColorScheme.setColor(15, getResources().getColor(R.color.cb_gray_200));
        editorColorScheme.setColor(16, getResources().getColor(R.color.cb_yellow)); // configure
        editorColorScheme.setColor(17, getResources().getColor(R.color.cb_on_background_black));
        editorColorScheme.setColor(18, getResources().getColor(R.color.cb_yellow)); // configure
        editorColorScheme.setColor(19, getResources().getColor(R.color.cb_on_background_black));
        editorColorScheme.setColor(20, getResources().getColor(R.color.cb_on_background_black));
        editorColorScheme.setColor(21, getResources().getColor(R.color.cb_light_blue));
        editorColorScheme.setColor(22, getResources().getColor(R.color.cb_gray_400));
        editorColorScheme.setColor(23, getResources().getColor(R.color.cb_pink));
        editorColorScheme.setColor(24, getResources().getColor(R.color.cb_purple));
        editorColorScheme.setColor(25, getResources().getColor(R.color.cb_on_background_white));
        editorColorScheme.setColor(26, getResources().getColor(R.color.cb_yellow_green));
        editorColorScheme.setColor(27, getResources().getColor(R.color.cb_on_background_white));
        editorColorScheme.setColor(28, getResources().getColor(R.color.cb_pink));
        editorColorScheme.setColor(29, getResources().getColor(R.color.cb_on_background_black));
        editorColorScheme.setColor(30, getResources().getColor(R.color.cb_on_background_black));

        tfConsolasRegular = ResourcesCompat.getFont(mContext, R.font.consolas_regular);

        ceExerciseEditorFragmentEditor.setColorScheme(editorColorScheme);
        ceExerciseEditorFragmentEditor.setTextSize(14);
        ceExerciseEditorFragmentEditor.setTypefaceText(tfConsolasRegular);

        Exercise activity = (Exercise) getActivity();
        slug = activity.getExerciseSlug();
        templateCode = activity.getExerciseTemplateCode();
        initialInput = activity.getExerciseInitialInput();
        exerciseID = activity.getExerciseID();
        moduleID = activity.getExerciseModuleID();
        solution = activity.getExerciseSolution();
        exercisesCount = activity.getExercisesCount();

        ExerciseHandler.checkExerciseRemarks(mContext, exerciseID, new VolleyResponseListener() {
            @Override
            public void onError(String response) {
                Log.e(TAG, "onError: checkExerciseRemarks: " + response);
            }

            @Override
            public void onResponse(String response) {
                if (response.equals("Success")) {
                    btnExerciseEditorFragmentSolution.setVisibility(View.VISIBLE);
                }

                setValues();
            }
        });

//        setValues();
//        btnExerciseEditorFragmentSolution.setVisibility(View.VISIBLE);
    }

    private void setValues() {
        ceExerciseEditorFragmentEditor.setText(templateCode);
        etExerciseEditorFragmentInputs.setText(initialInput);
    }
}