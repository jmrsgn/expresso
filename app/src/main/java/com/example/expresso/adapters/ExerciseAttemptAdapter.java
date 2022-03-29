package com.example.expresso.adapters;

import android.content.Context;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expresso.models.ExerciseAttempt;
import com.example.expresso.R;
import com.google.android.material.card.MaterialCardView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ExerciseAttemptAdapter extends RecyclerView.Adapter<ExerciseAttemptAdapter.ViewHolder> {
    // constants
    private static final String TAG = "ExerciseAttemptAdapter";

    // variables
    private Context mContext;
    private ArrayList<ExerciseAttempt> exerciseAttempts;
    private String exerciseID;

    public ExerciseAttemptAdapter(Context mContext) {
        this.mContext = mContext;
        initAll();
    }

    private void initAll() {
        exerciseAttempts = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_exercise_attempt, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseAttemptAdapter.ViewHolder holder, int position) {
        try {
            String stringDate = exerciseAttempts.get(holder.getAdapterPosition()).getDate();
            Date date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(stringDate);

            DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
            String strDate = dateFormat.format(date);

            String[] splitDate = strDate.split(" ");
            holder.tvItemExerciseAttemptDate.setText(splitDate[1] + " " + splitDate[0] + ", " + splitDate[2]);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        holder.tvItemExerciseAttemptRemarks.setText(exerciseAttempts.get(holder.getAdapterPosition()).getRemarks());
        holder.tvItemExerciseAttemptCode.setText(exerciseAttempts.get(holder.getAdapterPosition()).getCode());

        if (holder.tvItemExerciseAttemptRemarks.getText().equals("passed")) {
            holder.tvItemExerciseAttemptRemarks.setTextColor(mContext.getColor(R.color.cb_green));
        } else {
            holder.tvItemExerciseAttemptRemarks.setTextColor(mContext.getColor(R.color.cb_red));
        }
    }

    @Override
    public int getItemCount() {
        return exerciseAttempts.size();
    }

    public void setExerciseAttempts(ArrayList<ExerciseAttempt> exerciseAttempts) {
        this.exerciseAttempts = exerciseAttempts;
    }

    public void setExerciseID(String exerciseID) {
        this.exerciseID = exerciseID;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        //views
        private MaterialCardView mcvItemExerciseAttemptCode;
        private CardView cvItemExerciseAttempt;
        private ImageView ivItemExerciseAttemptShowCode, ivItemExerciseAttemptHideCode;
        private TextView tvItemExerciseAttemptDate, tvItemExerciseAttemptRemarks, tvItemExerciseAttemptCode;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mcvItemExerciseAttemptCode = itemView.findViewById(R.id.mcv_item_exercise_attempt_code);
            cvItemExerciseAttempt = itemView.findViewById(R.id.cv_item_exercise_attempt);
            ivItemExerciseAttemptShowCode = itemView.findViewById(R.id.iv_item_exercise_attempt_show_code);
            ivItemExerciseAttemptHideCode = itemView.findViewById(R.id.iv_item_exercise_attempt_hide_code);
            tvItemExerciseAttemptDate = itemView.findViewById(R.id.tv_item_exercise_attempt_date);
            tvItemExerciseAttemptRemarks = itemView.findViewById(R.id.tv_item_exercise_attempt_remarks);
            tvItemExerciseAttemptCode = itemView.findViewById(R.id.tv_item_exercise_attempt_code);

            AutoTransition autoTransition = new AutoTransition();
            autoTransition.setDuration(100);

            ivItemExerciseAttemptShowCode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TransitionManager.beginDelayedTransition(cvItemExerciseAttempt, autoTransition);
                    ivItemExerciseAttemptShowCode.setVisibility(View.GONE);
                    mcvItemExerciseAttemptCode.setVisibility(View.VISIBLE);
                    ivItemExerciseAttemptHideCode.setVisibility(View.VISIBLE);
                }
            });

            ivItemExerciseAttemptHideCode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TransitionManager.beginDelayedTransition(cvItemExerciseAttempt, autoTransition);
                    ivItemExerciseAttemptHideCode.setVisibility(View.GONE);
                    ivItemExerciseAttemptShowCode.setVisibility(View.VISIBLE);
                    mcvItemExerciseAttemptCode.setVisibility(View.GONE);
                }
            });
        }
    }
}

