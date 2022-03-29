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
import android.widget.LinearLayout;

import com.example.expresso.activities.Topics;
import com.example.expresso.adapters.SummativeAdapter;
import com.example.expresso.interfaces.VolleyResponseListener;
import com.example.expresso.utils.Generals;
import com.example.expresso.models.Quiz;
import com.example.expresso.R;
import com.example.expresso.utils.ModuleHandler;
import com.example.expresso.utils.QuizHandler;
import com.example.expresso.utils.TopicHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SummativeFragment extends Fragment {
    // constants
    private static final String TAG = "SummativeFragment";
    private Context mContext;

    // variables
    private ArrayList<Quiz> quizzes;
    private ArrayList<String> passedQuizIDs, userQuizIDs;
    private String moduleID;
    private SummativeAdapter quizzesAdapter;

    // views
    private RecyclerView rvQuizzesFragmentQuizzes;
    private LinearLayout llQuizzesFragmentLoading;

    public SummativeFragment(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_summative, container, false);
        initAll(view);

        llQuizzesFragmentLoading.setVisibility(View.VISIBLE);

        Topics activity = (Topics) getActivity();
        moduleID = activity.getModuleID();

        if (Generals.checkInternetConnection(mContext)) {
            getUserDoneTopics();
        }

        return view;
    }

    private void getUserDoneTopics() {
        QuizHandler.getInstance(mContext).getUserQuizIDs(new VolleyResponseListener() {
            @Override
            public void onError(String response) {
                Log.e(TAG, "onError: getUserQuizIDs: " + response);
            }

            @Override
            public void onResponse(String response) {
                try {
                    JSONArray arrayQuizIDs = new JSONArray(response);

                    for (int i = 0; i < arrayQuizIDs.length(); i ++) {
                        JSONObject objectQuizID = arrayQuizIDs.getJSONObject(i);
                        String quidID = objectQuizID.getString("quiz_id");

                        userQuizIDs.add(quidID);
                    }

                    QuizHandler.getInstance(mContext).getPassedQuizIDs(new VolleyResponseListener() {
                        @Override
                        public void onError(String response) {
                            Log.e(TAG, "onError: getPassedQuizIDs: " + response);
                        }

                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONArray arrayQuizIDs = new JSONArray(response);

                                for (int i = 0; i < arrayQuizIDs.length(); i ++) {
                                    JSONObject objectQuizID = arrayQuizIDs.getJSONObject(i);

                                    passedQuizIDs.add(objectQuizID.getString("quiz_id"));
                                }

                                QuizHandler.getInstance(mContext).getAllQuizzes(moduleID, new VolleyResponseListener() {
                                    @Override
                                    public void onError(String response) {
                                        Log.e(TAG, "onError: getAllQuizzes: " + response);
                                    }

                                    @Override
                                    public void onResponse(String response) {
                                        try {
                                            JSONArray arrayQuizzes = new JSONArray(response);

                                            for (int i = 0; i < arrayQuizzes.length(); i ++) {
                                                JSONObject objectQuiz = arrayQuizzes.getJSONObject(i);

                                                quizzes.add(new Quiz(objectQuiz.getString("id"),
                                                                     objectQuiz.getString("module_id"),
                                                                     objectQuiz.getString("title"),
                                                                     objectQuiz.getString("passing"),
                                                                     objectQuiz.getString("items")));
                                            }

                                            displayQuizzes(quizzes);
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

    private void displayQuizzes(ArrayList<Quiz> quizzes) {
        llQuizzesFragmentLoading.setVisibility(View.GONE);

        quizzesAdapter = new SummativeAdapter(mContext);
        rvQuizzesFragmentQuizzes.setAdapter(quizzesAdapter);
        rvQuizzesFragmentQuizzes.setLayoutManager(new LinearLayoutManager(mContext));
        quizzesAdapter.setQuizzes(quizzes);
        quizzesAdapter.setModuleID(moduleID);
        quizzesAdapter.setUserQuizIDs(userQuizIDs);
        quizzesAdapter.setPassedQuizIDs(passedQuizIDs);
    }

    private void initAll(View view) {
        // variables
        quizzes = new ArrayList<>();
        passedQuizIDs = new ArrayList<>();
        userQuizIDs = new ArrayList<>();

        // views
        rvQuizzesFragmentQuizzes = view.findViewById(R.id.rv_quizzes_fragment_quizzes);
        llQuizzesFragmentLoading = view.findViewById(R.id.ll_quizzes_fragment_loading);
    }
}