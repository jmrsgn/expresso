package com.example.expresso.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.example.expresso.R;
import com.example.expresso.interfaces.VolleyResponseListener;
import com.example.expresso.utils.Constants;
import com.example.expresso.utils.Generals;
import com.example.expresso.utils.ModuleHandler;
import com.example.expresso.utils.QuizHandler;
import com.example.expresso.utils.SessionHandler;
import com.example.expresso.utils.TopicHandler;
import com.example.expresso.utils.UserHandler;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

public class Home extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    // constants
    private static final String TAG = "Home";
    private final Context thisContext = Home.this;

    // views
    private TextView tvNdHeaderHomeEmail, tvNdHeaderHomeJoinedDate, tvNdHeaderHomeName, tvHomeName, tvItemLatestModuleTitle, tvItemLatestModuleDescription, tvHomeContinueReadingTitle, tvHomeItemModulesCount, tvHomeItemTopicsCount, tvHomeItemQuizzesCount, tvHomeItemExercisesCount, tvHomeItemModuleProgressCountInPB;
    private Toolbar tbHomeToolbar;
    private DrawerLayout dlHomeContainer;
    private NavigationView nvHomeContainer;
    private View vHeaderHome;
    private ImageView ivNdHeaderHomeUserAvatar;
    private Button btnHomeNdItemLogout;
    private LinearLayout llHomeInfoLoading, llHomeNoInternetConnection, llHomeLatestModule, llHomeProgressInfo;
    private ProgressBar pbHomeItemModuleProgress;
    private RelativeLayout rlHomeUserGreetings;
    private MaterialCardView mcvHomeItemLatestModuleContainer;

    // google oauth
    private GoogleSignInAccount account;

    // variables
    private String attempt, moduleID, moduleTitle, moduleDescription, userDoneModulesCount, userDoneTopicsCount, userDoneQuizzesCount, userDoneExercisesCount;
    private Gson gson;
    private SharedPreferences spModulesInfo, spTopicsInfo, spTopicsPathIndexes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initAll();

        btnHomeNdItemLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SessionHandler.getInstance(thisContext).signOut();
                startActivity(new Intent(thisContext, MainActivity.class));

                UserHandler.getInstance(thisContext).logLoggedOutFromTheSystem();
            }
        });

        mcvHomeItemLatestModuleContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<String> topicsPathIndexes = new ArrayList<>();

                String topicsPathIndexesJson = spTopicsPathIndexes.getString(moduleID, null);
                String[] topicsPathIndexesArray = gson.fromJson(topicsPathIndexesJson, String[].class);

                Collections.addAll(topicsPathIndexes, topicsPathIndexesArray);

                Map<String,?> keys = spTopicsInfo.getAll();
                for(Map.Entry<String,?> entry : keys.entrySet()){
                    String json_ = spTopicsInfo.getString(entry.getKey(), null);
                    String[] topicInfo = gson.fromJson(json_, String[].class);

                    if (topicInfo[4].equals(topicsPathIndexes.get(0)) && topicInfo[2].equals(moduleID)) {
                        TopicHandler.getInstance(thisContext).checkTopicID(topicInfo[0], new VolleyResponseListener() {
                            @Override
                            public void onError(String response) {
                                Log.e(TAG, "onError: checkTopicID: " + response);
                            }

                            @Override
                            public void onResponse(String response) {
                                if (response.equals("Success")) {
                                    goToTopics(moduleID);
                                } else {
                                    TopicHandler.getInstance(thisContext).addTopic(topicInfo[0], new VolleyResponseListener() {
                                        @Override
                                        public void onError(String response) {
                                            Log.e(TAG, "onError: addTopic: " + response);
                                        }

                                        @Override
                                        public void onResponse(String response) {
                                            goToTopics(moduleID);
                                        }
                                    });
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    private void goToTopics(String moduleID) {
        Intent intent = new Intent(thisContext, Topics.class);
        intent.putExtra("FROM_ACTIVITY", "Modules");
        intent.putExtra("module_id", moduleID);
        startActivity(intent);
    }

    private void hideViews() {
        llHomeLatestModule.setVisibility(View.GONE);
        llHomeProgressInfo.setVisibility(View.GONE);
        rlHomeUserGreetings.setVisibility(View.GONE);
    }

    private void showViews() {
        llHomeLatestModule.setVisibility(View.VISIBLE);
        llHomeProgressInfo.setVisibility(View.VISIBLE);
        rlHomeUserGreetings.setVisibility(View.VISIBLE);
    }

    private void initAll() {
        // views
        tbHomeToolbar = findViewById(R.id.mtb_home_toolbar);
        dlHomeContainer = findViewById(R.id.dl_home_container);
        nvHomeContainer = findViewById(R.id.nv_home_container);
        llHomeInfoLoading = findViewById(R.id.ll_home_info_loading);
        llHomeNoInternetConnection = findViewById(R.id.ll_home_no_internet_connection);
        llHomeLatestModule = findViewById(R.id.ll_home_latest_module);
        llHomeProgressInfo = findViewById(R.id.ll_home_progress_info);
        rlHomeUserGreetings = findViewById(R.id.rl_home_user_greetings);
        tvItemLatestModuleTitle = findViewById(R.id.tv_item_latest_module_title);
        tvItemLatestModuleDescription = findViewById(R.id.tv_item_latest_module_description);
        tvHomeName = findViewById(R.id.tv_home_name);
        tvHomeContinueReadingTitle = findViewById(R.id.tv_home_continue_reading_title);
        tvHomeItemModulesCount = findViewById(R.id.tv_home_item_modules_count);
        tvHomeItemTopicsCount = findViewById(R.id.tv_home_item_topics_count);
        tvHomeItemQuizzesCount = findViewById(R.id.tv_home_item_quizzes_count);
        tvHomeItemExercisesCount = findViewById(R.id.tv_home_item_exercises_count);
        tvHomeItemModuleProgressCountInPB = findViewById(R.id.tv_home_item_module_progress_count_in_pb);
        pbHomeItemModuleProgress = findViewById(R.id.pb_home_item_module_progress);
        vHeaderHome = nvHomeContainer.getHeaderView(0);
        tvNdHeaderHomeJoinedDate = vHeaderHome.findViewById(R.id.tv_nd_header_home_joined_date);
        tvNdHeaderHomeEmail = vHeaderHome.findViewById(R.id.tv_nd_header_home_email);
        tvNdHeaderHomeName = vHeaderHome.findViewById(R.id.tv_nd_header_home_name);
        ivNdHeaderHomeUserAvatar = vHeaderHome.findViewById(R.id.iv_nd_header_home_user_avatar);
        btnHomeNdItemLogout = findViewById(R.id.btn_home_nd_item_logout);
        mcvHomeItemLatestModuleContainer = findViewById(R.id.mcv_home_item_latest_module_container);

        // variables
        gson = new Gson();
        spModulesInfo = getSharedPreferences(Constants.SP_MODULES_INFO, MODE_PRIVATE);
        spTopicsInfo = getSharedPreferences(Constants.SP_TOPICS_INFO, Context.MODE_PRIVATE);
        spTopicsPathIndexes = getSharedPreferences(Constants.SP_TOPICS_PATH_INDEXES, Context.MODE_PRIVATE);

        // account
        account = GoogleSignIn.getLastSignedInAccount(this);
        UserHandler.getInstance(thisContext).setUserInfo(account);

        if (Generals.checkInternetConnection(thisContext)) {
            Generals.setUpOfflineModules(thisContext);
        }

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                dlHomeContainer,
                tbHomeToolbar,
                R.string.nd_open,
                R.string.nd_close
        );

        dlHomeContainer.setDrawerElevation(0);

        // listeners
        dlHomeContainer.addDrawerListener(toggle);
        toggle.syncState();
        nvHomeContainer.setNavigationItemSelectedListener(this);

        // ==========

        // start
        hideViews();
        llHomeInfoLoading.setVisibility(View.VISIBLE);

        if (Generals.checkInternetConnection(thisContext)) {
            ModuleHandler.getInstance(thisContext).checkUserModules(new VolleyResponseListener() {
                @Override
                public void onError(String response) {
                    Log.e(TAG, "onError: checkUserModules: " + response);
                }

                @Override
                public void onResponse(String response) {
                    if (response.equals("Failed")) {
                        QuizHandler.getInstance(thisContext).getPretestInfo(new VolleyResponseListener() {
                            @Override
                            public void onError(String response) {
                                Log.e(TAG, "onError: getPretestInfo: " + response);
                            }

                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONArray array = new JSONArray(response);
                                    JSONObject object = array.getJSONObject(0);

                                    String passing = object.getString("passing");
                                    String items = object.getString("items");

                                    showPretestDialog(passing, items);
                                    llHomeInfoLoading.setVisibility(View.GONE);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    } else {
                        setValues();
                    }
                }
            });
        } else {
            setValues();
        }
    }

    private void setValues() {
        if (account != null) {
            if (Generals.checkInternetConnection(thisContext)) {
                getValues();
            } else {
                tvNdHeaderHomeJoinedDate.setVisibility(View.GONE);
                llHomeNoInternetConnection.setVisibility(View.VISIBLE);
                llHomeInfoLoading.setVisibility(View.GONE);
            }

            tvHomeName.setText("Welcome " + account.getGivenName() + "!");
            tvNdHeaderHomeEmail.setText(modifyEmail(Constants.EMAIL));
            tvNdHeaderHomeName.setText((Constants.GIVEN_NAME + " " + Constants.FAMILY_NAME));

            if (!Constants.IMAGE_URL.equals("")) {
                Glide.with(thisContext)
                        .asBitmap()
                        .load(Constants.IMAGE_URL)
                        .into(ivNdHeaderHomeUserAvatar);
            } else {
                Glide.with(thisContext)
                        .asBitmap()
                        .load(R.drawable.avatar_icon)
                        .into(ivNdHeaderHomeUserAvatar);
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.home_nd_item_home) {
            startActivity(new Intent(thisContext, Home.class));
        } else if (id == R.id.home_nd_item_modules) {
            startActivity(new Intent(thisContext, Modules.class));
        } else if (id == R.id.home_nd_item_certifications) {
            startActivity(new Intent(thisContext, Certificate.class));
        } else if (id == R.id.home_nd_item_about_us) {
            startActivity(new Intent(thisContext, AboutUs.class));
        }

        dlHomeContainer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        // do nothing
    }

    private String modifyEmail(String email) {
        return email.charAt(0) + "*******" + email.substring(email.indexOf('@'));
    }

    private void showPretestDialog(String passing, String items) {
        Dialog pretestDialog = new Dialog(thisContext);
        pretestDialog.setContentView(R.layout.alert_box_pretest_popup);

        TextView tvAbPretestPopupTitle = (TextView) pretestDialog.findViewById(R.id.tv_ab_pretest_popup_title);
        TextView tvAbPretestPopupMessage = (TextView) pretestDialog.findViewById(R.id.tv_ab_pretest_popup_message);
        Button btnAbPretestPopContinue = (Button) pretestDialog.findViewById(R.id.btn_ab_pretest_popup_continue);
        Button btnAbPretestPopLogout = (Button) pretestDialog.findViewById(R.id.btn_ab_pretest_popup_logout);

        tvAbPretestPopupTitle.setText(Html.fromHtml("Hi <font color=#BB86FC>" + account.getGivenName() + "!</font>"));
        tvAbPretestPopupMessage.setText("Welcome to Expresso, " + account.getGivenName() + "! \uD83C\uDF89\n" + "Before you proceed, you’ll need to take a pretest first so I can evaluate your knowledge.\n" + "Click CONTINUE to take the pretest.");

        btnAbPretestPopLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SessionHandler.getInstance(thisContext).signOut();
                startActivity(new Intent(thisContext, MainActivity.class));

                UserHandler.getInstance(thisContext).logLoggedOutFromTheSystem();
            }
        });

        btnAbPretestPopContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                QuizHandler.getInstance(thisContext).getUserQuizID("1", new VolleyResponseListener() {
                    @Override
                    public void onError(String response) {
                        Log.e(TAG, "onError: getUserQuizID: " + response);
                    }

                    @Override
                    public void onResponse(String response) {
                        String userQuizID = response;

                        QuizHandler.getInstance(thisContext).getMaxQuizAttempt(userQuizID, new VolleyResponseListener() {
                            @Override
                            public void onError(String response) {
                                Log.e(TAG, "onError: getMaxQuizAttempt: " + response);
                            }

                            @Override
                            public void onResponse(String response) {
                                if (response == null || response.equals("") || response.equals("0")) {
                                    attempt = "1";
                                    QuizHandler.getInstance(thisContext).addQuizAttempt("1", userQuizID, attempt, new VolleyResponseListener() {
                                        @Override
                                        public void onError(String response) {
                                            Log.e(TAG, "onError: addQuizAttempt: " + response);
                                        }

                                        @Override
                                        public void onResponse(String response) {
                                            goToQuiz(attempt, userQuizID, passing, items);
                                        }
                                    });
                                } else {
                                    attempt = response;
                                    QuizHandler.getInstance(thisContext).checkQuizAttempt(userQuizID, attempt, new VolleyResponseListener() {
                                        @Override
                                        public void onError(String response) {
                                            Log.e(TAG, "onError: checkQuizAttempt: " + response);
                                        }

                                        @Override
                                        public void onResponse(String response) {
                                            if (response.equals("Success")) {
                                                int temp = Integer.parseInt(attempt) + 1;
                                                String newAttempt = Integer.toString(temp);

                                                QuizHandler.getInstance(thisContext).addQuizAttempt("1", userQuizID, newAttempt, new VolleyResponseListener() {
                                                    @Override
                                                    public void onError(String response) {
                                                        Log.e(TAG, "onError: addQuizAttempt: " + response);
                                                    }

                                                    @Override
                                                    public void onResponse(String response) {
                                                        goToQuiz(newAttempt, userQuizID, passing, items);
                                                    }
                                                });
                                            } else {
                                                goToQuiz(attempt, userQuizID, passing, items);
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

        pretestDialog.setCancelable(false);
        pretestDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        pretestDialog.show();
    }

    private void goToQuiz(String attempt, String userQuizID, String passing, String items) {
        Intent intent = new Intent(thisContext, Quiz.class);
        intent.putExtra("quiz_id", "1");
        intent.putExtra("quiz_module_id", "1");
        intent.putExtra("quiz_title", "Pre-test");
        intent.putExtra("passing", passing);
        intent.putExtra("items", items);
        intent.putExtra("user_max_quiz_attempt", attempt);
        intent.putExtra("user_quiz_id", userQuizID);
        startActivity(intent);
    }

    private void getValues() {
        UserHandler.getInstance(thisContext).getJoinedDate(new VolleyResponseListener() {
            @Override
            public void onError(String response) {
                Log.e(TAG, "onError: getJoinedDate: " + response);
            }

            @Override
            public void onResponse(String response) {
                try {
                    Date date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(response);
                    DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
                    String strDate = dateFormat.format(date);
                    String[] splitDate = strDate.split(" ");
                    tvNdHeaderHomeJoinedDate.setText("Joined: " + splitDate[1] + " " + splitDate[0] + ", " + splitDate[2]);

                    Generals.getUserDeliverablesCount(thisContext, new VolleyResponseListener() {
                        @Override
                        public void onError(String response) {
                            Log.e(TAG, "onError: getUserDeliverablesCount: " + response);
                        }

                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONArray arrayDeliverableCounts = new JSONArray(response);
                                JSONObject objectDeliverableCount = arrayDeliverableCounts.getJSONObject(0);

                                String userDoneModulesCountResponse = objectDeliverableCount.getString("userDoneModulesCount");
                                String userDoneTopicsCountResponse = objectDeliverableCount.getString("userDoneTopicsCount");
                                String userDoneQuizzesCountResponse = objectDeliverableCount.getString("userDoneQuizzesCount");
                                String userDoneExercisesCountResponse = objectDeliverableCount.getString("userDoneExercisesCount");

                                userDoneModulesCount = userDoneModulesCountResponse;
                                userDoneTopicsCount = userDoneTopicsCountResponse;
                                userDoneQuizzesCount = userDoneQuizzesCountResponse;
                                userDoneExercisesCount = userDoneExercisesCountResponse;

                                getLatestTopic();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void getLatestTopic() {
        ModuleHandler.getInstance(thisContext).getMaxUserModuleID(new VolleyResponseListener() {
            @Override
            public void onError(String response) {
                Log.e(TAG, "onError: getMaxUserModule: " + response);
            }

            @Override
            public void onResponse(String response) {
                String latestModuleID = response;

                String moduleInfoJson = spModulesInfo.getString(latestModuleID, null);

                if (moduleInfoJson != null) {
                    String[] moduleInfo = gson.fromJson(moduleInfoJson, String[].class);

                    moduleID = moduleInfo[0];
                    moduleTitle = moduleInfo[1];
                    moduleDescription = moduleInfo[5];

                    tvItemLatestModuleTitle.setText(moduleTitle);
                    tvItemLatestModuleDescription.setText(moduleDescription);
                }

                // getting all modules count from SP

                int allModulesCount = spModulesInfo.getAll().size();
                double overallModuleProgress = ((double) Integer.parseInt(userDoneModulesCount) / (double) allModulesCount) * 100;

                tvHomeItemModulesCount.setText(userDoneModulesCount);
                tvHomeItemTopicsCount.setText(userDoneTopicsCount);
                tvHomeItemExercisesCount.setText(userDoneExercisesCount);
                tvHomeItemQuizzesCount.setText(userDoneQuizzesCount);

                String overAllProgressString = String.valueOf((int) overallModuleProgress);
                tvHomeItemModuleProgressCountInPB.setText(overAllProgressString + "%");

                pbHomeItemModuleProgress.setProgress((int) overallModuleProgress);

                showViews();
                llHomeInfoLoading.setVisibility(View.GONE);

                if (response.equals("") || response == null) {
                    llHomeLatestModule.setVisibility(View.GONE);
                }
            }
        });
    }
}