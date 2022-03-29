package com.example.expresso.fragments;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expresso.R;
import com.example.expresso.activities.Topics;
import com.example.expresso.adapters.ExercisesAdapter;
import com.example.expresso.interfaces.VolleyResponseListener;
import com.example.expresso.models.Exercise;
import com.example.expresso.models.Module;
import com.example.expresso.utils.ExerciseHandler;
import com.example.expresso.utils.Generals;
import com.example.expresso.utils.ModuleHandler;
import com.example.expresso.utils.QuizHandler;
import com.example.expresso.utils.TopicHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ExercisesFragment extends Fragment {
    // constants
    private Context mContext;
    private static final String TAG = "ExercisesFragment";

    // variables
    private ArrayList<Exercise> exercises;
    private ArrayList<String> passedExerciseIDs, generatedExerciseIDs;
    private String moduleID;
    private ExercisesAdapter exercisesAdapter;

    // views
    private RecyclerView rvExercisesFragmentExercises;
    private LinearLayout llExercisesFragmentLoading, llExercisesFragmentNoResultsFound;

    public ExercisesFragment(Context mContext) {
        this.mContext = mContext;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exercises, container, false);
        initAll(view);

        llExercisesFragmentLoading.setVisibility(View.VISIBLE);

        Topics activity = (Topics) getActivity();
        moduleID = activity.getModuleID();

        if (Generals.checkInternetConnection(mContext)) {
            loadExercises();
        }

        return view;
    }

    private void loadExercises() {
        ExerciseHandler.getPassedExerciseIDs(mContext, new VolleyResponseListener() {
            @Override
            public void onError(String response) {
                Log.e(TAG, "onError: getPassedExerciseIDs: " + response);
            }

            @Override
            public void onResponse(String response) {
                try {
                    JSONArray arrayPassedExerciseIDs = new JSONArray(response);

                    for (int i = 0; i < arrayPassedExerciseIDs.length(); i ++) {
                        JSONObject objectPassedExerciseID = arrayPassedExerciseIDs.getJSONObject(i);

                        String exerciseID = objectPassedExerciseID.getString("exercise_id");
                        passedExerciseIDs.add(exerciseID);
                    }

                    // TODO: temporary only
//                    ExerciseHandler.getAllExercises(mContext, moduleID, new VolleyResponseListener() {
//                        @Override
//                        public void onError(String response) {
//                            Log.e(TAG, "onError: getAllExercises: " + response);
//                        }
//
//                        @Override
//                        public void onResponse(String response) {
//                            try {
//                                JSONArray array = new JSONArray(response);
//
//                                for (int i = 0; i < array.length(); i ++) {
//                                    JSONObject object = array.getJSONObject(i);
//
//                                    exercises.add(new Exercise("",
//                                            object.getString("id"),
//                                            object.getString("title"),
//                                            object.getString("difficulty"),
//                                            object.getString("module_id"),
//                                            object.getString("description"),
//                                            object.getString("template_code"),
//                                            object.getString("hint"),
//                                            object.getString("initial_input"),
//                                            object.getString("solution")));
//
//
//                                    generatedExerciseIDs.add(object.getString("id"));
//                                }
//
//                                displayExercises(exercises);
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    });


                    ExerciseHandler.getUserExercises(mContext, moduleID, new VolleyResponseListener() {
                        @Override
                        public void onError(String response) {
                            Log.e(TAG, "onError: getUserExercises: " + response);
                        }

                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONArray array = new JSONArray(response);

                                for (int i = 0; i < array.length(); i ++) {
                                    JSONObject object = array.getJSONObject(i);

                                    exercises.add(new Exercise(object.getString("user_exercise_id"),
                                                               object.getString("id"),
                                                               object.getString("title"),
                                                               object.getString("difficulty"),
                                                               object.getString("module_id"),
                                                               object.getString("description"),
                                                               object.getString("slug"),
                                                               object.getString("template_code"),
                                                               object.getString("hint"),
                                                               object.getString("initial_input"),
                                                               object.getString("solution")));


                                    generatedExerciseIDs.add(object.getString("id"));
                                }

                                displayExercises(exercises);
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

    private void displayExercises(ArrayList<Exercise> exercises) {
        llExercisesFragmentLoading.setVisibility(View.GONE);

        if (exercises.size() == 0) {
            llExercisesFragmentNoResultsFound.setVisibility(View.VISIBLE);
//            showExpressoDialog();
        } else {
            exercisesAdapter = new ExercisesAdapter(mContext);
            rvExercisesFragmentExercises.setAdapter(exercisesAdapter);
            rvExercisesFragmentExercises.setLayoutManager(new LinearLayoutManager(mContext));
            exercisesAdapter.setExercises(exercises);
            exercisesAdapter.setModuleID(moduleID);
            exercisesAdapter.setPassedExerciseIDs(passedExerciseIDs);
        }
    }

    private void initAll(View view) {
        // variables
        exercises = new ArrayList<>();
        passedExerciseIDs = new ArrayList<>();
        generatedExerciseIDs = new ArrayList<>();


        // views
        rvExercisesFragmentExercises = view.findViewById(R.id.rv_exercises_fragment_exercises);
        llExercisesFragmentLoading = view.findViewById(R.id.ll_exercises_fragment_loading);
        llExercisesFragmentNoResultsFound = view.findViewById(R.id.ll_exercises_fragment_no_results_found);
    }

    private void showExpressoDialog() {
        Dialog expressoDialog = new Dialog(mContext);
        expressoDialog.setContentView(R.layout.alert_box_expresso);
        expressoDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        TextView tvAbExpressoMessage = (TextView) expressoDialog.findViewById(R.id.tv_ab_expresso_message);
        Button btnAbExpressoProceed = (Button) expressoDialog.findViewById(R.id.btn_ab_expresso_proceed);

        tvAbExpressoMessage.setText("You must finish all the topics first to help me generate your exercises.");
        btnAbExpressoProceed.setVisibility(View.GONE);

        expressoDialog.show();
    }
}