package com.example.expresso.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expresso.R;
import com.example.expresso.activities.Exercise;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;

public class ExercisesAdapter extends RecyclerView.Adapter<ExercisesAdapter.ViewHolder> {
    // constants
    private static final String TAG = "ExercisesAdapter";

    // variables
    private Context mContext;
    private ArrayList<com.example.expresso.models.Exercise> exercises;
    private ArrayList<String> passedExerciseIDs;
    private String moduleID;

    public ExercisesAdapter(Context mContext) {
        this.mContext = mContext;
        initAll();
    }

    private void initAll() {
        exercises = new ArrayList<>();
        passedExerciseIDs = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_exercise, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExercisesAdapter.ViewHolder holder, int position) {
        if (passedExerciseIDs.contains(exercises.get(holder.getAdapterPosition()).getId())) {
            holder.ivItemExerciseDone.setVisibility(View.VISIBLE);
        }

        holder.tvItemExerciseTitle.setText(exercises.get(holder.getAdapterPosition()).getTitle());

        holder.mcvItemExerciseContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, Exercise.class);
                intent.putExtra("user_exercise_id", exercises.get(holder.getAdapterPosition()).getUserExerciseID());
                intent.putExtra("id", exercises.get(holder.getAdapterPosition()).getId());
                intent.putExtra("title", exercises.get(holder.getAdapterPosition()).getTitle());
                intent.putExtra("difficulty", exercises.get(holder.getAdapterPosition()).getDifficulty());
                intent.putExtra("module_id", moduleID);
                intent.putExtra("description", exercises.get(holder.getAdapterPosition()).getDescription());
                intent.putExtra("slug", exercises.get(holder.getAdapterPosition()).getSlug());
                intent.putExtra("template_code", exercises.get(holder.getAdapterPosition()).getTemplateCode());
                intent.putExtra("hint", exercises.get(holder.getAdapterPosition()).getHint());
                intent.putExtra("initial_input", exercises.get(holder.getAdapterPosition()).getInitialInput());
                intent.putExtra("solution", exercises.get(holder.getAdapterPosition()).getSolution());
                intent.putExtra("exercises_count", String.valueOf(exercises.size()));
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    public void setExercises(ArrayList<com.example.expresso.models.Exercise> exercises) {
        this.exercises = exercises;
    }

    public void setModuleID(String moduleID) {
        this.moduleID = moduleID;
    }

    public void setPassedExerciseIDs(ArrayList<String> passedExerciseIDs) {
        this.passedExerciseIDs = passedExerciseIDs;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        //views
        private MaterialCardView mcvItemExerciseContainer;
        private ImageView ivItemExerciseDone;
        private TextView tvItemExerciseTitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            ivItemExerciseDone = itemView.findViewById(R.id.iv_item_exercise_done);
            mcvItemExerciseContainer = itemView.findViewById(R.id.mcv_item_exercise_container);
            tvItemExerciseTitle = itemView.findViewById(R.id.tv_item_exercise_title);
        }
    }
}
