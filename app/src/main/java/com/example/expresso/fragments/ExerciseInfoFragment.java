package com.example.expresso.fragments;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.example.expresso.R;
import com.example.expresso.activities.Exercise;
import com.example.expresso.interfaces.VolleyResponseListener;
import com.example.expresso.utils.ExerciseHandler;
import com.example.expresso.utils.Generals;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ExerciseInfoFragment extends Fragment {
    // constants
    private static final String TAG = "ExerciseInfoFragment";
    private Context mContext;

    // views
    private TextView tvExerciseInfoFragmentTitle, tvExerciseInfoFragmentDifficulty, tvExerciseInfoFragmentDescription, tvExerciseInfoFragmentRobotHelpMessage;
    private LinearLayout llExerciseInfoFragmentExamples;
    private LinearLayout.LayoutParams lpcvExerciseExample, lpllInfo, lpllExerciseExample, lpllInfoRight, lpllInfoLeft;
    private ImageView ivExerciseInfoFragmentRobotHint;

    // variables;
    private String id, title, difficulty, moduleID, description, templateCode, hint, initialInput;

    public ExerciseInfoFragment(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exercise_info, container, false);
        initAll(view);

        Exercise activity = (Exercise) getActivity();
        id = activity.getExerciseID();
        title = activity.getExerciseTitle();
        difficulty = activity.getExerciseDifficulty();
        moduleID = activity.getExerciseModuleID();
        description = activity.getExerciseDescription();
        templateCode = activity.getExerciseTemplateCode();
        hint = activity.getExerciseHint();
        initialInput = activity.getExerciseInitialInput();

        setValues();

        ivExerciseInfoFragmentRobotHint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Generals.fadeOutView(tvExerciseInfoFragmentRobotHelpMessage);
                tvExerciseInfoFragmentRobotHelpMessage.setText(hint);
                Generals.fadeInView(tvExerciseInfoFragmentRobotHelpMessage);
            }
        });


        return view;
    }

    private void setValues() {
        tvExerciseInfoFragmentTitle.setText(title);
        tvExerciseInfoFragmentDifficulty.setText(difficulty);
        tvExerciseInfoFragmentDescription.setText(description);

        if (difficulty.equals("easy")) {
            tvExerciseInfoFragmentDifficulty.setTextColor(getResources().getColor(R.color.cb_green));
        } else if (difficulty.equals("medium")) {
            tvExerciseInfoFragmentDifficulty.setTextColor(getResources().getColor(R.color.cb_yellow));
        } else if (difficulty.equals("hard")) {
            tvExerciseInfoFragmentDifficulty.setTextColor(getResources().getColor(R.color.cb_red));
        }

        ExerciseHandler.getExerciseExamples(mContext, id, new VolleyResponseListener() {
            @Override
            public void onError(String response) {
                Log.e(TAG, "onError: getExerciseExamples" + response);
            }

            @Override
            public void onResponse(String response) {
                float scale = getResources().getDisplayMetrics().density;
                int padding_12dp = (int) (12 * scale + 0.5f);
                int padding_6dp = (int) (6 * scale * 0.5f);

                try {
                    JSONArray array = new JSONArray(response);

                    for (int i = 0; i < array.length(); i ++) {
                        JSONObject object = array.getJSONObject(i);
                        Typeface tfOpenSansRegular = ResourcesCompat.getFont(mContext, R.font.open_sans_regular);

                        MaterialCardView mcvExerciseExample = new MaterialCardView(mContext);
                        mcvExerciseExample.setLayoutParams(lpcvExerciseExample);
                        mcvExerciseExample.setBackground(getResources().getDrawable(R.drawable.rounded_corners_background_surface));

                        // =====

                        LinearLayout llInfoInput = new LinearLayout(mContext);
                        llInfoInput.setOrientation(LinearLayout.HORIZONTAL);
                        llInfoInput.setLayoutParams(lpllInfo);
                        llInfoInput.setPadding(padding_6dp, padding_6dp, padding_6dp, padding_6dp);

                        // =====

                        LinearLayout llInputIeft = new LinearLayout(mContext);
                        llInputIeft.setLayoutParams(lpllInfoLeft);

                        // =====

                        TextView tvInputTitle = new TextView(mContext);
                        tvInputTitle.setText("Input:");
                        tvInputTitle.setTextColor(getResources().getColor(R.color.cb_on_surface_white));
                        tvInputTitle.setTypeface(tfOpenSansRegular);
                        tvInputTitle.setAlpha(0.87f);
                        llInputIeft.addView(tvInputTitle);

                        // =====

                        LinearLayout llInputRight = new LinearLayout(mContext);
                        llInputRight.setLayoutParams(lpllInfoRight);

                        // =====

                        TextView tvInput = new TextView(mContext);
                        tvInput.setText(object.getString("input"));
                        tvInput.setTextColor(getResources().getColor(R.color.cb_on_surface_white));
                        tvInput.setTypeface(tfOpenSansRegular);
                        tvInput.setAlpha(0.87f);
                        llInputRight.addView(tvInput);

                        // =====

                        LinearLayout llInfoOutput = new LinearLayout(mContext);
                        llInfoOutput.setOrientation(LinearLayout.HORIZONTAL);
                        llInfoOutput.setLayoutParams(lpllInfo);
                        llInfoOutput.setPadding(padding_6dp, padding_6dp, padding_6dp, padding_6dp);

                        // =====

                        LinearLayout llOutputIeft = new LinearLayout(mContext);
                        llOutputIeft.setLayoutParams(lpllInfoLeft);

                        // =====

                        TextView tvOutputTitle = new TextView(mContext);
                        tvOutputTitle.setText("Output:");
                        tvOutputTitle.setTextColor(getResources().getColor(R.color.cb_on_surface_white));
                        tvOutputTitle.setTypeface(tfOpenSansRegular);
                        tvOutputTitle.setAlpha(0.87f);
                        llOutputIeft.addView(tvOutputTitle);

                        // =====

                        LinearLayout llOutputRight = new LinearLayout(mContext);
                        llOutputRight.setLayoutParams(lpllInfoRight);

                        // =====

                        TextView tvOutput = new TextView(mContext);
                        tvOutput.setText(object.getString("output"));
                        tvOutput.setTextColor(getResources().getColor(R.color.cb_on_surface_white));
                        tvOutput.setTypeface(tfOpenSansRegular);
                        tvOutput.setAlpha(0.87f);
                        llOutputRight.addView(tvOutput);

                        // =====

                        llInfoInput.addView(llInputIeft);
                        llInfoInput.addView(llInputRight);
                        llInfoOutput.addView(llOutputIeft);
                        llInfoOutput.addView(llOutputRight);

                        // =====

                        LinearLayout llExerciseExample = new LinearLayout(mContext);
                        llExerciseExample.setLayoutParams(lpllExerciseExample);
                        llExerciseExample.setOrientation(LinearLayout.VERTICAL);
                        llExerciseExample.setPadding(padding_12dp, padding_12dp, padding_12dp, padding_12dp);

                        llExerciseExample.addView(llInfoInput);
                        llExerciseExample.addView(llInfoOutput);

                        mcvExerciseExample.addView(llExerciseExample);
                        llExerciseInfoFragmentExamples.addView(mcvExerciseExample);
                    }

                    final Animation animShake = AnimationUtils.loadAnimation(mContext, R.anim.shake);
                    tvExerciseInfoFragmentRobotHelpMessage.startAnimation(animShake);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initAll(View view) {
        tvExerciseInfoFragmentTitle = view.findViewById(R.id.tv_exercise_info_fragment_title);
        tvExerciseInfoFragmentDifficulty = view.findViewById(R.id.tv_exercise_info_fragment_difficulty);
        tvExerciseInfoFragmentDescription = view.findViewById(R.id.tv_exercise_info_fragment_description);
        tvExerciseInfoFragmentRobotHelpMessage = view.findViewById(R.id.tv_exercise_info_fragment_robot_help_message);
        ivExerciseInfoFragmentRobotHint = view.findViewById(R.id.iv_exercise_info_fragment_robot_hint);

        llExerciseInfoFragmentExamples = view.findViewById(R.id.ll_exercise_info_fragment_examples);

        lpcvExerciseExample = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpcvExerciseExample.setMargins(0, 24, 0, 0);

        lpllInfo = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpllInfoLeft = new LinearLayout.LayoutParams(300, LinearLayout.LayoutParams.MATCH_PARENT);
        lpllInfoRight = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        lpllExerciseExample = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    }
}