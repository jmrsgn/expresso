package com.example.expresso.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.example.expresso.R;
import com.example.expresso.adapters.ViewPagerAdapter;
import com.example.expresso.fragments.ExerciseAttemptsFragment;
import com.example.expresso.fragments.ExerciseEditorFragment;
import com.example.expresso.fragments.ExerciseInfoFragment;
import com.google.android.material.tabs.TabLayout;

public class Exercise extends AppCompatActivity {
    // constants
    private static final String TAG = "Exercise";
    private final Context thisContext = Exercise.this;

    // views
    private Toolbar tbExercise;
    private ViewPager vpExercise;
    private TabLayout tlExercise;

    // variables
    private Intent intent;
    private static String userExerciseID, id, title, difficulty, moduleID, description, slug, templateCode, hint, initialInput, solution, exercisesCount;

    // fragments
    private ExerciseInfoFragment exerciseInfoFragment;
    private ExerciseEditorFragment exerciseEditorFragment;
    private ExerciseAttemptsFragment exerciseAttemptsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise);

        intent = getIntent();
        userExerciseID = intent.getStringExtra("user_exercise_id");
        id = intent.getStringExtra("id");
        title = intent.getStringExtra("title");
        difficulty = intent.getStringExtra("difficulty");
        moduleID = intent.getStringExtra("module_id");
        description = intent.getStringExtra("description");
        slug = intent.getStringExtra("slug");
        templateCode = intent.getStringExtra("template_code");
        hint = intent.getStringExtra("hint");
        initialInput = intent.getStringExtra("initial_input");
        solution = intent.getStringExtra("solution");
        exercisesCount = intent.getStringExtra("exercises_count");

        initAll();
    }

    public static String getUserExerciseID() { return userExerciseID; }
    public static String getExerciseID() { return id; }
    public static String getExerciseTitle() { return title; }
    public static String getExerciseDifficulty() { return difficulty; }
    public static String getExerciseModuleID() {
        return moduleID;
    }
    public static String getExerciseDescription() { return description; }
    public static String getExerciseSlug() { return slug; }
    public static String getExerciseTemplateCode() { return templateCode; }
    public static String getExerciseHint() { return hint; }
    public static String getExerciseInitialInput() { return initialInput; }
    public static String getExerciseSolution() { return solution; }
    public static String getExercisesCount() { return exercisesCount; }

    private void initAll() {
        tbExercise = findViewById(R.id.mtb_exercise);

        vpExercise = findViewById(R.id.vp_exercise);
        tlExercise = findViewById(R.id.tl_exercise);
        tlExercise.setupWithViewPager(vpExercise);

        // fragments
        exerciseInfoFragment = new ExerciseInfoFragment(thisContext);
        exerciseEditorFragment = new ExerciseEditorFragment(thisContext);
        exerciseAttemptsFragment = new ExerciseAttemptsFragment(thisContext);

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), 0);
        viewPagerAdapter.addFragment(exerciseInfoFragment, "Info");
        viewPagerAdapter.addFragment(exerciseEditorFragment, "Editor");
        viewPagerAdapter.addFragment(exerciseAttemptsFragment, "Attempts");

        vpExercise.setAdapter(viewPagerAdapter);

        // listeners
        tbExercise.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(thisContext, Topics.class);
                intent.putExtra("FROM_ACTIVITY", "Exercise");
                intent.putExtra("module_id", moduleID);
                startActivity(intent);
            }
        });
    }
}