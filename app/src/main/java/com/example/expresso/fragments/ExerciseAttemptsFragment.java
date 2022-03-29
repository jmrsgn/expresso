package com.example.expresso.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.expresso.activities.Exercise;
import com.example.expresso.adapters.ExerciseAttemptAdapter;
import com.example.expresso.interfaces.VolleyResponseListener;
import com.example.expresso.models.ExerciseAttempt;
import com.example.expresso.R;
import com.example.expresso.utils.ExerciseHandler;
import com.example.expresso.utils.Generals;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ExerciseAttemptsFragment extends Fragment {
    // constants
    private static final String TAG = "ExerciseAttemptsFragment";

    // variables
    private Context mContext;
    private String exerciseID, userExerciseID;
    private ExerciseAttemptAdapter exerciseAttemptAdapter;
    private ArrayList<ExerciseAttempt> exerciseAttempts;
    private ImageView ivExerciseAttemptsFragmentRefreshAttempts;
    private LinearLayout llExerciseAttemptsFragmentLoading, llExerciseAttemptsFragmentNoResultsFound;

    // views
    private RecyclerView rvExerciseAttemptsFragmentAttempts;

    public ExerciseAttemptsFragment(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exercise_attempts, container, false);

        initAll(view);

        ivExerciseAttemptsFragmentRefreshAttempts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rvExerciseAttemptsFragmentAttempts.setVisibility(View.GONE);
                llExerciseAttemptsFragmentNoResultsFound.setVisibility(View.GONE);
                llExerciseAttemptsFragmentLoading.setVisibility(View.VISIBLE);
                exerciseAttempts.clear();
                loadExerciseAttempts();
            }
        });

        return view;
    }

    private void initAll(View view) {
        rvExerciseAttemptsFragmentAttempts = view.findViewById(R.id.rv_exercise_attempts_fragment_attempts);
        llExerciseAttemptsFragmentLoading = view.findViewById(R.id.ll_exercise_attempts_fragment_loading);
        llExerciseAttemptsFragmentNoResultsFound = view.findViewById(R.id.ll_exercise_attempts_fragment_no_results_found);
        ivExerciseAttemptsFragmentRefreshAttempts = view.findViewById(R.id.iv_exercise_attempts_fragment_refresh_attempts);
        exerciseAttempts = new ArrayList<>();

        Exercise activity = (Exercise) getActivity();
        exerciseID = activity.getExerciseID();
        userExerciseID = activity.getUserExerciseID();

        loadExerciseAttempts();
    }

    private void loadExerciseAttempts() {
        rvExerciseAttemptsFragmentAttempts.setVisibility(View.GONE);
        llExerciseAttemptsFragmentLoading.setVisibility(View.VISIBLE);

        ExerciseHandler.getExerciseAttempts(mContext, userExerciseID, exerciseID, new VolleyResponseListener() {
            @Override
            public void onError(String response) {
                Log.e(TAG, "onError: " + response);
            }

            @Override
            public void onResponse(String response) {
                try {
                    JSONArray array = new JSONArray(response);

                    for (int i = 0; i < array.length(); i ++) {
                        JSONObject object = array.getJSONObject(i);

                        exerciseAttempts.add(new ExerciseAttempt(object.getString("created_at"),
                                                                 object.getString("remarks"),
                                                                 object.getString("exercise_id"),
                                                                 object.getString("code")));
                    }

                    displayExerciseAttempts(exerciseAttempts);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void displayExerciseAttempts(ArrayList<ExerciseAttempt> exerciseAttempts) {
        llExerciseAttemptsFragmentLoading.setVisibility(View.GONE);
        rvExerciseAttemptsFragmentAttempts.setVisibility(View.VISIBLE);

        if (exerciseAttempts.size() == 0) {
            llExerciseAttemptsFragmentNoResultsFound.setVisibility(View.VISIBLE);
        } else {
            llExerciseAttemptsFragmentNoResultsFound.setVisibility(View.GONE);
            exerciseAttemptAdapter = new ExerciseAttemptAdapter(mContext);
            rvExerciseAttemptsFragmentAttempts.setAdapter(exerciseAttemptAdapter);
            rvExerciseAttemptsFragmentAttempts.setLayoutManager(new LinearLayoutManager(mContext));
            exerciseAttemptAdapter.setExerciseAttempts(exerciseAttempts);
            exerciseAttemptAdapter.setExerciseID(exerciseID);
            exerciseAttemptAdapter.notifyDataSetChanged();
        }
    }
}