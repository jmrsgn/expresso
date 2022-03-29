package com.example.expresso.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expresso.R;
import com.example.expresso.adapters.ModulesAdapter;
import com.example.expresso.interfaces.VolleyResponseListener;
import com.example.expresso.models.Module;
import com.example.expresso.utils.Constants;
import com.example.expresso.utils.ExerciseHandler;
import com.example.expresso.utils.Generals;
import com.example.expresso.utils.ModuleHandler;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class Modules extends AppCompatActivity {
    // constants
    private static final String TAG = "Modules";
    private final Context thisContext = Modules.this;

    // views
    private Toolbar tbModulesToolbar;
    private RecyclerView rvModulesModules;
    private LinearLayout llModulesLoading, llModulesNoResultsFound;
    private Button btnModulesUnlockNextModule;

    // variables
    private ArrayList<Module> modules;
    private ArrayList<String> unlockedModuleIDs, doneModuleIDs, modulesPathIndexes;
    private ModulesAdapter modulesAdapter;
    private SharedPreferences spUserSavedModulesID, spModulesInfo, spModulesPathIndexes;
    private String nextModuleID;
    private Gson gson;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modules);

        initAll();

        if (Generals.checkInternetConnection(thisContext)) {
            loadOnlineModules();
        } else {
            loadOfflineModules();
        }

        btnModulesUnlockNextModule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ModuleHandler.getInstance(thisContext).addModule(nextModuleID, new VolleyResponseListener() {
                    @Override
                    public void onError(String response) {
                        Log.e(TAG, "onError: addModule: " + response);
                    }

                    @Override
                    public void onResponse(String response) {
                        rvModulesModules.setVisibility(View.GONE);
                        btnModulesUnlockNextModule.setVisibility(View.GONE);
                        showLoadingScreen();
                        reloadModules();
                    }
                });
            }
        });
    }

    private void reloadModules() {
        modules.clear();

        ModuleHandler.getInstance(thisContext).getUnlockedModuleIDs(new VolleyResponseListener() {
            @Override
            public void onError(String response) {
                Log.e(TAG, "onError: getUnlockedModuleIDs: " + response);
            }

            @Override
            public void onResponse(String response) {
                try {
                    JSONArray arrayModuleIDs = new JSONArray(response);

                    for (int i = 0; i < arrayModuleIDs.length(); i ++) {
                        JSONObject objectModuleID = arrayModuleIDs.getJSONObject(i);

                        unlockedModuleIDs.add(objectModuleID.getString("module_id"));
                    }

                    ModuleHandler.getInstance(thisContext).getDoneModuleIDs(new VolleyResponseListener() {
                        @Override
                        public void onError(String response) {
                            Log.e(TAG, "onError: getDoneModuleIDs: " + response);
                        }

                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONArray arrayModuleIDs = new JSONArray(response);

                                for (int i = 0; i < arrayModuleIDs.length(); i ++) {
                                    JSONObject objectModuleID = arrayModuleIDs.getJSONObject(i);

                                    doneModuleIDs.add(objectModuleID.getString("module_id"));
                                }

                                ArrayList<String> modulesPathIndexes = new ArrayList<>();
                                String modulesPathIndexesJson = spModulesPathIndexes.getString(Constants.ID, null);
                                String[] modulesPathIndexesArray = gson.fromJson(modulesPathIndexesJson, String[].class);

                                Collections.addAll(modulesPathIndexes, modulesPathIndexesArray);

                                for (int i = 0; i < modulesPathIndexes.size(); i ++) {
                                    Map<String,?> keys = spModulesInfo.getAll();
                                    for(Map.Entry<String,?> entry : keys.entrySet()){
                                        gson = new Gson();

                                        String json_ = spModulesInfo.getString(entry.getKey(), null);
                                        String[] moduleInfo = gson.fromJson(json_, String[].class);

                                        if (moduleInfo[4].equals(modulesPathIndexes.get(i))) {
                                            modules.add(new Module(moduleInfo[0],
                                                                   moduleInfo[1],
                                                                   moduleInfo[2],
                                                                   moduleInfo[3],
                                                                   moduleInfo[4],
                                                                   moduleInfo[5]));
                                        }
                                    }
                                }

                                displayModules(modules);
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

    private void setUpModuleNavigation(String modulePathIndex) {
        String modulesPathIndexesJson = spModulesPathIndexes.getString(Constants.ID, null);
        String[] modulesPathIndexesArray = gson.fromJson(modulesPathIndexesJson, String[].class);

        Collections.addAll(modulesPathIndexes, modulesPathIndexesArray);

        String nextPathIndex;

        if (!modulesPathIndexes.get(modulesPathIndexes.size() - 1).equals(modulePathIndex)) {
            nextPathIndex = modulesPathIndexes.get(modulesPathIndexes.indexOf(modulePathIndex) + 1);
            getModuleInfoUsingIndex(nextPathIndex);
        }
    }

    private void loadOnlineModules() {
        ModuleHandler.getInstance(thisContext).getUnlockedModuleIDs(new VolleyResponseListener() {
            @Override
            public void onError(String response) {
                Log.e(TAG, "onError: getUnlockedModuleIDs: " + response);
            }

            @Override
            public void onResponse(String response) {
                try {
                    JSONArray arrayModuleIDs = new JSONArray(response);

                    for (int i = 0; i < arrayModuleIDs.length(); i ++) {
                        JSONObject objectModuleID = arrayModuleIDs.getJSONObject(i);

                        unlockedModuleIDs.add(objectModuleID.getString("module_id"));
                    }

                    ModuleHandler.getInstance(thisContext).getDoneModuleIDs(new VolleyResponseListener() {
                        @Override
                        public void onError(String response) {
                            Log.e(TAG, "onError: getDoneModuleIDs: " + response);
                        }

                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONArray arrayModuleIDs = new JSONArray(response);

                                for (int i = 0; i < arrayModuleIDs.length(); i ++) {
                                    JSONObject objectModuleID = arrayModuleIDs.getJSONObject(i);

                                    doneModuleIDs.add(objectModuleID.getString("module_id"));
                                }

                                ModuleHandler.getInstance(thisContext).getMaxUserDoneModule(new VolleyResponseListener() {
                                    @Override
                                    public void onError(String response) {
                                        Log.e(TAG, "onError: getMaxUserDoneModule: " + response);
                                    }

                                    @Override
                                    public void onResponse(String response) {
                                        if (response.equals("") || response.equals("[]") || response == null) {
                                            // do nothing
                                        } else {
                                            try {
                                                JSONArray array = new JSONArray(response);
                                                JSONObject object = array.getJSONObject(0);

                                                String maxUserDoneModuleID = object.getString("max_user_done_module_id");
                                                String maxUserDoneModulePathIndex = object.getString("max_user_done_module_path_index");

                                                ExerciseHandler.getUserExercises(thisContext, maxUserDoneModuleID, new VolleyResponseListener() {
                                                    @Override
                                                    public void onError(String response) {
                                                        Log.e(TAG, "onError: getUserExercises: " + response);
                                                    }

                                                    @Override
                                                    public void onResponse(String response) {
                                                        try {
                                                            JSONArray array1 = new JSONArray(response);

                                                            String generatedExercisesCount = String.valueOf(array1.length());

                                                            Generals.getUserPassedModuleQuizAndGeneratedExercisesCount(thisContext, maxUserDoneModuleID, new VolleyResponseListener() {
                                                                @Override
                                                                public void onError(String response) {
                                                                    Log.e(TAG, "onError: getUserPassedModuleQuizAndGeneratedExercisesCount: " + response);
                                                                }

                                                                @Override
                                                                public void onResponse(String response) {
                                                                    try {
                                                                        JSONArray array_ = new JSONArray(response);
                                                                        JSONObject object_ = array_.getJSONObject(0);

                                                                        String userPassedGeneratedModuleExercisesCount = object_.getString("user_passed_generated_module_exercises_count");
                                                                        String userPassedModuleSummativeCount = object_.getString("user_passed_module_summative_count");

                                                                        if (userPassedGeneratedModuleExercisesCount.equals(generatedExercisesCount) && userPassedModuleSummativeCount.equals("1")) {
                                                                            setUpModuleNavigation(maxUserDoneModulePathIndex);
                                                                        }
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
                                    }
                                });

                                ArrayList<String> modulesPathIndexes = new ArrayList<>();
                                String modulesPathIndexesJson = spModulesPathIndexes.getString(Constants.ID, null);
                                String[] modulesPathIndexesArray = gson.fromJson(modulesPathIndexesJson, String[].class);

                                Collections.addAll(modulesPathIndexes, modulesPathIndexesArray);

                                for (int i = 0; i < modulesPathIndexes.size(); i ++) {
                                    Map<String,?> keys = spModulesInfo.getAll();
                                    for(Map.Entry<String,?> entry : keys.entrySet()){
                                        gson = new Gson();

                                        String json_ = spModulesInfo.getString(entry.getKey(), null);
                                        String[] moduleInfo = gson.fromJson(json_, String[].class);

                                        if (moduleInfo[4].equals(modulesPathIndexes.get(i))) {
                                            modules.add(new Module(moduleInfo[0],
                                                                   moduleInfo[1],
                                                                   moduleInfo[2],
                                                                   moduleInfo[3],
                                                                   moduleInfo[4],
                                                                   moduleInfo[5]));
                                        }
                                    }
                                }

                                displayModules(modules);
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

    private void getModuleInfoUsingIndex(String nextModulePathIndex) {
        Map<String,?> keys = spModulesInfo.getAll();
        for(Map.Entry<String,?> entry : keys.entrySet()){
            gson = new Gson();

            String json = spModulesInfo.getString(entry.getKey(), null);
            String[] moduleInfo = gson.fromJson(json, String[].class);

            if (moduleInfo[4].equals(nextModulePathIndex)) {
                ModuleHandler.getInstance(thisContext).checkModuleID(entry.getKey(), new VolleyResponseListener() {
                    @Override
                    public void onError(String response) {
                        Log.e(TAG, "onError: checkModuleID: " + response);
                    }

                    @Override
                    public void onResponse(String response) {
                        if (response.equals("Failed")) {
                            nextModuleID = entry.getKey();
                            btnModulesUnlockNextModule.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        }
    }

    private void initAll() {
        // views
        tbModulesToolbar = findViewById(R.id.mtb_modules_toolbar);
        rvModulesModules = findViewById(R.id.rv_modules_modules);
        llModulesLoading = findViewById(R.id.ll_modules_loading);
        llModulesNoResultsFound = findViewById(R.id.ll_modules_no_results_found);
        btnModulesUnlockNextModule = findViewById(R.id.btn_modules_unlock_next_module);

        // variables
        spUserSavedModulesID = thisContext.getSharedPreferences(Constants.SP_USER_SAVED_MODULES_ID, MODE_PRIVATE);
        spModulesInfo = thisContext.getSharedPreferences(Constants.SP_MODULES_INFO, MODE_PRIVATE);
        spModulesPathIndexes = getSharedPreferences(Constants.SP_MODULES_PATH_INDEXES, Context.MODE_PRIVATE);
        gson = new Gson();
        modules = new ArrayList<>();
        unlockedModuleIDs = new ArrayList<>();
        modulesPathIndexes = new ArrayList<>();
        doneModuleIDs = new ArrayList<>();

        // listeners
        tbModulesToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (llModulesLoading.getVisibility() == View.GONE) {
                    startActivity(new Intent(thisContext, Home.class));
                    finish();
                }
            }
        });

        showLoadingScreen();
    }

    private void loadOfflineModules() {
        ArrayList<String> modulesPathIndexes = new ArrayList<>();
        ArrayList<String> arrangedModuleIDs = new ArrayList<>();
        String modulesPathIndexesJson = spModulesPathIndexes.getString(Constants.ID, null);
        String[] modulesPathIndexesArray = gson.fromJson(modulesPathIndexesJson, String[].class);

        Collections.addAll(modulesPathIndexes, modulesPathIndexesArray);
        String moduleIDsJson = spUserSavedModulesID.getString(Constants.ID, null);

        if (moduleIDsJson != null) {
            String[] moduleIDs = gson.fromJson(moduleIDsJson, String[].class);

            for (int i = 0; i < modulesPathIndexes.size(); i ++) {
                for (String moduleID : moduleIDs) {
                    String json = spModulesInfo.getString(moduleID, null);
                    String[] moduleInfo = gson.fromJson(json, String[].class);

                    if (moduleInfo[4].equals(modulesPathIndexes.get(i))) {
                        arrangedModuleIDs.add(moduleID);
                    }
                }
            }

            for (int m = 0; m < arrangedModuleIDs.size(); m ++) {
                Map<String,?> keys = spModulesInfo.getAll();
                for (Map.Entry<String, ?> entry : keys.entrySet()) {
                    if (entry.getKey().equals(arrangedModuleIDs.get(m))) {
                        String moduleInfoJson = spModulesInfo.getString(entry.getKey(), null);
                        String[] moduleInfo = gson.fromJson(moduleInfoJson, String[].class);

                        modules.add(new Module(moduleInfo[0],
                                               moduleInfo[1],
                                               moduleInfo[2],
                                               moduleInfo[3],
                                               moduleInfo[4],
                                               moduleInfo[5]));
                    }
                }
            }

            displayModules(modules);
        } else {
            displayModules(modules);
        }
    }

    private void displayModules(ArrayList<Module> modules) {
        hideLoadingScreen();

        if (modules.size() == 0) {
            llModulesNoResultsFound.setVisibility(View.VISIBLE);
        } else {
            rvModulesModules.setVisibility(View.VISIBLE);
            modulesAdapter = new ModulesAdapter(thisContext);
            rvModulesModules.setAdapter(modulesAdapter);
            rvModulesModules.setLayoutManager(new LinearLayoutManager(thisContext));
            modulesAdapter.setModules(modules);
            modulesAdapter.setUnlockedModuleIDs(unlockedModuleIDs);
            modulesAdapter.setDoneModuleIDs(doneModuleIDs);
        }
    }

    private void showLoadingScreen() {
        llModulesLoading.setVisibility(View.VISIBLE);
    }

    private void hideLoadingScreen() {
        llModulesLoading.setVisibility(View.GONE);
    }
}