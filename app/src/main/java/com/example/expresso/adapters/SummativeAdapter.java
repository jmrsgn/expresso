package com.example.expresso.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expresso.R;
import com.example.expresso.activities.Quiz;
import com.example.expresso.activities.Topics;
import com.example.expresso.interfaces.VolleyResponseListener;
import com.example.expresso.utils.Generals;
import com.example.expresso.utils.QuizHandler;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;

public class SummativeAdapter extends RecyclerView.Adapter<SummativeAdapter.ViewHolder> {
    // constants
    private static final String TAG = "SummativeAdapter";

    // variables
    private Context mContext;
    private ArrayList<com.example.expresso.models.Quiz> quizzes;
    private ArrayList<String> passedQuizIDs, userQuizIDs;
    private String moduleID;
    private String attempt;

    public SummativeAdapter(Context mContext) {
        this.mContext = mContext;
        initAll();
    }

    private void initAll() {
        quizzes = new ArrayList<>();
        passedQuizIDs = new ArrayList<>();
        userQuizIDs = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_quiz, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SummativeAdapter.ViewHolder holder, int position) {
        moduleID = Topics.getModuleID();

        if (userQuizIDs.contains(quizzes.get(holder.getAdapterPosition()).getId())) {
            holder.tvItemQuizTitle.setAlpha(0.87f);

            if (passedQuizIDs.contains(quizzes.get(holder.getAdapterPosition()).getId())) {
                holder.ivItemQuizDone.setVisibility(View.VISIBLE);
            }
        } else {
            holder.mcvItemQuizContainer.setEnabled(false);
            holder.tvItemQuizTitle.setAlpha(0.30f);
        }

        holder.tvItemQuizTitle.setText(quizzes.get(holder.getAdapterPosition()).getTitle());
        holder.mcvItemQuizContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                QuizHandler.getInstance(mContext).getUserQuizID(quizzes.get(holder.getAdapterPosition()).getId(), new VolleyResponseListener() {
                    @Override
                    public void onError(String response) {
                        Log.e(TAG, "onError: getUserQuizID: " + response);
                    }

                    @Override
                    public void onResponse(String response) {
                        String userQuizID = response;

                        QuizHandler.getInstance(mContext).getMaxQuizAttempt(userQuizID, new VolleyResponseListener() {
                            @Override
                            public void onError(String response) {
                                Log.e(TAG, "onError: getMaxQuizAttempt " + response);
                            }

                            @Override
                            public void onResponse(String response) {
                                if (response == null || response.equals("")) {
                                    attempt = "1";
                                    QuizHandler.getInstance(mContext).addQuizAttempt(quizzes.get(holder.getAdapterPosition()).getId(), userQuizID, attempt, new VolleyResponseListener() {
                                        @Override
                                        public void onError(String response) {
                                            Log.e(TAG, "onError: addQuizAttempt: " + response);
                                        }

                                        @Override
                                        public void onResponse(String response) {
                                            goToQuiz(holder, attempt, userQuizID);
                                        }
                                    });
                                } else {
                                    attempt = response;
                                    QuizHandler.getInstance(mContext).checkQuizAttempt(userQuizID, attempt, new VolleyResponseListener() {
                                        @Override
                                        public void onError(String response) {
                                            Log.e(TAG, "onError: checkQuizAttempt: " + response);
                                        }

                                        @Override
                                        public void onResponse(String response) {
                                            if (response.equals("Success")) {
                                                int temp = Integer.parseInt(attempt) + 1;
                                                String newAttempt = Integer.toString(temp);
                                                QuizHandler.getInstance(mContext).addQuizAttempt(quizzes.get(holder.getAdapterPosition()).getId(), userQuizID, newAttempt, new VolleyResponseListener() {
                                                    @Override
                                                    public void onError(String response) {
                                                        Log.e(TAG, "onError: addQuizAttempt: " + response);
                                                    }

                                                    @Override
                                                    public void onResponse(String response) {
                                                        goToQuiz(holder, newAttempt, userQuizID);
                                                    }
                                                });
                                            } else {
                                                goToQuiz(holder, attempt, userQuizID);
                                            }
                                        }
                                    });
                                }
                            }
                        });
                    }
                });
            }
        });
    }

    private void goToQuiz(ViewHolder holder, String attempt, String userQuizID) {
        Intent intent = new Intent(mContext, Quiz.class);
        intent.putExtra("quiz_id", quizzes.get(holder.getAdapterPosition()).getId());
        intent.putExtra("quiz_module_id", quizzes.get(holder.getAdapterPosition()).getModuleID());
        intent.putExtra("quiz_title", quizzes.get(holder.getAdapterPosition()).getTitle());
        intent.putExtra("passing", quizzes.get(holder.getAdapterPosition()).getPassing());
        intent.putExtra("items", quizzes.get(holder.getAdapterPosition()).getItems());
        intent.putExtra("user_max_quiz_attempt", attempt);
        intent.putExtra("user_quiz_id", userQuizID);

        mContext.startActivity(intent);
    }

    public void setQuizzes(ArrayList<com.example.expresso.models.Quiz> quizzes) {
        this.quizzes = quizzes;
    }

    public void setModuleID(String moduleID) {
        this.moduleID = moduleID;
    }

    public void setUserQuizIDs(ArrayList<String> userQuizIDs) {
        this.userQuizIDs = userQuizIDs;
    }

    public void setPassedQuizIDs(ArrayList<String> passedQuizIDs) {
        this.passedQuizIDs = passedQuizIDs;
    }

    @Override
    public int getItemCount() {
        return quizzes.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        //views
        private MaterialCardView mcvItemQuizContainer;
        private ImageView ivItemQuizDone;
        private TextView tvItemQuizTitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mcvItemQuizContainer = itemView.findViewById(R.id.mcv_item_quiz_container);
            ivItemQuizDone = itemView.findViewById(R.id.iv_item_quiz_done);
            tvItemQuizTitle = itemView.findViewById(R.id.tv_item_quiz_title);
        }
    }
}
